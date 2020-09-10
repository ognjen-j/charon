package ognjenj.charon.web.model.forms;

import java.io.Serializable;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class FilterForm implements Serializable {
	@DateTimeFormat(pattern = "dd.MM.yyyy")
	private Date startDate;
	@DateTimeFormat(pattern = "dd.MM.yyyy")
	private Date endDate;

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
