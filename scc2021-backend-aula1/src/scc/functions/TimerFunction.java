package scc.functions;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;

import cosmos.CosmosDBLayer;
import redis.clients.jedis.Jedis;
import data.Entity;
import data.Calendar;
import data.Forum;
import data.ForumMessage;
import data.Period;
import data.Reservation;
import scc.redis.RedisCache;
import scc.utils.AzureProperties;
import scc.utils.TableName;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class TimerFunction {
	static int count = 0;

	/*
	 * Timers para apagar coisas antigas e talvez para popular cache
	 */

	@FunctionName("periodic-compute")
	public void cosmosFunction(@TimerTrigger(name = "keepAliveTrigger", schedule = "*/20 * * * * *") String timerInfo,
			ExecutionContext context) {
		synchronized (HttpFunction.class) {
			HttpFunction.count++;
		}
		try (Jedis jedis = RedisCache.getCache().getJedisPool().getResource()) {
			jedis.set("serverlesstime", new SimpleDateFormat().format(new Date()));
			try {
				CosmosPagedIterable<ForumMessage> it = CosmosDBLayer.getInstance(Forum.class).getCosmosClient()
						.getDatabase(AzureProperties.getProperty(AzureProperties.COSMOSDB_DATABASE))
						.getContainer("Forum")
						.queryItems("SELECT * FROM Forum f ORDER BY f.creationTime DESC OFFSET 0 LIMIT 10",
								new CosmosQueryRequestOptions(), ForumMessage.class);

				List<ForumMessage> lst = new ArrayList<ForumMessage>();
				it.stream().forEach(m -> lst.add(m));

				jedis.set("serverless:cosmos", new ObjectMapper().writeValueAsString(lst));
			} catch (Exception e) {
				jedis.set("serverless:cosmos", "[]");
			}
		}
	}

	@FunctionName("periodic-delete")
	public void deleteCalendars(@TimerTrigger(name = "keepAliveTrigger", schedule = "0 0 0 * * Sun") String timerInfo,
			ExecutionContext context) {
		CosmosDBLayer<?> dbLayerEntity = CosmosDBLayer.getInstance(Entity.class);
		CosmosDBLayer<?> dbLayerCalendar = CosmosDBLayer.getInstance(Calendar.class);
		CosmosDBLayer<?> dbLayerPeriod = CosmosDBLayer.getInstance(Period.class);
		CosmosDBLayer<?> dbLayerReservation = CosmosDBLayer.getInstance(Reservation.class);
		String entityTable = TableName.ENTITY.getName();
		String calendarTable = TableName.CALENDAR.getName();
		String periodTable = TableName.PERIOD.getName();
		String resTable = TableName.RESERVATION.getName();
		// query that selects all entities marked as deleted and which delete period of
		// 10 days has expired
		LocalDateTime utcDateLimit = LocalDateTime.now(ZoneOffset.UTC).minusDays(10);
		String query = "SELECT "  + entityTable + ".id" + " FROM " + entityTable + " WHERE " + entityTable + ".isDeleted=1 AND " + entityTable
				+ ".deletionDate<=\"" + utcDateLimit + "\"";
		CosmosPagedIterable<?> items = dbLayerEntity.getItemsBySpecialQuery(query, entityTable);

		for (Object item : items) {
			String entityId = (String) item;
			// delete calendars
			// query that selects all calendars of that entity
			String calendarQuery = "SELECT " + calendarTable + ".id" + " FROM " + calendarTable + " WHERE " + calendarTable + ".ownerId=\""
					+ entityId + "\"";
			CosmosPagedIterable<?> itemsCalendar = dbLayerCalendar.getItemsBySpecialQuery(calendarQuery, calendarTable);
			for (Object itemCalendar : itemsCalendar) {
				String calendarId = (String) itemCalendar;
				dbLayerCalendar.delItem(calendarId, calendarTable);
				//delete periods
				String periodQuery = "SELECT " + periodTable + ".id" + " FROM " + periodTable + " WHERE " + periodTable + ".calendarId=\""
						+ calendarId + "\"";
				CosmosPagedIterable<?> itemsPeriod = dbLayerCalendar.getItemsBySpecialQuery(periodQuery, periodTable);
				for (Object itemPeriod: itemsPeriod) {
					String periodId = (String) itemPeriod;
					dbLayerPeriod.delItem(periodId, periodTable);
						//delete reservations
						String resQuery = "SELECT " + resTable + ".id" + " FROM " + resTable + " WHERE " + resTable + ".periodId=\""
								+ periodId + "\"";
						CosmosPagedIterable<?> itemsRes = dbLayerReservation.getItemsBySpecialQuery(resQuery, resTable);
						for (Object itemRes: itemsRes) {
							String reservationId = (String) itemRes;
							dbLayerReservation.delItem(reservationId, resTable);
						}
				}
			}
		}
	}

}
