package ognjenj.charon.web.model.radius;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "radreply")
public class RadReply implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String username;
	@Column(nullable = false)
	private String attribute;
	@Column(name = "op", nullable = false)
	private String operator;
	@Column(nullable = false)
	private String value;

	public RadReply() {

	}

	public RadReply(String username, String attribute, String operator, String value) {
		this.username = username;
		this.attribute = attribute;
		this.operator = operator;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
