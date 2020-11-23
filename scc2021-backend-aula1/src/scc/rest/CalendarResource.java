package scc.rest;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.util.CosmosPagedIterable;

import cosmos.CosmosDBLayer;
import data.Calendar;
import data.ForumMessage;
import data.Period;
import data.Reservation;
import scc.utils.TableName;

@Path("/calendar")
public class CalendarResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createCalendar(Calendar calendar) {
		CosmosDBLayer<?> dbLayerCalendar = CosmosDBLayer.getInstance(Calendar.class);
		try {
			calendar.setId(UUID.randomUUID().toString());
			dbLayerCalendar.createItem(calendar, TableName.CALENDAR.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.CONFLICT);
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateCalendar(@PathParam("id") String id, Calendar calendar) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Calendar.class);
		try {
			dbLayer.putItem(id, calendar, TableName.CALENDAR.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Calendar getCalendar(@PathParam("id") String id) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Calendar.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.CALENDAR.getName());
		Calendar calendar = null;
		for (Object item : items) {
			calendar = (Calendar) item;
		}
		if (calendar == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		return calendar;
	}

	// -----------------------------------AVAILABLE_PERIOD-----------------------------//

	@PUT
	@Path("/period/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void setAvailablePeriod(@PathParam("id") String id, Period period) {
		CosmosDBLayer<?> dbLayerCalendar = CosmosDBLayer.getInstance(Calendar.class);
		CosmosPagedIterable<?> items = dbLayerCalendar.getItemById(id, TableName.CALENDAR.getName());
		Calendar calendar = null;
		for (Object item : items) {
			calendar = (Calendar) item;
		}
		if (calendar == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		} else {
			String periodId = UUID.randomUUID().toString();
			period.setId(periodId);
			// update calendar
			calendar.addPeriod(periodId);
			dbLayerCalendar.putItem(id, calendar, TableName.CALENDAR.getName());
			// add new available period
			CosmosDBLayer<?> dbLayerPeriod = CosmosDBLayer.getInstance(Period.class);
			dbLayerPeriod.putItem(periodId, period, TableName.PERIOD.getName());
		}
	}

	// ---------------------------------RESERVATION----------------------------------//

	@PUT
	@Path("/reservation/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void addReservation(@PathParam("id") String id, Reservation reservation) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Calendar.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.CALENDAR.getName());
		Calendar calendar = null;
		for (Object item : items) {
			calendar = (Calendar) item;
		}
		if (calendar == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		} else {
			Period availablePeriod = getAvailablePeriod(reservation);
			if (availablePeriod == null) {
				throw new WebApplicationException(Status.CONFLICT);
			} else {
				String reservationId = UUID.randomUUID().toString();
				reservation.setId(reservationId);
				// update calendar
				calendar.addReservation(reservationId);
				dbLayer.putItem(id, calendar, TableName.CALENDAR.getName());
				// update period
				availablePeriod.updateAvailablePeriod(reservation.getStartDate(), reservation.getEndDate());
				CosmosDBLayer<?> dbLayerPeriod = CosmosDBLayer.getInstance(Period.class);
				dbLayerPeriod.putItem(availablePeriod.getId(), availablePeriod, TableName.PERIOD.getName());
				// add new reservation
				CosmosDBLayer<?> dbLayerReservation = CosmosDBLayer.getInstance(Reservation.class);
				dbLayerReservation.putItem(reservationId, reservation, TableName.RESERVATION.getName());
			}
		}
	}

	@PUT
	@Path("/reservation/cancel/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void cancelReservation(@PathParam("id") String id, Reservation reservation) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Calendar.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.CALENDAR.getName());
		Calendar calendar = null;
		for (Object item : items) {
			calendar = (Calendar) item;
		}
		if (calendar == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		} else {
			// update calendar
			calendar.cancelReservation(reservation.getId());
			dbLayer.putItem(id, calendar, TableName.CALENDAR.getName());
			// update period
			Period reservationPeriod = getReservationPeriod(reservation);
			reservationPeriod.cancelReservation(reservation.getStartDate(), reservation.getEndDate());
			CosmosDBLayer<?> dbLayerPeriod = CosmosDBLayer.getInstance(Period.class);
			dbLayerPeriod.putItem(reservationPeriod.getId(), reservationPeriod, TableName.PERIOD.getName());
			// delete reservation
			CosmosDBLayer<?> dbLayerReservation = CosmosDBLayer.getInstance(Reservation.class);
			dbLayerReservation.delItem(reservation.getId(), TableName.RESERVATION.getName());
		}
	}

	// mudar a query, fred depois ajuda
	private Period getAvailablePeriod(Reservation reservation) {
		CosmosDBLayer<?> dbLayerPeriod = CosmosDBLayer.getInstance(Period.class);
		String query = "SELECT * FROM " + TableName.PERIOD.getName() + " WHERE " + TableName.PERIOD.getName()
				+ ".availableStartDate<=\"" + reservation.getStartDate() + " AND " + TableName.PERIOD.getName()
				+ ".availableEndDate>=\"" + reservation.getEndDate() + "\"";
		CosmosPagedIterable<?> items = dbLayerPeriod.getItemsBySpecialQuery(query, TableName.PERIOD.getName());
		Period period = null;
		for (Object item : items) {
			period = (Period) item;
		}
		return period;
	}

	private Period getReservationPeriod(Reservation reservation) {
		CosmosDBLayer<?> dbLayerPeriod = CosmosDBLayer.getInstance(Period.class);
		CosmosPagedIterable<?> items = dbLayerPeriod.getItemsBySpecialQuery(
				"SELECT * FROM " + TableName.PERIOD.getName() + " WHERE " + TableName.PERIOD.getName() + ".periodId=\""
						+ reservation.getPeriodId() + "\"",
				TableName.PERIOD.getName());
		Period period = null;
		for (Object item : items) {
			period = (Period) item;
		}
		return period;
	}
}
