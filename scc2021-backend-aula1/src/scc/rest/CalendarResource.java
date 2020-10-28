package scc.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;

import cosmos.ForumDBLayer;
import cosmos.ReservationDBLayer;
import data.Calendar;
import data.Entity;
import data.Forum;
import data.ForumMessage;
import data.Period;
import data.Reservation;

@Path("/calendar")
public class CalendarResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createCalendar(Calendar calendar) {
		CosmosPagedIterable<Calendar> items = ReservationDBLayer.getInstance().getCalendarById(calendar.getId());
		Calendar ent = null;
		for (Calendar item : items) {
			ent = item;
		}
		if (ent != null)
			throw new WebApplicationException(Status.CONFLICT);

		CosmosItemResponse<Calendar> cosmos_response = ReservationDBLayer.getInstance().createCalendar(calendar);
		int response = cosmos_response.getStatusCode();
		if (response != 200)
			throw new WebApplicationException(response);
	}
	
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateCalendar(@PathParam("id") String id, Calendar calendar) {
		ReservationDBLayer dbLayer = ReservationDBLayer.getInstance();
		CosmosPagedIterable<Calendar> items = dbLayer.getCalendarById(id);
		Calendar ent = null;
		for (Calendar item : items) {
			ent = item;
		}
		if (ent == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		CosmosItemResponse<Calendar> cosmos_response = dbLayer.putCalendar(ent);
		int response = cosmos_response.getStatusCode();
		if (response != 200)
			throw new WebApplicationException(response);
	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Calendar getCalendar(@PathParam("id") String id) {
		CosmosPagedIterable<Calendar> items = ReservationDBLayer.getInstance().getCalendarById(id);
		Calendar entity = null;
		for (Calendar item : items) {
			entity = item;
		}
		if (entity == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		return entity;
	}
	
	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setAvailablePeriod(@PathParam("id") String id, Period period) {
		CosmosPagedIterable<Calendar> items = ReservationDBLayer.getInstance().getCalendarById(id);
        Calendar entity = null;
        for (Calendar item : items) {
            entity = item;
        }
        if (entity == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        else {
            entity.addPeriod(period);
            ReservationDBLayer.getInstance().putCalendar(entity);
        }
	}

	@POST
	@Path("/reservation{id}/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addReservation(@PathParam("id") String id, Reservation reservation) {
		CosmosPagedIterable<Calendar> items = ReservationDBLayer.getInstance().getCalendarById(id);
        Calendar entity = null;
        for (Calendar item : items) {
            entity = item;
        }
        if (entity == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        else {
            entity.addReservation(reservation);
            ReservationDBLayer.getInstance().putCalendar(entity);
        }
	}

	@DELETE
	@Path("/reservation/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void cancelReservation(@PathParam("id") String id, Reservation reservation) {
		CosmosPagedIterable<Calendar> items = ReservationDBLayer.getInstance().getCalendarById(id);
        Calendar entity = null;
        for (Calendar item : items) {
            entity = item;
        }
        if (entity == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        else {
            entity.cancelReservation(reservation);
            ReservationDBLayer.getInstance().putCalendar(entity);
        }
	}
}
