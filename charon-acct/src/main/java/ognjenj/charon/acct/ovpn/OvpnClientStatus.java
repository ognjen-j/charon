package ognjenj.charon.acct.ovpn;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OvpnClientStatus implements Serializable {
	private final String username;
	private final String callingStationId;
	private final long bytesSent;
	private final long bytesReceived;
	private final LocalDateTime sessionStartTime;

	public OvpnClientStatus(String username, String callingStationId, long bytesReceived, long bytesSent,
			LocalDateTime sessionStartTime) {
		this.username = username;
		this.callingStationId = callingStationId;
		this.bytesSent = bytesSent;
		this.bytesReceived = bytesReceived;
		this.sessionStartTime = sessionStartTime;
	}

	public String getUsername() {
		return username;
	}

	public String getCallingStationId() {
		return callingStationId;
	}

	public long getBytesSent() {
		return bytesSent;
	}

	public long getBytesReceived() {
		return bytesReceived;
	}

	public LocalDateTime getSessionStartTime() {
		return sessionStartTime;
	}
}
