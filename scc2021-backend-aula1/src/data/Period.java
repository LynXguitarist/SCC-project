package data;

import java.util.Date;

public class Period {

	private Date startDate;
	private Date endDate;

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return "Period [start_date=" + startDate + ", end_date=" + endDate + "]";
	}

}
