package data;

import java.util.Date;

public class Reservation {

	private String _rid;
    private String id;
	private String client_name;
	private Date start_date;
	private Date end_date;
	private String periodId;
	
	// BUSCAR PERIODS DENTRO DO CALENDAR DENTRO DO STARTDATE E ENDDATE
	// BUSCAR RESERVATIONS DENTRO DO PERIOD EM QUE STARTDATE E ENDDATE NAO OCUPADOS
	
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
