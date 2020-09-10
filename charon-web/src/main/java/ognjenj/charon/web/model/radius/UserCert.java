package ognjenj.charon.web.model.radius;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.*;

@Entity
@Table(name = "user_cert")
public class UserCert implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cert_id")
	private Long certId;
	@Column(name = "common_name")
	private String commonName;
	@Column(name = "cert_serial")
	private String certSerial;
	@Column
	private String dn;
	@Column(name = "issue_date")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date issueDate;
	@Column(name = "expiration_date")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date expirationDate;
	@Column(name = "is_downloaded")
	private boolean downloaded;
	@Column(name = "is_revoked")
	private boolean revoked;
	@Column(name = "revocation_date")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date revocationDate;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserCert userCert = (UserCert) o;
		return certSerial.equals(userCert.certSerial);
	}

	@Override
	public int hashCode() {
		return Objects.hash(certSerial);
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public Date getRevocationDate() {
		return revocationDate;
	}

	public void setRevocationDate(Date revocationDate) {
		this.revocationDate = revocationDate;
	}

	public Long getCertId() {
		return certId;
	}

	public void setCertId(Long certId) {
		this.certId = certId;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getCertSerial() {
		return certSerial;
	}

	public void setCertSerial(String certSerial) {
		this.certSerial = certSerial;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public Date getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(Date issueDate) {
		this.issueDate = issueDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}
}
