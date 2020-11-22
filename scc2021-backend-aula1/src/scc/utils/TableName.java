package scc.utils;

public enum TableName {
	ENTITY("Entity"), FORUM("Forum"), FORUMMESSAGE("ForumMessage"), CALENDAR("Calendar"), PERIOD("Period"), RESERVATION("Reservation");

	private String name;

	TableName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
