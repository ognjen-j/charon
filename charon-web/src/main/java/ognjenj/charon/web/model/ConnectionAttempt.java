package ognjenj.charon.web.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

import ognjenj.charon.web.config.ConfigurationStore;
import ognjenj.charon.web.model.radius.RadPostAuth;

public class ConnectionAttempt implements Serializable {
	private String username;
	private String reply;
	private LocalDateTime authTime;

	public ConnectionAttempt() {
	}

	public ConnectionAttempt(RadPostAuth radPostAuth) {
		ConfigurationStore store = ConfigurationStore.getInstance();
		this.username = radPostAuth.getUsername();
		this.reply = radPostAuth.getReply();
		this.authTime = radPostAuth.getAuthTime().toInstant().atZone(ZoneId.of(store.getDbTimezone()))
				.toLocalDateTime();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public LocalDateTime getAuthTime() {
		return authTime;
	}

	public void setAuthTime(LocalDateTime authTime) {
		this.authTime = authTime;
	}
}
