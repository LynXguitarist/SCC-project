package data;

import java.util.Date;
import java.util.List;


public class Period {

	private String _rid;
    private String id;
    private String name;
	private String startDate;
	private String endDate;
	//tirar lista
	private List<String> reservations;// list reservations' ids
	private String calendarId;

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
    
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    
	public String getStartDate() {
		return this.startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return this.endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	public void addReservation(String reservationId) {
		this.reservations.add(reservationId);
	}
	
	
	public void cancelReservation(String reservationId) {
		for (String resId: this.reservations) {
			if(resId.equals(reservationId)) {
				this.reservations.remove(resId);
			}
		}
	}
	
	public String getCalendarId() {
		return calendarId;
	}
	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}
	
	@Override
	public String toString() {
		return "Period [_rid=" + _rid + ", id=" + id + ", name=" + name +", start_date=" + startDate + ", end_date=" + endDate +
				", reservations=" + reservations.toString() + "]";
	}

}
