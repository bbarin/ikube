package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * @author Michael Couck
 * @since 08.03.2011
 * @version 01.00
 */
@Entity()
@SuppressWarnings("serial")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class IndexableFileSystemLog extends Indexable<IndexableFileSystemLog> {

	private String path;
	private String fileFieldName;
	private String pathFieldName;
	private String lineFieldName;
	private String contentFieldName;

	public String getPath() {
		return path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getFileFieldName() {
		return fileFieldName;
	}

	public void setFileFieldName(String fileFieldName) {
		this.fileFieldName = fileFieldName;
	}

	public String getPathFieldName() {
		return pathFieldName;
	}

	public void setPathFieldName(String pathFieldName) {
		this.pathFieldName = pathFieldName;
	}

	public String getLineFieldName() {
		return lineFieldName;
	}

	public void setLineFieldName(String lineFieldName) {
		this.lineFieldName = lineFieldName;
	}

	public String getContentFieldName() {
		return contentFieldName;
	}

	public void setContentFieldName(String contentFieldName) {
		this.contentFieldName = contentFieldName;
	}

}