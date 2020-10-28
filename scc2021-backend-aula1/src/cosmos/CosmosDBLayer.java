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

public class CosmosDBLayer<T> {

	// DATABASE CONNECTIONS, KEYS, CLIENTS AND CONTAINER
	private static final String CONNECTION_URL = "https://sc42764.documents.azure.com:443/";
	private static final String DB_KEY = "wclICjcLUCsL36XN1cTVxcvEunzLfkjLfjmSGGI0J086HByJf1YNBJnReax8iiFrCFEGm0zLmRj5yX1WI3bc3g==";
	private static final String DB_NAME = "sc42764DB";

	private static CosmosDBLayer<?> instance;
	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer container;

	// Generic type
	private Class<T> t;

	public CosmosDBLayer(CosmosClient client, Class<T> t) {
		this.client = client;
		this.t = t;
	}

	public static synchronized <T> CosmosDBLayer<?> getInstance(Class<T> t) {
		Locale.setDefault(Locale.US);
		if (instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder().endpoint(CONNECTION_URL).key(DB_KEY).directMode()
				.consistencyLevel(ConsistencyLevel.SESSION).connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true).buildClient();
		instance = new CosmosDBLayer<T>(client, t);
		return instance;
	}

	private synchronized void init(String tableName) {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		container = db.getContainer(tableName);
	}

	public <T> CosmosItemResponse<T> createItem(T item, String tableName) {
		init(tableName);
		return container.createItem(item);
	}

	public <T> CosmosItemResponse<T> putItem(String id, T item, String tableName) {
		init(tableName);
		return container.replaceItem(item, id, new PartitionKey(id), new CosmosItemRequestOptions());
	}

	public CosmosPagedIterable<T> getItemById(String id, String tableName) {
		init(tableName);
		return container.queryItems("SELECT * FROM " + tableName + " WHERE entities.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), t);
	}

	public CosmosPagedIterable<T> getItems(String tableName) {
		init(tableName);
		return container.queryItems("SELECT * FROM " + tableName, new CosmosQueryRequestOptions(), t);
	}

	public CosmosItemResponse<?> delItem(String id, String tableName) {
		init(tableName);
		return container.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
	}

	// -----------------------------SPECIAL_METHODS---------------------------------//

	public CosmosPagedIterable<T> getItemsBySpeacialQuery(String query, String tableName) {
		init(tableName);
		return container.queryItems(query, new CosmosQueryRequestOptions(), t);
	}

	public void close() {
		client.close();
	}

}
