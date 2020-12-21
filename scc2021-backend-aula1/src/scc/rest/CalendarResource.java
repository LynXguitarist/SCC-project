package scc.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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
import javax.ws.rs.core.Response.Status;

import org.bson.Document;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cosmos.CosmosDBLayer;
import data.Calendar;
import data.Entity;
import data.Period;
import data.Reservation;
import mongoDB.MongoDBLayer;
import scc.redis.CacheKeyNames;
import scc.redis.RedisCache;
import scc.utils.AdvancedFeatures;
import scc.utils.TableName;

@Path("/calendar")
public class CalendarResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createCalendar(Calendar calendar) {
		calendar.setId(UUID.randomUUID().toString());
		if (Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.MONGODB))) {
			createCalendarMongo(calendar);
		} else {
			if (ownerExists(calendar.getOwnerId())) {
				CosmosDBLayer<?> dbLayerCalendar = CosmosDBLayer.getInstance(Calendar.class);
				try {
					dbLayerCalendar.createItem(calendar, TableName.CALENDAR.getName());
				} catch (CosmosException e) {
					throw new WebApplicationException(Status.CONFLICT);
				}
			} else {
				throw new WebApplicationException(Status.NOT_FOUND);
			}
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateCalendar(@PathParam("id") String id, Calendar calendar) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Calendar.class);
		try {
			calendar.setId(id);
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
		Calendar calendar = null;
		String key = TableName.CALENDAR.getName() + id;
		boolean hasCache = Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.REDIS));
		String cacheItem = new String();
		if (hasCache)
			cacheItem = RedisCache.getCache().getItemFromCache(key);
		if (cacheItem == null || !hasCache) { // calls the service
			CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Calendar.class);
			CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.CALENDAR.getName());
			for (Object item : items) {
				calendar = (Calendar) item;
			}
			if (calendar == null) {
				throw new WebApplicationException(Status.NOT_FOUND);
			} else if (hasCache) {
				RedisCache.getCache().addItemToCache(key, calendar, 120);
			}
		} else { // retrieves from cache
			ObjectMapper mapper = new ObjectMapper();
			try {
				Calendar cal = mapper.readValue(cacheItem, Calendar.class);
				calendar = cal;
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
			}
		}
		return calendar;
	}

	@GET
	@Path("/entities/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Calendar> getCalendarsByEntityId(@PathParam("id") String id) {
		// entities to return
		List<Calendar> calendars = new LinkedList<>();

		String key = CacheKeyNames.MR_CALENDAR.getName();
		boolean hasCache = Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.REDIS));
		List<String> values = new ArrayList<>();
		if (hasCache)
			values = RedisCache.getCache().getListFromCache(key);
		// Verifies if there is a value for the key in cache
		if (values.isEmpty() || !hasCache) {
			// Calls the Service(CosmosDB)
			CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Calendar.class);
			String query = "SELECT * FROM " + TableName.CALENDAR.getName() + " WHERE " + TableName.CALENDAR.getName()
					+ ".ownerId=\"" + id + "\"";
			CosmosPagedIterable<?> items = dbLayer.getItemsBySpecialQuery(query, TableName.CALENDAR.getName());
			for (Object item : items) {
				Calendar entity = (Calendar) item;
				calendars.add(entity);
			}
			if (calendars.isEmpty())
				throw new WebApplicationException(Status.NOT_FOUND);
			else if (hasCache)
				RedisCache.getCache().addListToCache(key, calendars, 120);

		} else {
			// Retrieves from cache
			for (String v : values) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					Calendar e = mapper.readValue(v, Calendar.class);
					calendars.add(e);
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return calendars;
	}

	// -----------------------------------AVAILABLE_PERIOD-----------------------------//

	@POST
	@Path("/period/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setAvailablePeriod(@PathParam("id") String id, Period period) {
		CosmosDBLayer<?> dbLayerCalendar = CosmosDBLayer.getInstance(Calendar.class);
		CosmosPagedIterable<?> items = dbLayerCalendar.getItemById(id, TableName.CALENDAR.getName());
		Calendar calendar = null;
		for (Object item : items) {
			calendar = (Calendar) item;
		}
		if (calendar == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm");
		LocalDateTime startDate = LocalDateTime.parse(period.getStartDate(), formatter);
		LocalDateTime endDate = LocalDateTime.parse(period.getEndDate(), formatter);
		LocalDateTime currDate = LocalDateTime.now();
		// startDate cant be before currDate and startDate cant be after endDate
		if (startDate.compareTo(currDate) < 0 || endDate.compareTo(startDate) < 0)
			throw new WebApplicationException(Status.BAD_REQUEST);

		String periodId = UUID.randomUUID().toString();
		period.setId(periodId);
		period.setCalendarId(id);
		// Define TTL
		int ttl = (int) ChronoUnit.DAYS.between(currDate, endDate) * 60 * 24 * 30;
		period.setTtl(ttl);

		// add new available period
		CosmosDBLayer<?> dbLayerPeriod = CosmosDBLayer.getInstance(Period.class);
		dbLayerPeriod.createItem(period, TableName.PERIOD.getName());
	}

	@GET
	@Path("/period/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Period> getAvailablePeriodByCalendarId(@PathParam("id") String id) {
		List<Period> periods = new ArrayList<>();
		String key = TableName.PERIOD.getName() + id;
		boolean hasCache = Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.REDIS));
		List<String> cacheItem = new ArrayList<>();

		if (hasCache)
			cacheItem = RedisCache.getCache().getListFromCache(key);

		if (cacheItem.isEmpty() || !hasCache) { // calls the service
			CosmosDBLayer<?> dbLayerPeriod = CosmosDBLayer.getInstance(Period.class);
			String query = "SELECT * FROM " + TableName.PERIOD.getName() + " WHERE " + TableName.PERIOD.getName()
					+ ".calendarId=\"" + id + "\"";
			CosmosPagedIterable<?> items = dbLayerPeriod.getItemsBySpecialQuery(query, TableName.PERIOD.getName());
			for (Object item : items) {
				Period period = (Period) item;
				periods.add(period);
			}

			if (periods.isEmpty()) {
				throw new WebApplicationException(Status.NOT_FOUND);
			} else if (hasCache) {
				RedisCache.getCache().addListToCache(key, periods, 120);
			}

		} else { // retrieves from cache
			for (String v : cacheItem) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					Period p = mapper.readValue(v, Period.class);
					periods.add(p);
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return periods;
	}

	// ---------------------------------RESERVATION----------------------------------//

	@POST
	@Path("/reservation/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addReservation(@PathParam("id") String id, Reservation reservation) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Calendar.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.CALENDAR.getName());
		Calendar calendar = null;
		for (Object item : items) {
			calendar = (Calendar) item;
		}
		if (calendar == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime startDate = LocalDateTime.parse(reservation.getStartDate(), formatter);
		LocalDateTime endDate = LocalDateTime.parse(reservation.getEndDate(), formatter);
		LocalDateTime currDate = LocalDateTime.now();
		// startDate cant be before currDate and startDate cant be after endDate
		if (startDate.compareTo(currDate) < 0 || endDate.compareTo(startDate) < 0)
			throw new WebApplicationException(Status.BAD_REQUEST);

		Period availablePeriod = getAvailablePeriodQuery(id, reservation, startDate, endDate);
		if (availablePeriod == null)
			throw new WebApplicationException(Status.CONFLICT);

		String reservationId = UUID.randomUUID().toString();
		reservation.setId(reservationId);
		reservation.setPeriodId(availablePeriod.getId());
		// Define TTL
		int ttl = (int) ChronoUnit.DAYS.between(currDate, endDate) * 60 * 24 * 30;
		reservation.setTtl(ttl);

		// add new reservation
		CosmosDBLayer<?> dbLayerReservation = CosmosDBLayer.getInstance(Reservation.class);
		dbLayerReservation.createItem(reservation, TableName.RESERVATION.getName());

	}

	@DELETE
	@Path("/reservation/cancel/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void cancelReservation(@PathParam("id") String id) {
		Reservation reservation = null;
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Reservation.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.RESERVATION.getName());
		for (Object item : items) {
			reservation = (Reservation) item;
		}
		if (reservation == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime endDate = LocalDateTime.parse(reservation.getEndDate(), formatter);
		LocalDateTime currDate = LocalDateTime.now();

		if (endDate.compareTo(currDate) < 0)
			throw new WebApplicationException(Status.CONFLICT);

		try {
			dbLayer.delItem(id, TableName.RESERVATION.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@GET
	@Path("/reservation/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Reservation> getReservationByPeriodId(@PathParam("id") String id) {
		List<Reservation> reservations = new ArrayList<>();
		String key = TableName.RESERVATION.getName() + id;
		boolean hasCache = Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.REDIS));
		List<String> cacheItem = new ArrayList<>();

		if (hasCache)
			cacheItem = RedisCache.getCache().getListFromCache(key);

		if (cacheItem.isEmpty() || !hasCache) { // calls the service
			CosmosDBLayer<?> dbLayerRes = CosmosDBLayer.getInstance(Reservation.class);
			String query = "SELECT * FROM " + TableName.RESERVATION.getName() + " WHERE "
					+ TableName.RESERVATION.getName() + ".periodId=\"" + id + "\"";
			CosmosPagedIterable<?> items = dbLayerRes.getItemsBySpecialQuery(query, TableName.RESERVATION.getName());
			for (Object item : items) {
				Reservation res = (Reservation) item;
				reservations.add(res);
			}

			if (reservations.isEmpty()) {
				throw new WebApplicationException(Status.NOT_FOUND);
			} else if (hasCache) {
				RedisCache.getCache().addListToCache(key, reservations, 120);
			}

		} else { // retrieves from cache
			for (String v : cacheItem) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					Reservation p = mapper.readValue(v, Reservation.class);
					reservations.add(p);
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return reservations;
	}

	// ---------------------------------AUX
	// FUNCTIONS----------------------------------//

	private Period getAvailablePeriodQuery(String id, Reservation reservation, LocalDateTime startDate,
			LocalDateTime endDate) {
		Period period = null;
		CosmosDBLayer<?> dbLayerPeriod = CosmosDBLayer.getInstance(Period.class);
		String tableName = TableName.PERIOD.getName();
		String query = "SELECT * FROM " + tableName + " WHERE " + tableName + ".calendarId=\"" + id + "\"" + " AND (\""
				+ startDate + "\" BETWEEN " + tableName + ".startDate AND " + tableName + ".endDate) AND (\"" + endDate
				+ "\" BETWEEN " + tableName + ".startDate AND " + tableName + ".endDate)";
		CosmosPagedIterable<?> items = dbLayerPeriod.getItemsBySpecialQuery(query, TableName.PERIOD.getName());
		for (Object item : items) {
			Period p = (Period) item;
			CosmosDBLayer<?> dbLayerReservation = CosmosDBLayer.getInstance(Reservation.class);
			String query2 = "SELECT * FROM " + TableName.RESERVATION.getName() + " WHERE "
					+ TableName.RESERVATION.getName() + ".periodId=\"" + p.getId() + "\"";
			List<Reservation> reservations = new ArrayList<>();
			CosmosPagedIterable<?> itemsRes = dbLayerReservation.getItemsBySpecialQuery(query2,
					TableName.RESERVATION.getName());
			for (Object itemRes : itemsRes) {
				Reservation r = (Reservation) itemRes;
				reservations.add(r);
			}

			if (reservations.isEmpty()) {
				period = p;
				break;
			}

			for (Reservation r : reservations) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				LocalDateTime resStartDate = LocalDateTime.parse(r.getStartDate(), formatter);
				LocalDateTime resEndDate = LocalDateTime.parse(r.getEndDate(), formatter);
				if ((startDate.compareTo(resStartDate) >= 0 && startDate.compareTo(resEndDate) <= 0)
						|| (endDate.compareTo(resStartDate) >= 0 && endDate.compareTo(resEndDate) <= 0)) {
					return null;
				}
			}

		}
		return period;
	}

	private boolean ownerExists(String id) {
		CosmosDBLayer<?> dbLayerEntity = CosmosDBLayer.getInstance(Entity.class);
		CosmosPagedIterable<?> items = dbLayerEntity.getItemById(id, TableName.ENTITY.getName());
		Entity entity = null;
		for (Object item : items) {
			entity = (Entity) item;
		}
		if (entity == null)
			return false;
		else {
			return true;
		}
	}

	/*-------------------------------------------------MongoDB------------------------------------------------ */
	public void createCalendarMongo(Calendar calendar) {
		MongoDBLayer mongo = MongoDBLayer.getInstance();

		Document document = new Document();
		document.append("id", calendar.getId());
		document.append("name", calendar.getName());
		document.append("description", calendar.getDescription());
		document.append("ownerId", calendar.getOwnerId());

		mongo.addItem(TableName.CALENDAR.getName(), document);
	}
}
