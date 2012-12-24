package ikube.model;

import ikube.index.IndexManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;

/**
 * This is the context for a single index. It has the properties that define the index like what it is going to index, i.e. the databases,
 * intranets etc., and properties relating to the Lucene index. This object acts a like the command in the 'Command Pattern' as in this
 * context is passed to handlers that will perform certain logic based on the properties of this context.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@SuppressWarnings({ "serial", "deprecation" })
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({ @NamedQuery(name = IndexContext.FIND_BY_NAME, query = IndexContext.FIND_BY_NAME) })
public class IndexContext<T> extends Indexable<T> implements Comparable<IndexContext<?>> {

	public static final String FIND_BY_NAME = "select i from IndexContext as i where i.name = :name";

	private static final transient Logger LOGGER = Logger.getLogger(IndexContext.class);

	@Transient
	private boolean open;
	@Transient
	private long numDocs;
	@Transient
	private long indexSize;
	@Transient
	private Date latestIndexTimestamp;

	/** Can be null if there are no indexes running. */
	@Transient
	private transient volatile IndexWriter indexWriter;
	/** Can be null if there is no index created. */
	@Transient
	private transient volatile MultiSearcher multiSearcher;
	/** This analyzer will be used to index the data, and indeed to do the searching. */
	@Transient
	private transient volatile Analyzer analyzer;

	/** The maximum age of the index defined in minutes. */
	@Column
	@Min(value = 1)
	@Max(value = Integer.MAX_VALUE)
	@Attribute(field = false, description = "This is the maximum age that the index can become before it is re-indexed")
	private long maxAge;
	/** The delay between documents being indexed, slows the indexing down. */
	@Column
	@Min(value = 0)
	@Max(value = 60000)
	@Attribute(field = false, description = "This is the throttle in mili seconds that will slow down the indexing")
	private long throttle = 0;

	/** Lucene properties. */
	@Column
	@Min(value = 10)
	@Max(value = 1000000)
	@Attribute(field = false, description = "The number of documents to keep in the segments before they are merged to the main file during indexing")
	private int mergeFactor;
	@Column
	@Min(value = 10)
	@Max(value = 1000000)
	@Attribute(field = false, description = "The number of documents to keep in memory before writing to the file")
	private int bufferedDocs;
	@Column
	@Min(value = 1)
	@Max(value = 1000)
	@Attribute(field = false, description = "The size of the memory Lucene can occupy before the documents are written to the file")
	private double bufferSize;
	@Column
	@Min(value = 10)
	@Max(value = 1000000)
	@Attribute(field = false, description = "The maximum length of a field in the Lucene index")
	private int maxFieldLength;
	@Column
	@Attribute(field = false, description = "Whether this index should be in a compound file format")
	private boolean compoundFile;

	/** Jdbc properties. */
	@Column
	@Min(value = 1)
	@Max(value = 1000000)
	@Attribute(field = false, description = "The batch size of the result set for database indexing")
	private int batchSize;
	/** Internet properties. */
	@Column
	@Min(value = 1)
	@Max(value = 1000000)
	@Attribute(field = false, description = "The batch size of urls for the crawler")
	private int internetBatchSize;

	/** The maximum length of a document that can be read. */
	@Column
	@Min(value = 1)
	@Max(value = 1000000000)
	@Attribute(field = false, description = "The maximum read length for a document")
	private long maxReadLength;
	/** The path to the index directory, either relative or absolute. */
	@Column
	@NotNull
	@Size(min = 2, max = 256)
	@Attribute(field = false, description = "The absolute or relative path to the directory where the index will be written")
	private String indexDirectoryPath;
	/** The path to the backup index directory, either relative or absolute. */
	@Column
	@NotNull
	@Size(min = 2, max = 256)
	@Attribute(field = false, description = "The absolute or relative path to the directory where the index will be backed up")
	private String indexDirectoryPathBackup;
	@Column
	@Attribute(field = false, description = "The is dynamically set by the logic to validate that there is disk space left on the drive where the index is")
	private long availableDiskSpace;
	@Column
	@Attribute(field = false, description = "This flag indicates whether the index is being generated currently")
	private boolean indexing;
	@Column
	@Attribute(field = false, description = "This flag indicates whether the index should be delta indexed, i.e. no new index just the changes n the resources")
	private boolean delta;

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "indexContext", fetch = FetchType.EAGER)
	private List<Snapshot> snapshots = new ArrayList<Snapshot>();

	public String getIndexName() {
		return super.getName();
	}

	public void setIndexName(final String indexName) {
		super.setName(indexName);
	}

	public long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(final long maxAge) {
		this.maxAge = maxAge;
	}

	public long getThrottle() {
		return throttle;
	}

	public void setThrottle(final long throttle) {
		this.throttle = throttle;
	}

	public boolean isCompoundFile() {
		return compoundFile;
	}

	public void setCompoundFile(final boolean compoundFile) {
		this.compoundFile = compoundFile;
	}

	public int getMaxFieldLength() {
		return maxFieldLength;
	}

	public void setMaxFieldLength(final int maxFieldLength) {
		this.maxFieldLength = maxFieldLength;
	}

	public int getBufferedDocs() {
		return bufferedDocs;
	}

	public void setBufferedDocs(final int bufferedDocs) {
		this.bufferedDocs = bufferedDocs;
	}

	public int getMergeFactor() {
		return mergeFactor;
	}

	public void setMergeFactor(final int mergeFactor) {
		this.mergeFactor = mergeFactor;
	}

	public double getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(final double bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}

	public int getInternetBatchSize() {
		return internetBatchSize;
	}

	public void setInternetBatchSize(final int internetBatchSize) {
		this.internetBatchSize = internetBatchSize;
	}

	public long getMaxReadLength() {
		return maxReadLength;
	}

	public void setMaxReadLength(final long maxReadLength) {
		this.maxReadLength = maxReadLength;
	}

	public String getIndexDirectoryPath() {
		return indexDirectoryPath;
	}

	public void setIndexDirectoryPath(final String indexDirectoryPath) {
		this.indexDirectoryPath = indexDirectoryPath;
	}

	public String getIndexDirectoryPathBackup() {
		return indexDirectoryPathBackup;
	}

	public void setIndexDirectoryPathBackup(final String indexDirectoryPathBackup) {
		this.indexDirectoryPathBackup = indexDirectoryPathBackup;
	}

	public List<Indexable<?>> getIndexables() {
		return super.getChildren();
	}

	public void setIndexables(final List<Indexable<?>> indexables) {
		super.setChildren(indexables);
	}

	public IndexWriter getIndexWriter() {
		return indexWriter;
	}

	public void setIndexWriter(final IndexWriter indexWriter) {
		setIndexing(indexWriter != null);
		this.indexWriter = indexWriter;
	}

	public MultiSearcher getMultiSearcher() {
		return multiSearcher;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(final boolean open) {
		this.open = open;
	}

	public long getNumDocs() {
		numDocs = IndexManager.getNumDocs(this);
		return numDocs;
	}

	public void setNumDocs(long numDocs) {
		this.numDocs = numDocs;
	}

	public long getIndexSize() {
		indexSize = IndexManager.getIndexSize(this);
		return indexSize;
	}

	public void setIndexSize(long indexSize) {
		this.indexSize = indexSize;
	}

	public Date getLatestIndexTimestamp() {
		latestIndexTimestamp = IndexManager.getLatestIndexDirectoryDate(this);
		return latestIndexTimestamp;
	}

	public void setLatestIndexTimestamp(Date latestIndexTimestamp) {
		this.latestIndexTimestamp = latestIndexTimestamp;
	}

	public void setMultiSearcher(final MultiSearcher multiSearcher) {
		setOpen(multiSearcher != null);
		// We'll close the current searcher if it is not already closed
		if (this.multiSearcher != null && !this.multiSearcher.equals(multiSearcher)) {
			try {
				LOGGER.info("Searcher not closed, will close now : " + this.multiSearcher);
				Searchable[] searchables = this.multiSearcher.getSearchables();
				if (searchables != null) {
					for (Searchable searchable : searchables) {
						try {
							searchable.close();
						} catch (Exception e) {
							LOGGER.error("Exception closing the searchable : ", e);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error("Exception closing the searcher : " + this.multiSearcher, e);
			}
		}
		this.multiSearcher = multiSearcher;
	}

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	public List<Snapshot> getSnapshots() {
		if (snapshots == null) {
			snapshots = new ArrayList<Snapshot>();
		}
		return snapshots;
	}

	public void setSnapshots(List<Snapshot> snapshots) {
		this.snapshots = snapshots;
	}

	public Snapshot getLastSnapshot() {
		if (snapshots == null || getSnapshots().size() == 0) {
			return null;
		}
		return getSnapshots().get(getSnapshots().size() - 1);
	}

	public long getAvailableDiskSpace() {
		return availableDiskSpace;
	}

	public void setAvailableDiskSpace(long availableDiskSpace) {
		this.availableDiskSpace = availableDiskSpace;
	}

	public boolean isIndexing() {
		setIndexing(getIndexWriter() != null);
		return indexing;
	}

	public void setIndexing(boolean indexing) {
		this.indexing = indexing;
	}

	public boolean isDelta() {
		return delta;
	}

	public void setDelta(boolean delta) {
		this.delta = delta;
	}

	@Override
	public int compareTo(final IndexContext<?> other) {
		return getIndexName().compareTo(other.getIndexName());
	}

}