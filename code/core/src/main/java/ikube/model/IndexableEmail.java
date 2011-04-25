package ikube.model;

import javax.persistence.Entity;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexableEmail extends Indexable<IndexableEmail> {

	@Field()
	private String idField;
	@Field()
	private String titleField;
	@Field()
	private String contentField;

	private boolean stored = Boolean.FALSE;
	private boolean analyzed = Boolean.TRUE;
	private boolean vectored = Boolean.TRUE;

	private String mailHost;
	private String username;
	private String password;
	private String port;
	private String protocol;
	private boolean secureSocketLayer;

	public String getIdField() {
		return idField;
	}

	public void setIdField(final String idField) {
		this.idField = idField;
	}

	public String getTitleField() {
		return titleField;
	}

	public void setTitleField(final String titleField) {
		this.titleField = titleField;
	}

	public String getContentField() {
		return contentField;
	}

	public void setContentField(final String contentField) {
		this.contentField = contentField;
	}

	public boolean isStored() {
		return stored;
	}

	public void setStored(final boolean stored) {
		this.stored = stored;
	}

	public boolean isAnalyzed() {
		return analyzed;
	}

	public void setAnalyzed(final boolean analyzed) {
		this.analyzed = analyzed;
	}

	public boolean isVectored() {
		return vectored;
	}

	public void setVectored(final boolean vectored) {
		this.vectored = vectored;
	}

	public String getMailHost() {
		return mailHost;
	}

	public void setMailHost(final String mailHost) {
		this.mailHost = mailHost;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getPort() {
		return port;
	}

	public void setPort(final String port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(final String protocol) {
		this.protocol = protocol;
	}

	public boolean isSecureSocketLayer() {
		return secureSocketLayer;
	}

	public void setSecureSocketLayer(final boolean secureSocketLayer) {
		this.secureSocketLayer = secureSocketLayer;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
