package cosmos;

import java.util.Locale;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;

import data.Calendar;
import data.Entity;

public class ReservationDBLayer {

	// DATABASE
	private static final String CONNECTION_URL = "https://sc42764.documents.azure.com:443/";
	private static final String DB_KEY = "wclICjcLUCsL36XN1cTVxcvEunzLfkjLfjmSGGI0J086HByJf1YNBJnReax8iiFrCFEGm0zLmRj5yX1WI3bc3g==";
	private static final String DB_NAME = "sc42764DB";
	private static final String DB_CONTAINER = "reservations";
	
	private static ReservationDBLayer instance;
	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer entities;

	public ReservationDBLayer(CosmosClient client) {
		this.client = client;
	}

	public static synchronized ReservationDBLayer getInstance() {
		Locale.setDefault(Locale.US);
		if (instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder().endpoint(CONNECTION_URL).key(DB_KEY).directMode()
				.consistencyLevel(ConsistencyLevel.SESSION).connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true).buildClient();
		instance = new ReservationDBLayer(client);
		return instance;

	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		entities = db.getContainer(DB_CONTAINER);
	}

	public CosmosItemResponse<Calendar> createCalendar(Calendar calendar) {
		init();
		return entities.createItem(calendar);
	}

	public CosmosItemResponse<Calendar> putCalendar(Calendar calendar) {
		init();
		return entities.replaceItem(calendar, calendar.getId(), new PartitionKey(calendar.getId()),
				new CosmosItemRequestOptions());
	}

	public CosmosPagedIterable<Calendar> getCalendarById(String id) {
		init();
		return entities.queryItems("SELECT * FROM reservations WHERE reservations.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), Calendar.class);
	}

	public CosmosPagedIterable<Calendar> getCalendars() {
		init();
		return entities.queryItems("SELECT * FROM reservations ", new CosmosQueryRequestOptions(), Calendar.class);
	}
	
	public CosmosItemResponse<Object> delCalendar(String id) {
		init();
		return entities.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
	}

	public void close() {
		client.close();
	}
}
