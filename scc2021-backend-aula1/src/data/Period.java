package data;

import java.util.Date;

public class Period {

	private String _rid;
    private String id;
	private Date startDate;
	private Date endDate;
	private String calendarId; //worth it?

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

	public void setCalendarId(String calendarId) {
		this.calendarId = id;
	}
	
	public String getCalendarId() {
		return this.calendarId;
	}
	
	@Override
	public String toString() {
		return "Period [_rid=" + _rid + ", id=" + id + ", start_date=" + startDate + ", end_date=" + endDate + ", calendar_id=" + calendarId +"]";
	}

}
