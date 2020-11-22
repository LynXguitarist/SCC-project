package data;

import java.util.List;

public class Calendar {

	private String _rid;
	private String id;
	private String name;
	private String description;
	private List<String> availablePeriods;// available periods' ids
	private List<String> reservations;// list reservations' ids
	
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void addPeriod(String periodId) {
		this.availablePeriods.add(periodId);
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
	
	@Override
	public String toString() {
		return "Calendar [_rid=" + _rid + ", id=" + id + ", name=" + name + ", description=" + description + ", availablePeriods="
				+ availablePeriods.toString() + ", reservations=" + reservations.toString() + "]";
	}
	
}
