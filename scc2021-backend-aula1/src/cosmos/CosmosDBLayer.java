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

import scc.utils.AzureProperties;

public class CosmosDBLayer<T> {

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

		CosmosClient client = new CosmosClientBuilder()
				.endpoint(AzureProperties.getProperty(AzureProperties.COSMOSDB_URL))
				.key(AzureProperties.getProperty(AzureProperties.COSMOSDB_KEY)).directMode()
				.consistencyLevel(ConsistencyLevel.SESSION).connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true).buildClient();
		instance = new CosmosDBLayer<T>(client, t);
		return instance;
	}

	public CosmosClient getCosmosClient() {
		return client;
	}

	private synchronized void init(String tableName) {
		if (db != null)
			return;
		db = client.getDatabase(AzureProperties.getProperty(AzureProperties.COSMOSDB_DATABASE));
		container = db.getContainer(tableName);
	}

	/**
	 * Creates a record in the table
	 * 
	 * @param <T>
	 * @param item
	 * @param tableName
	 * @return response
	 */
	public <T> CosmosItemResponse<T> createItem(T item, String tableName) {
		init(tableName);
		return container.createItem(item);
	}

	/**
	 * Updates a record in the table
	 * 
	 * @param <T>
	 * @param id
	 * @param item
	 * @param tableName
	 * @return response
	 */
	public <T> CosmosItemResponse<T> putItem(String id, T item, String tableName) {
		init(tableName);
		return container.replaceItem(item, id, new PartitionKey(id), new CosmosItemRequestOptions());
	}

	public CosmosPagedIterable<T> getItemById(String id, String tableName) {
		init(tableName);
		return container.queryItems("SELECT * FROM " + tableName + " WHERE " + tableName + ".id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), t);
	}

	/**
	 * Returns all the records in a Table
	 * 
	 * @param tableName
	 * @return items
	 */
	public CosmosPagedIterable<T> getItems(String tableName) {
		init(tableName);
		return container.queryItems("SELECT * FROM " + tableName, new CosmosQueryRequestOptions(), t);
	}

	/**
	 * Deletes a record from the table
	 * 
	 * @param id
	 * @param tableName
	 * @return response
	 */
	public CosmosItemResponse<?> delItem(String id, String tableName) {
		init(tableName);
		return container.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
	}

	// -----------------------------SPECIAL_METHODS---------------------------------//

	public CosmosPagedIterable<T> getItemsBySpecialQuery(String query, String tableName) {
		init(tableName);
		return container.queryItems(query, new CosmosQueryRequestOptions(), t);
	}

	public void close() {
		client.close();
	}

}
