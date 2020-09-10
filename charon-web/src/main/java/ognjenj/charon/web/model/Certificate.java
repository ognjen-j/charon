package ognjenj.charon.web.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

import ognjenj.charon.web.config.ConfigurationStore;
import ognjenj.charon.web.model.radius.UserCert;

public class Certificate implements Serializable {
	private Long certificateId;
	private String commonName;
	private String distinguishedName;
	private String certificateSerial;
	private String keyId;
	private LocalDateTime issueDate;
	private LocalDateTime expirationDate;
	private boolean downloaded;
	private boolean revoked;
	private boolean expired = false;
	private LocalDateTime revocationDate;
	public Certificate() {
	}
	public Certificate(UserCert cert) {
		ConfigurationStore store = ConfigurationStore.getInstance();
		this.certificateId = cert.getCertId();
		this.certificateSerial = cert.getCertSerial();
		this.commonName = cert.getCommonName();
		this.distinguishedName = cert.getDn();
		this.downloaded = cert.isDownloaded();
		if (cert.getExpirationDate() != null) {
			this.expirationDate = cert.getExpirationDate().toInstant().atZone(ZoneId.of(store.getDbTimezone()))
					.toLocalDateTime();
			this.expired = this.expirationDate.isBefore(LocalDateTime.now());
		}
		this.issueDate = cert.getIssueDate().toInstant().atZone(ZoneId.of(store.getDbTimezone())).toLocalDateTime();
		if (cert.getRevocationDate() != null) {
			this.revocationDate = cert.getRevocationDate().toInstant().atZone(ZoneId.of(store.getDbTimezone()))
					.toLocalDateTime();
		}
		this.revoked = cert.isRevoked();
	}

	public String getDistinguishedName() {
		return distinguishedName;
	}

	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public Long getCertificateId() {
		return certificateId;
	}

	public void setCertificateId(Long certificateId) {
		this.certificateId = certificateId;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getCertificateSerial() {
		return certificateSerial;
	}

	public void setCertificateSerial(String certificateSerial) {
		this.certificateSerial = certificateSerial;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public LocalDateTime getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(LocalDateTime issueDate) {
		this.issueDate = issueDate;
	}

	public LocalDateTime getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(LocalDateTime expirationDate) {
		this.expirationDate = expirationDate;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public LocalDateTime getRevocationDate() {
		return revocationDate;
	}

	public void setRevocationDate(LocalDateTime revocationDate) {
		this.revocationDate = revocationDate;
	}
}
