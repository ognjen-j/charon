package ognjenj.charon.web.model.forms;

import java.io.Serializable;

public class CertsForm implements Serializable {
	private boolean showActive = true;
	private boolean showRevoked = false;
	private boolean showExpired = false;

	public boolean isShowActive() {
		return showActive;
	}

	public void setShowActive(boolean showActive) {
		this.showActive = showActive;
	}

	public boolean isShowRevoked() {
		return showRevoked;
	}

	public void setShowRevoked(boolean showRevoked) {
		this.showRevoked = showRevoked;
	}

	public boolean isShowExpired() {
		return showExpired;
	}

	public void setShowExpired(boolean showExpired) {
		this.showExpired = showExpired;
	}
}
