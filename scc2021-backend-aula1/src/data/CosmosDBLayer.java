package data;

import java.util.Locale;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;

public class CosmosDBLayer {
	// DATABASE
	private static final String CONNECTION_URL = "https://sc42764.documents.azure.com:443/";
	private static final String DB_KEY = "wclICjcLUCsL36XN1cTVxcvEunzLfkjLfjmSGGI0J086HByJf1YNBJnReax8iiFrCFEGm0zLmRj5yX1WI3bc3g==";
	private static final String DB_NAME = "sc42764";

	private static CosmosDBLayer instance;
	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer entities;

	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
	}

	public static synchronized CosmosDBLayer getInstance() {
		Locale.setDefault(Locale.US);
		if (instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder().endpoint(CONNECTION_URL).key(DB_KEY).directMode()
				.consistencyLevel(ConsistencyLevel.SESSION).connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true).buildClient();
		instance = new CosmosDBLayer(client);
		return instance;

	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		entities = db.getContainer("entities");

	}

	public CosmosItemResponse<Object> delEntity(Entity entity) {
		init();
		return entities.deleteItem(entity, new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<Entity> putEntity(Entity entity) {
		init();
		return entities.createItem(entity);
	}

	public CosmosPagedIterable<Entity> getEntityById(String id) {
		init();
		return entities.queryItems("SELECT * FROM entities WHERE entities.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), Entity.class);
	}

	public CosmosPagedIterable<Entity> getEntities() {
		init();
		return entities.queryItems("SELECT * FROM entities ", new CosmosQueryRequestOptions(), Entity.class);
	}

	public void close() {
		client.close();
	}

}