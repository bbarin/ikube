package ikube.model;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexableInternet extends Indexable<IndexableInternet> {

	@Transient
	private transient String currentUrl;
	@Transient
	private transient InputStream currentInputStream;

	private URI uri;
	private String url;

	@Field(field = true)
	private String titleFieldName;
	@Field(field = true)
	private String idFieldName;
	@Field(field = true)
	private String contentFieldName;

	private String excludedPattern;

	public URI getUri() {
		if (uri == null && getUrl() != null) {
			try {
				uri = new URI(getUrl());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitleFieldName() {
		return titleFieldName;
	}

	public void setTitleFieldName(String titleFieldName) {
		this.titleFieldName = titleFieldName;
	}

	public String getIdFieldName() {
		return idFieldName;
	}

	public void setIdFieldName(String idFieldName) {
		this.idFieldName = idFieldName;
	}

	public String getContentFieldName() {
		return contentFieldName;
	}

	public void setContentFieldName(String contentFieldName) {
		this.contentFieldName = contentFieldName;
	}

	public String getExcludedPattern() {
		return excludedPattern;
	}

	public void setExcludedPattern(String excludedPatterns) {
		this.excludedPattern = excludedPatterns;
	}

	@Transient
	public String getCurrentUrl() {
		return currentUrl;
	}

	public void setCurrentUrl(String currentUrl) {
		this.currentUrl = currentUrl;
	}

	@Transient
	public InputStream getCurrentInputStream() {
		return currentInputStream;
	}

	public void setCurrentInputStream(InputStream currentInputStream) {
		this.currentInputStream = currentInputStream;
	}

}
