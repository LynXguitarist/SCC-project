package data;

public enum TableName {
	ENTITY("entities"), FORUM("forums"), CALENDAR("calendars");

	private String name;

	TableName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
