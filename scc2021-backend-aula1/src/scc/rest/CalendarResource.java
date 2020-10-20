package scc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@Path("/calendar")
public class CalendarResource {

	/**
	 * Vai ter de ter uma entity para reservation e outra para
	 * availablePeriods(CosmoDB)
	 */

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public void setAvailablePeriod() {

	}

	@POST
	@Path("/reservation")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public void addReservation() {

	}

	@DELETE
	@Path("/reservation/{id}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	public void cancelReservation(@PathParam("id") String id) {

	}
}
