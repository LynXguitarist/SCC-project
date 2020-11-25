package data;

import java.util.Date;

public class Reservation {

	private String _rid;
    private String id;
	private String client_name;
	private String start_date;
	private String end_date;
	private String periodId;
	
	public String get_rid() {
        return _rid;
    }
    public void set_rid(String _rid) {
        this._rid = _rid;
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
	public String getClientName() {
		return this.client_name;
	}
	
	public String getStartDate() {
		return this.start_date;
	}
	
	public String getEndDate() {
		return this.end_date;
	}
	
	public void setClientName(String clientName) {
		this.client_name = clientName;
	}
	
	public void setStartDate(String startDate) {
		this.start_date = startDate;
	}
	
	public void setEndDate(String endDate) {
		this.end_date = endDate;
	}
	
	public void setPeriodId(String periodId) {
		this.periodId = periodId;
	}
	
	public String getPeriodId() {
		return this.periodId;
	}
	
	@Override
	public String toString() {
		return "Reservation [_rid=" + _rid + ", id=" + id + ", client_name=" + client_name + ", start_date=" + start_date
				+ ", end_date=" + end_date + ", period_id=" + periodId + "]";
	}
}
