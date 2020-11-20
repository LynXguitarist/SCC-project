package scc.fucntions;

import java.text.SimpleDateFormat;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;

import cosmos.CosmosDBLayer;
import redis.clients.jedis.Jedis;
import data.Forum;
import data.ForumMessage;
import scc.redis.RedisCache;
import scc.utils.AzureProperties;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class TimerFunction {
	static int count = 0;

	@FunctionName("periodic-compute")
	public void cosmosFunction(@TimerTrigger(name = "keepAliveTrigger", schedule = "*/20 * * * * *") String timerInfo,
			ExecutionContext context) {
		synchronized (HttpFunction.class) {
			HttpFunction.count++;
		}
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
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
}
