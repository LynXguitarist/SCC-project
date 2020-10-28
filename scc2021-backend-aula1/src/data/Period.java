package data;

import java.util.Date;

public class Period {

	private Date start_date;
	private Date end_date;
	
	public Date getStartDate() {
		return this.start_date;
	}
	
	public Date getEndDate() {
		return this.end_date;
	}
	
	public void setStartDate(Date startDate) {
		this.start_date = startDate;
	}
	
	public void setEndDate(Date endDate) {
		this.end_date = endDate;
	}
	
	@Override
	public String toString() {
		return "Period [start_date=" + start_date + ", end_date=" + end_date + "]";
	}
	
}
