package scc.functions;

import java.util.*;

import com.microsoft.azure.functions.annotation.*;

import cosmos.CosmosDBLayer;
import data.Entity;
import data.ForumMessage;
import scc.utils.AzureProperties;
import scc.utils.TableName;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.google.gson.Gson;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger. These functions can be accessed at
 * {Server_URL}/api/{route}
 */
public class HttpFunction {
	static int count = 0;

	/**
	 * Function for the most popular entities(5 Entities)
	 * 
	 * @param request
	 * @return list of most popular entities
	 */
	@FunctionName("popular-entities")
	public HttpResponseMessage getPopularEntities(@HttpTrigger(name = "req", methods = {
			HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "serverless/popular/entities") HttpRequestMessage<Optional<String>> request) {
		
		// Use cache too
		String query = "SELECT * FROM " + TableName.ENTITY.getName() + " e ORDER BY e.numberOfLikes DESC LIMIT 5";
		CosmosPagedIterable<Entity> it = CosmosDBLayer.getInstance(Entity.class).getCosmosClient()
				.getDatabase(AzureProperties.getProperty(AzureProperties.COSMOSDB_DATABASE))
				.getContainer(TableName.ENTITY.getName())
				.queryItems(query, new CosmosQueryRequestOptions(), Entity.class);

		Gson gson = new Gson();
		String result = gson.toJson(it);
		return request.createResponseBuilder(HttpStatus.OK).body(result).build();
	}

	/**
	 * Function for the recent entities(5 Entities)
	 * 
	 * @param request
	 * @return list of the recent entities
	 */
	@FunctionName("recent-entities")
	public HttpResponseMessage getRecentEntities(@HttpTrigger(name = "req", methods = {
			HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "serverless/recent/entities") HttpRequestMessage<Optional<String>> request) {

		// use cache too
		String query = "SELECT * FROM " + TableName.ENTITY.getName() + " e ORDER BY e._ts DESC LIMIT 5";
		CosmosPagedIterable<Entity> it = CosmosDBLayer.getInstance(Entity.class).getCosmosClient()
				.getDatabase(AzureProperties.getProperty(AzureProperties.COSMOSDB_DATABASE))
				.getContainer(TableName.ENTITY.getName())
				.queryItems(query, new CosmosQueryRequestOptions(), Entity.class);

		Gson gson = new Gson();
		String result = gson.toJson(it);
		return request.createResponseBuilder(HttpStatus.OK).body(result).build();
	}

	/**
	 * Function for the recent forumMessages of the owner without Reply
	 *
	 * @param request
	 * @return list of messages without replies
	 */
	@FunctionName("recent-messages-without-reply")
	public HttpResponseMessage getForumMessagesWithoutReply(@HttpTrigger(name = "req", methods = {
			HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "serverless/messages/withoutreply") HttpRequestMessage<Optional<String>> request) {

		String forumId = request.getQueryParameters().getOrDefault("forumId", "");

		// use cache too
		String query = "SELECT * FROM " + TableName.FORUMMESSAGE.getName() + " e WHERE e.forumId=\"" + forumId
				+ "\" AND e.reply IS NULL ORDER BY e._ts DESC LIMIT 5";
		CosmosPagedIterable<ForumMessage> it = CosmosDBLayer.getInstance(ForumMessage.class).getCosmosClient()
				.getDatabase(AzureProperties.getProperty(AzureProperties.COSMOSDB_DATABASE))
				.getContainer(TableName.FORUMMESSAGE.getName())
				.queryItems(query, new CosmosQueryRequestOptions(), ForumMessage.class);

		Gson gson = new Gson();
		String result = gson.toJson(it);
		return request.createResponseBuilder(HttpStatus.OK).body(result).build();
	}
}
