package ognjenj.charon.acct.ovpn;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import inet.ipaddr.ipv4.IPv4Address;

public class ClientSession implements Serializable {
	private String username;
	private String ovpnInternalSessionId;
	private String radiusSessionId;
	private LocalDateTime sessionStartTime;
	private String callingStationId;
	private IPv4Address framedIpAddress;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ClientSession that = (ClientSession) o;
		return username.equals(that.username) && ovpnInternalSessionId.equals(that.ovpnInternalSessionId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, ovpnInternalSessionId);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOvpnInternalSessionId() {
		return ovpnInternalSessionId;
	}

	public void setOvpnInternalSessionId(String ovpnInternalSessionId) {
		this.ovpnInternalSessionId = ovpnInternalSessionId;
	}

	public String getRadiusSessionId() {
		return radiusSessionId;
	}

	public void setRadiusSessionId(String radiusSessionId) {
		this.radiusSessionId = radiusSessionId;
	}

	public LocalDateTime getSessionStartTime() {
		return sessionStartTime;
	}

	public void setSessionStartTime(LocalDateTime sessionStartTime) {
		this.sessionStartTime = sessionStartTime;
	}

	public String getCallingStationId() {
		return callingStationId;
	}

	public void setCallingStationId(String callingStationId) {
		this.callingStationId = callingStationId;
	}

	public IPv4Address getFramedIpAddress() {
		return framedIpAddress;
	}

	public void setFramedIpAddress(IPv4Address framedIpAddress) {
		this.framedIpAddress = framedIpAddress;
	}
}
