package ognjenj.charon.web.model.forms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

public class NewUserForm implements Serializable {
	private String username;
	@DateTimeFormat(pattern = "dd.MM.yyyy")
	private Date expiration;
	private boolean existingUser = false;
	private boolean regenerateCertificate = true;
	private String routeString = "";
	private List<String> assignedRoles = new ArrayList<>();
	private boolean routeAllTraffic = true;
	private String archivePassword;
	private String duration;
	private boolean assignStaticIpAddress = false;
	private String staticIpAddress;

	public boolean isAssignStaticIpAddress() {
		return assignStaticIpAddress;
	}

	public void setAssignStaticIpAddress(boolean assignStaticIpAddress) {
		this.assignStaticIpAddress = assignStaticIpAddress;
	}

	public String getStaticIpAddress() {
		return staticIpAddress;
	}

	public void setStaticIpAddress(String staticIpAddress) {
		this.staticIpAddress = staticIpAddress;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getArchivePassword() {
		return archivePassword;
	}

	public void setArchivePassword(String archivePassword) {
		this.archivePassword = archivePassword;
	}

	public String getRouteString() {
		return routeString;
	}

	public void setRouteString(String routeString) {
		this.routeString = routeString;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}

	public boolean isExistingUser() {
		return existingUser;
	}

	public void setExistingUser(boolean existingUser) {
		this.existingUser = existingUser;
	}

	public boolean isRegenerateCertificate() {
		return regenerateCertificate;
	}

	public void setRegenerateCertificate(boolean regenerateCertificate) {
		this.regenerateCertificate = regenerateCertificate;
	}

	public List<String> getAssignedRoles() {
		return assignedRoles;
	}

	public void setAssignedRoles(List<String> assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

	public boolean isRouteAllTraffic() {
		return routeAllTraffic;
	}

	public void setRouteAllTraffic(boolean routeAllTraffic) {
		this.routeAllTraffic = routeAllTraffic;
	}

}
