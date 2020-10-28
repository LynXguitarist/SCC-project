package data;

import java.util.Arrays;
import java.util.List;

public class Calendar {

	private String _rid;
	private String id;
	private String name;
	private String description;
	private List<Period> availablePeriods;// available period
	private List<Reservation> reservations;// list reservation
	
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
	
	public void addPeriod(Period period) {
		this.availablePeriods.add(period);
	}
	
	public void addReservation(Reservation reservation) {
		this.reservations.add(reservation);
	}
	
	public void cancelReservation(Reservation reservation) {
		for (Reservation res: this.reservations) {
			if(res.getClientName().equals(reservation.getClientName()) 
					&& res.getStartDate().equals(reservation.getStartDate()) 
						&& res.getEndDate().equals(reservation.getEndDate())) {
				this.reservations.remove(res);
			}
		}
	}
	
	@Override
	public String toString() {
		return "Calendar [_rid=" + _rid + ", id=" + id + ", name=" + name + ", description=" + description + ", availablePeriods"
				+ availablePeriods.toString() + ", reservations" + reservations.toString() + "]";
	}
	
}
