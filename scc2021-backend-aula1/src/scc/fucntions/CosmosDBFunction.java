package scc.fucntions;

import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import scc.redis.RedisCache;
import scc.utils.AzureProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class CosmosDBFunction {
	static int count = 0;

	@FunctionName("cosmosDBtest")
	public void cosmosDbProcessor(
			@CosmosDBTrigger(name = "messages", databaseName = AzureProperties.COSMOSDB_DATABASE, collectionName = "Forum", 
			createLeaseCollectionIfNotExists = true, connectionStringSetting = "AzureCosmosDBConnection") String[] msgs,
			final ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			try {
				jedis.lpush("serverless::cosmos::msgs", new ObjectMapper().writeValueAsString(msgs));
			} catch (JsonProcessingException e) {
				jedis.lpush("serverless::cosmos::msgs", e.getMessage());
			}
			jedis.ltrim("serverless::cosmos::msgs", 0, 9);
		}
	}

}
