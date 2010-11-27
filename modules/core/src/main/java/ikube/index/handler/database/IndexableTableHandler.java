package ikube.index.handler.database;

import ikube.IConstants;
import ikube.index.IndexManager;
import ikube.index.content.ColumnContentProvider;
import ikube.index.content.IContentProvider;
import ikube.index.handler.Handler;
import ikube.index.handler.IHandler;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.DatabaseUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

public class IndexableTableHandler extends Handler {

	private IContentProvider<IndexableColumn> contentProvider;

	public IndexableTableHandler(IHandler<Indexable<?>> previous) {
		super(previous);
		this.contentProvider = new ColumnContentProvider();
	}

	@Override
	public void handle(final IndexContext indexContext, final Indexable<?> indexable) throws Exception {
		Thread thread = null;
		if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
			IndexableTable indexableTable = (IndexableTable) indexable;
			for (int i = 0; i < getThreads(); i++) {
				final IndexableTable cloneIndexableTable = (IndexableTable) SerializationUtilities.clone(indexableTable);
				final Connection connection = indexableTable.getDataSource().getConnection();
				thread = new Thread(new Runnable() {
					public void run() {
						handleTable(indexContext, cloneIndexableTable, connection, null);
					}
				});
				thread.start();
			}
		}
		thread.join();
		// Do the next handler in the chain
		super.handle(indexContext, indexable);
	}

	protected void handleTable(IndexContext indexContext, IndexableTable indexableTable, Connection connection, Document document) {
		List<Indexable<?>> children = indexableTable.getChildren();
		ResultSet resultSet = null;
		try {
			resultSet = getResultSet(indexContext, indexableTable, connection);
			do {
				if (resultSet == null) {
					break;
				}
				// Set the column types
				setColumnTypes(children, resultSet);
				// Set the id field if this is a primary table
				if (indexableTable.isPrimary()) {
					document = new Document();
					setIdField(children, indexableTable, document, resultSet);
				}
				// Handle all the columns that are 'normal', i.e. that don't have references
				// to the values in other columns, like the attachment that needs the name
				// from the name column
				ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					Indexable<?> indexable = children.get(i - 1);
					if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
						break;
					}
					IndexableColumn indexableColumn = (IndexableColumn) indexable;
					Object object = resultSet.getObject(indexableColumn.getName());
					indexableColumn.setObject(object);
					if (indexableColumn.getIndexableColumn() != null) {
						continue;
					}
					handleColumn(indexableColumn, document);
				}
				// Handle all the columns that rely on another column, like the attachment
				// column that needs the name from the name column to get the content type
				// for the parser
				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					Indexable<?> indexable = children.get(i - 1);
					if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
						break;
					}
					IndexableColumn indexableColumn = (IndexableColumn) indexable;
					if (indexableColumn.getIndexableColumn() == null) {
						continue;
					}
					handleColumn(indexableColumn, document);
				}
				// Handle all the sub tables
				for (Indexable<?> indexable : children) {
					if (IndexableTable.class.isAssignableFrom(indexable.getClass())) {
						IndexableTable childIndexableTable = (IndexableTable) indexable;
						handleTable(indexContext, childIndexableTable, connection, document);
					}
				}
				// Add the document to the index if this is the primary table
				if (indexableTable.isPrimary()) {
					indexContext.getIndexWriter().addDocument(document);
				}
				// Move to the next row in the result set
				if (!resultSet.next()) {
					// If the table is not primary then we have exhausted the results
					if (indexableTable.isPrimary()) {
						// We need to see if there are any more results
						resultSet = getResultSet(indexContext, indexableTable, connection);
					} else {
						break;
					}
				}
			} while (true);
		} catch (Exception e) {
			logger.error("Exception indexing table : " + indexableTable, e);
		} finally {
			// Close the result set and the statement for this
			// table, could be the sub table of course
			Statement statement = null;
			try {
				if (resultSet != null) {
					statement = resultSet.getStatement();
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			DatabaseUtilities.close(resultSet);
			DatabaseUtilities.close(statement);
		}
		// Once we finish all the results in the primary table
		// then we can close the connection too
		if (indexableTable.isPrimary()) {
			DatabaseUtilities.closeAll(resultSet);
		}
	}

	protected synchronized ResultSet getResultSet(IndexContext indexContext, IndexableTable indexableTable, Connection connection)
			throws Exception {
		try {
			long idNumber = 0;

			if (indexableTable.isPrimary()) {
				long minId = getMinId(indexableTable, connection);
				if (idNumber < minId) {
					idNumber = minId;
				}
				idNumber = indexContext.getIdNumber();
				indexContext.setIdNumber(idNumber + indexContext.getBatchSize());
			}

			String sql = buildSql(indexContext, indexableTable, idNumber);
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			setParameters(indexableTable, preparedStatement);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (!resultSet.next()) {
				if (indexableTable.isPrimary()) {
					long maxId = getMaxId(indexableTable, connection);
					DatabaseUtilities.close(resultSet);
					DatabaseUtilities.close(preparedStatement);
					if (idNumber > maxId) {
						return null;
					}
					return getResultSet(indexContext, indexableTable, connection);
				}
				return null;
			}
			return resultSet;
		} finally {
			notifyAll();
		}
	}

	protected synchronized void setParameters(IndexableTable indexableTable, PreparedStatement preparedStatement) {
		try {
			List<Indexable<?>> children = indexableTable.getChildren();
			for (Indexable<?> child : children) {
				if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
					IndexableColumn indexableColumn = (IndexableColumn) child;
					if (indexableColumn.getForeignKey() != null) {
						IndexableColumn foreignKey = indexableColumn.getForeignKey();
						try {
							Object parameter = foreignKey.getObject();
							// logger.debug("Parameter : " + parameter);
							preparedStatement.setObject(1, parameter);
						} catch (SQLException e) {
							logger.error("Exception setting the parameters : " + indexableTable + ", " + indexableTable.getChildren(), e);
						}
					}
				}
			}
		} finally {
			notifyAll();
		}
	}

	protected synchronized String buildSql(IndexContext indexContext, IndexableTable indexableTable, long idNumber) throws Exception {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("select ");
			List<Indexable<?>> children = indexableTable.getChildren();
			boolean first = Boolean.TRUE;
			for (Indexable<?> child : children) {
				if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
					IndexableColumn indexableColumn = (IndexableColumn) child;
					if (!first) {
						builder.append(", ");
					} else {
						first = Boolean.FALSE;
					}
					builder.append(indexableTable.getName());
					builder.append(".");
					builder.append(indexableColumn.getName());
				}
			}
			builder.append(" from ");
			builder.append(indexableTable.getName());

			if (indexableTable.getPredicate() != null) {
				builder.append(" ");
				builder.append(indexableTable.getPredicate());
			}

			if (!indexableTable.isPrimary()) {
				for (Indexable<?> child : children) {
					if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
						IndexableColumn indexableColumn = (IndexableColumn) child;
						if (indexableColumn.getForeignKey() != null) {
							if (indexableTable.getPredicate() == null) {
								builder.append(" where ");
							} else {
								builder.append(" and ");
							}
							builder.append(indexableTable.getName());
							builder.append(".");
							builder.append(indexableColumn.getName());
							builder.append(" = ");
							builder.append("?");
							break;
						}
					}
				}
			}

			if (indexableTable.isPrimary()) {
				if (indexableTable.getPredicate() == null) {
					builder.append(" where ");
				} else {
					builder.append(" and ");
				}
				String idColumnName = getIdColumn(indexableTable.getChildren()).getName();
				builder.append(idColumnName);
				builder.append(" > ");
				builder.append(idNumber);
				builder.append(" and ");
				builder.append(idColumnName);
				builder.append(" <= ");
				builder.append(idNumber + indexContext.getBatchSize());
				logger.info("Sql : " + builder.toString());
			}

			String sql = builder.toString();
			// logger.info("Sql : " + sql);
			return sql;
		} finally {
			notifyAll();
		}
	}

	protected synchronized long getMaxId(IndexableTable indexableTable, Connection connection) throws Exception {
		try {
			IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());

			long maxId = 0;
			Statement statement = null;
			ResultSet resultSet = null;

			try {
				StringBuilder builder = new StringBuilder("select max(");
				builder.append(idColumn.getName());
				builder.append(") from ");
				builder.append(indexableTable.getName());
				statement = connection.createStatement();
				resultSet = statement.executeQuery(builder.toString());
				if (resultSet.next()) {
					maxId = resultSet.getLong(1);
				}
			} finally {
				DatabaseUtilities.close(resultSet);
				DatabaseUtilities.close(statement);
			}

			// logger.debug("Max id : " + maxId);
			return maxId;
		} finally {
			notifyAll();
		}
	}

	protected synchronized long getMinId(IndexableTable indexableTable, Connection connection) throws Exception {
		try {
			IndexableColumn idColumn = getIdColumn(indexableTable.getChildren());

			long minId = 0;
			Statement statement = null;
			ResultSet resultSet = null;

			try {
				StringBuilder builder = new StringBuilder("select min(");
				builder.append(idColumn.getName());
				builder.append(") from ");
				builder.append(indexableTable.getName());
				statement = connection.createStatement();
				resultSet = statement.executeQuery(builder.toString());
				if (resultSet.next()) {
					minId = resultSet.getLong(1);
				}
			} finally {
				DatabaseUtilities.close(resultSet);
				DatabaseUtilities.close(statement);
			}

			// logger.debug("Max id : " + maxId);
			return minId;
		} finally {
			notifyAll();
		}
	}

	protected IndexableColumn getIdColumn(List<Indexable<?>> indexableColumns) {
		for (Indexable<?> indexable : indexableColumns) {
			if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				continue;
			}
			IndexableColumn indexableColumn = (IndexableColumn) indexable;
			if (indexableColumn.isIdColumn()) {
				return indexableColumn;
			}
		}
		logger.warn("No id column defined for table : " + indexableColumns);
		return null;
	}

	protected void handleColumn(IndexableColumn indexable, Document document) {
		try {
			Object content = contentProvider.getContent(indexable);
			if (content == null) {
				return;
			}
			String fieldName = indexable.getFieldName() != null ? indexable.getFieldName() : indexable.getName();
			Store store = indexable.isStored() ? Store.YES : Store.NO;
			Index analyzed = indexable.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
			TermVector termVector = indexable.isVectored() ? TermVector.YES : TermVector.NO;

			String mimeType = null;
			if (indexable.getIndexableColumn() != null) {
				if (indexable.getIndexableColumn().getObject() != null) {
					mimeType = indexable.getIndexableColumn().getObject().toString();
					// logger.debug("Got mime type : " + mimeType);
				}
			}

			byte[] bytes = new byte[1024];
			InputStream inputStream = null;

			if (String.class.isAssignableFrom(content.getClass())) {
				bytes = ((String) content).getBytes(IConstants.ENCODING);
				inputStream = new ByteArrayInputStream(bytes);
			} else if (InputStream.class.isAssignableFrom(content.getClass())) {
				inputStream = (InputStream) content;
			}

			if (inputStream.markSupported()) {
				inputStream.mark(bytes.length);
				inputStream.read(bytes);
				inputStream.reset();
			}

			// logger.debug("Bytes : " + new String(bytes, IConstants.ENCODING));
			IParser parser = ParserProvider.getParser(mimeType, bytes);
			OutputStream parsedOutputStream = parser.parse(inputStream);
			// logger.debug("After parse : " + parsedOutputStream);

			if (ByteArrayOutputStream.class.isAssignableFrom(parsedOutputStream.getClass())) {
				String fieldContent = parsedOutputStream.toString();
				IndexManager.addStringField(fieldName, fieldContent, document, store, analyzed, termVector);
			} else if (FileOutputStream.class.isAssignableFrom(parsedOutputStream.getClass())) {
				Reader reader = new InputStreamReader(inputStream);
				IndexManager.addReaderField(fieldName, document, store, termVector, reader);
			} else {
				logger.error("Type not supported from the parser : " + content.getClass().getName());
			}
		} catch (Exception e) {
			logger.error("Exception accessing the column content : ", e);
		}
	}

	protected void setIdField(List<Indexable<?>> children, IndexableTable indexableTable, Document document, ResultSet resultSet)
			throws Exception {
		IndexableColumn idColumn = getIdColumn(children);
		StringBuilder builder = new StringBuilder();
		builder.append(indexableTable.getName());
		builder.append(".");
		builder.append(idColumn.getName());
		builder.append(".");
		builder.append(resultSet.getObject(idColumn.getName()));

		String id = builder.toString();
		IndexManager.addStringField(IConstants.ID, id, document, Store.YES, Index.ANALYZED, TermVector.YES);
	}

	protected void setColumnTypes(List<Indexable<?>> children, ResultSet resultSet) throws Exception {
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			Indexable<?> indexable = children.get(i - 1);
			// Tables are at the end of the list so once we
			// get to the tables then we will exit the loop
			if (!IndexableColumn.class.isAssignableFrom(indexable.getClass())) {
				continue;
			}
			int columnType = resultSetMetaData.getColumnType(i);
			IndexableColumn indexableColumn = (IndexableColumn) indexable;
			indexableColumn.setColumnType(columnType);
		}
	}

}