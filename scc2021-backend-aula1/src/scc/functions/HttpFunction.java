package scc.functions;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import com.microsoft.azure.functions.annotation.*;

import cosmos.CosmosDBLayer;
import data.Entity;
import redis.clients.jedis.Jedis;
import scc.redis.RedisCache;
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

	/*

	@FunctionName("get-redis")
	public HttpResponseMessage getRedis(@HttpTrigger(name = "req", methods = {
			HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS, route = "serverless/redis/{key}") HttpRequestMessage<Optional<String>> request,
			@BindingName("key") String key, final ExecutionContext context) {
		synchronized (HttpFunction.class) {
			HttpFunction.count++;
		}
		try (Jedis jedis = RedisCache.getCache().getJedisPool().getResource()) {
			String val = jedis.get(key);
			return request.createResponseBuilder(HttpStatus.OK).body("GET key = " + key + "; val = " + val).build();
		}
	}

	@FunctionName("set-redis")
	public HttpResponseMessage setRedis(@HttpTrigger(name = "req", methods = {
			HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS, route = "serverless/redis/{key}") HttpRequestMessage<Optional<String>> request,
			@BindingName("key") String key, final ExecutionContext context) {
		synchronized (HttpFunction.class) {
			HttpFunction.count++;
		}
		String val = request.getBody().orElse("");
		try (Jedis jedis = RedisCache.getCache().getJedisPool().getResource()) {
			jedis.set(key, val);
			return request.createResponseBuilder(HttpStatus.OK).body("SET key = " + key + "; val = " + val).build();
		}
	}
	
	*/
}
