package cosmos;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.CosmosPagedIterable;
import data.Forum;

import java.util.Locale;

public class ForumDBLayer {

	// DATABASE
	private static final String CONNECTION_URL = "https://sc42764.documents.azure.com:443/";
	private static final String DB_KEY = "wclICjcLUCsL36XN1cTVxcvEunzLfkjLfjmSGGI0J086HByJf1YNBJnReax8iiFrCFEGm0zLmRj5yX1WI3bc3g==";
	private static final String DB_NAME = "sc42764DB";
	private static final String DB_CONTAINER = "forums";

	private static ForumDBLayer instance;
	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer forums;

	public ForumDBLayer(CosmosClient client){
		this.client = client;
	}

	public static synchronized ForumDBLayer getInstance() {
		Locale.setDefault(Locale.US);
		if (instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder().endpoint(CONNECTION_URL).key(DB_KEY).directMode()
				.consistencyLevel(ConsistencyLevel.SESSION).connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true).buildClient();
		instance = new ForumDBLayer(client);
		return instance;

	}

	private synchronized void init() {
		if (db != null)
			return;
		db = client.getDatabase(DB_NAME);
		forums = db.getContainer(DB_CONTAINER);
	}

	public CosmosItemResponse<Forum> createForum(Forum forum) {
		init();
		return forums.createItem(forum);
	}

	public CosmosItemResponse<Forum> putForum(Forum forum) {
		init();
		return forums.replaceItem(forum, forum.getId(), new PartitionKey(forum.getId()),
				new CosmosItemRequestOptions());
	}

	public CosmosPagedIterable<Forum> getForumById(String id) {
		init();
		return forums.queryItems("SELECT * FROM forums WHERE forums.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), Forum.class);
	}

	public CosmosPagedIterable<Forum> getForums() {
		init();
		return forums.queryItems("SELECT * FROM forums ", new CosmosQueryRequestOptions(), Forum.class);
	}

	public CosmosItemResponse<Object> delForum(String id) {
		init();
		return forums.deleteItem(id, new PartitionKey(id), new CosmosItemRequestOptions());
	}

	public void close() {
		client.close();
	}
}
