package ognjenj.charon.web.model.radius;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "radacct")
public class RadAcct implements Serializable {
	@Id
	@Column(name = "radacctid")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "acctsessionid")
	private String sessionId;
	private String username;
	@Column(name = "nasipaddress")
	private String nasIpAddress;
	@Column(name = "nasportid")
	private String nasPortId;
	@Column(name = "acctstarttime")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date acctStartTime;
	@Column(name = "acctstoptime")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date acctStopTime;
	@Column(name = "acctsessiontime")
	private Long acctSessionTime;
	@Column(name = "callingstationid")
	private String callingStationId;
	@Column(name = "acctinputoctets")
	private Long acctInputOctets;
	@Column(name = "acctoutputoctets")
	private Long acctOutputOctets;
	@Column(name = "acctterminatecause")
	private String acctTerminateCause;
	@Column(name = "framedipaddress")
	private String framedIpAddress;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNasIpAddress() {
		return nasIpAddress;
	}

	public void setNasIpAddress(String nasIpAddress) {
		this.nasIpAddress = nasIpAddress;
	}

	public String getNasPortId() {
		return nasPortId;
	}

	public void setNasPortId(String nasPortId) {
		this.nasPortId = nasPortId;
	}

	public Date getAcctStartTime() {
		return acctStartTime;
	}

	public void setAcctStartTime(Date acctStartTime) {
		this.acctStartTime = acctStartTime;
	}

	public Date getAcctStopTime() {
		return acctStopTime;
	}

	public void setAcctStopTime(Date acctStopTime) {
		this.acctStopTime = acctStopTime;
	}

	public Long getAcctSessionTime() {
		return acctSessionTime;
	}

	public void setAcctSessionTime(Long acctSessionTime) {
		this.acctSessionTime = acctSessionTime;
	}

	public String getCallingStationId() {
		return callingStationId;
	}

	public void setCallingStationId(String callingStationId) {
		this.callingStationId = callingStationId;
	}

	public Long getAcctInputOctets() {
		return acctInputOctets;
	}

	public void setAcctInputOctets(Long acctInputOctets) {
		this.acctInputOctets = acctInputOctets;
	}

	public Long getAcctOutputOctets() {
		return acctOutputOctets;
	}

	public void setAcctOutputOctets(Long acctOutputOctets) {
		this.acctOutputOctets = acctOutputOctets;
	}

	public String getAcctTerminateCause() {
		return acctTerminateCause;
	}

	public void setAcctTerminateCause(String acctTerminateCause) {
		this.acctTerminateCause = acctTerminateCause;
	}

	public String getFramedIpAddress() {
		return framedIpAddress;
	}

	public void setFramedIpAddress(String framedIpAddress) {
		this.framedIpAddress = framedIpAddress;
	}
}
