package ognjenj.charon.web.model.radius;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "radusergroup")
@IdClass(RadUserGroup.RadUserGroupId.class)
public class RadUserGroup implements Serializable {
	public static final String USER_GROUP = "users";
	@Id
	@Column
	private final int priority = 1;
	@Id
	@Column
	private String username;
	@Id
	@Column
	private String groupname;

	public RadUserGroup() {

	}

	public RadUserGroup(String username) {
		this.username = username;
		this.groupname = USER_GROUP;
	}
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public int getPriority() {
		return priority;
	}

	public static class RadUserGroupId implements Serializable {
		int priority;
		String username;
		String groupname;
	}
}
