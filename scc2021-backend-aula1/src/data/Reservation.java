package data;

public class Reservation {

	private String _rid;
	private String id;
	private String clientName;
	private String startDate;
	private String endDate;
	private String periodId;
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

	public String getClientName() {
		return this.clientName;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public String getEndDate() {
		return this.endDate;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public void setPeriodId(String periodId) {
		this.periodId = periodId;
	}

	public String getPeriodId() {
		return this.periodId;
	}

	public Integer getTtl() {
		return ttl;
	}

	public void setTtl(Integer ttl) {
		this.ttl = ttl;
	}

	@Override
	public String toString() {
		return "Reservation [_rid=" + _rid + ", id=" + id + ", clientName=" + clientName + ", startDate=" + startDate
				+ ", endDate=" + endDate + ", period_id=" + periodId + "]";
	}

}
