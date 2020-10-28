package data;

import java.util.Date;

public class Reservation {

	private String client_name;
	private Date start_date;
	private Date end_date;
	
	public String getClientName() {
		return this.client_name;
	}
	
	public Date getStartDate() {
		return this.start_date;
	}
	
	public Date getEndDate() {
		return this.end_date;
	}
	
	public void setClientName(String clientName) {
		this.client_name = clientName;
	}
	
	public void setStartDate(Date startDate) {
		this.start_date = startDate;
	}
	
	public void setEndDate(Date endDate) {
		this.end_date = endDate;
	}
	
	@Override
	public String toString() {
		return "Reservation [client_name=" + client_name + ", start_date=" + start_date + ", end_date=" + end_date + "]";
	}
}
