package ognjenj.charon.web.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

import ognjenj.charon.web.config.ConfigurationStore;
import ognjenj.charon.web.model.radius.RadAcct;
import ognjenj.charon.web.util.ByteSizesConverter;

public class Connection implements Serializable {
	private String username;
	private String framedIpAddress;
	private String callingStationId;
	private Long inputOctets;
	private Long outputOctets;
	private String inputOctetsReadable;
	private String outputOctetsReadable;
	private boolean stillActive = false;
	private LocalDateTime startTime;
	private LocalDateTime stopTime;

	public Connection() {
	}

	public Connection(RadAcct radAcct) {
		ConfigurationStore store = ConfigurationStore.getInstance();
		this.username = radAcct.getUsername();
		this.framedIpAddress = radAcct.getFramedIpAddress();
		this.callingStationId = radAcct.getCallingStationId();
		this.inputOctets = radAcct.getAcctInputOctets();
		this.outputOctets = radAcct.getAcctOutputOctets();
		this.inputOctetsReadable = ByteSizesConverter.bytesToHumanReadable(this.inputOctets);
		this.outputOctetsReadable = ByteSizesConverter.bytesToHumanReadable(this.outputOctets);
		this.startTime = radAcct.getAcctStartTime().toInstant().atZone(ZoneId.of(store.getDbTimezone()))
				.toLocalDateTime();
		if (radAcct.getAcctStopTime() != null) {
			this.stopTime = radAcct.getAcctStopTime().toInstant().atZone(ZoneId.of(store.getDbTimezone()))
					.toLocalDateTime();
		}
		this.stillActive = this.stopTime == null;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFramedIpAddress() {
		return framedIpAddress;
	}

	public void setFramedIpAddress(String framedIpAddress) {
		this.framedIpAddress = framedIpAddress;
	}

	public String getCallingStationId() {
		return callingStationId;
	}

	public void setCallingStationId(String callingStationId) {
		this.callingStationId = callingStationId;
	}

	public Long getInputOctets() {
		return inputOctets;
	}

	public void setInputOctets(Long inputOctets) {
		this.inputOctets = inputOctets;
	}

	public Long getOutputOctets() {
		return outputOctets;
	}

	public void setOutputOctets(Long outputOctets) {
		this.outputOctets = outputOctets;
	}

	public String getInputOctetsReadable() {
		return inputOctetsReadable;
	}

	public void setInputOctetsReadable(String inputOctetsReadable) {
		this.inputOctetsReadable = inputOctetsReadable;
	}

	public String getOutputOctetsReadable() {
		return outputOctetsReadable;
	}

	public void setOutputOctetsReadable(String outputOctetsReadable) {
		this.outputOctetsReadable = outputOctetsReadable;
	}

	public boolean isStillActive() {
		return stillActive;
	}

	public void setStillActive(boolean stillActive) {
		this.stillActive = stillActive;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getStopTime() {
		return stopTime;
	}

	public void setStopTime(LocalDateTime stopTime) {
		this.stopTime = stopTime;
	}
}
