package scc.utils;

public enum TableName {
	ENTITY("entities"), FORUM("forums"), FORUMMESSAGES("forummessages"), CALENDAR("calendars");

	private String name;

	TableName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
