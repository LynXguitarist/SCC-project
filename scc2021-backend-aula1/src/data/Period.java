package data;

import java.util.Date;

public class Period {

	private String _rid;
    private String id;
	private Date startDate;
	private Date endDate;
	private Date availableStartDate;
	private Date availableEndDate;

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

	public void setAvStartDate(Date avStartDate) {
		this.availableStartDate = avStartDate;
	}
	
	public Date getAvStartDate() {
		return this.availableStartDate;
	}
	
	public void setAvEndDate(Date avEndDate) {
		this.availableEndDate = avEndDate;
	}
	
	public Date getAvEndDate() {
		return this.availableEndDate;
	}
	
	public void updateAvailablePeriod(Date resStartDate, Date resEndDate) { //doesn't allow reservations in the "middle"
		if(resEndDate.before(this.availableEndDate)) {                      //reserv need to start exactly at the availableStartDate 
			this.availableStartDate = resEndDate;                           //OR  end exactly at the availableEndDate
		} else if(resStartDate.after(this.availableStartDate)) {
			this.availableEndDate = resStartDate;
		}
	}
	
	public void cancelReservation(Date resStartDate, Date resEndDate) { //to be changed, prob doesn't work
		if(resStartDate.before(this.availableStartDate)) {
			this.availableStartDate = resStartDate;
		} else if(resEndDate.after(this.availableEndDate)) {
			this.availableEndDate = resEndDate;
		}
	}
	
	@Override
	public String toString() {
		return "Period [_rid=" + _rid + ", id=" + id + ", start_date=" + startDate + ", end_date=" + endDate +"]";
	}

}
