package ognjenj.charon.web.model.forms;

import java.io.Serializable;

public class NewCertForm implements Serializable {
	private String commonName;
	private String duration;
	private boolean extendExisting = false;
	private String downloadPassword;

	public String getDownloadPassword() {
		return downloadPassword;
	}

	public void setDownloadPassword(String downloadPassword) {
		this.downloadPassword = downloadPassword;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public boolean isExtendExisting() {
		return extendExisting;
	}

	public void setExtendExisting(boolean extendExisting) {
		this.extendExisting = extendExisting;
	}
}
