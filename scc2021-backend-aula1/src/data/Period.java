package data;

import java.time.LocalDateTime;

public class Period {

	private String _rid;
	private String id;
	private String name;
	//private String startDate;
	//private String endDate;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private String calendarId;
	private Integer ttl;

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

	public LocalDateTime getStartDate() {
		return this.startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return this.endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public Integer getTtl() {
		return ttl;
	}

	public void setTtl(Integer ttl) {
		this.ttl = ttl;
	}

	@Override
	public String toString() {
		return "Period [_rid=" + _rid + ", id=" + id + ", name=" + name + ", start_date=" + startDate + ", end_date="
				+ endDate + ", calendarId=" + calendarId + "]";
	}

}
