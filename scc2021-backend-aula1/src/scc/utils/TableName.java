package scc.utils;

public enum TableName {
	ENTITY("entities"), FORUM("forums"), FORUMMESSAGE("forummessages"), CALENDAR("calendars"), PERIOD("periods"), RESERVATION("reservations");

	private String name;

	TableName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
