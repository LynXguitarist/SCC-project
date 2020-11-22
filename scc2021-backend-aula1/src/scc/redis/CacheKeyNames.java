package scc.redis;

public enum CacheKeyNames {
	MR_ENTITY("MostRecentEntities"), MR_FORUM("MostRecentForums"), MR_FORUMMESSAGE("MostRecentForumMessages"), 
		MR_CALENDAR("MostRecentCalendars"), MR_PERIOD("MostRecentPeriods"), MR_RESERVATION("MostRecentReservations");

	private String name;

	CacheKeyNames(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
