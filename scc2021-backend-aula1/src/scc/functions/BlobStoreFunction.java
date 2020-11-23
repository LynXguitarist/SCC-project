package scc.functions;

import com.microsoft.azure.functions.annotation.*;

import redis.clients.jedis.Jedis;
import scc.redis.RedisCache;

import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer Trigger.
 */
public class BlobStoreFunction {
	static int count = 0;

	@FunctionName("blobtest")
	public void run(
			@BlobTrigger(name = "blob", dataType = "binary", path = "images/{name}", connection = "BlobStoreConnection") byte[] content,
			@BindingName("name") String blobname, final ExecutionContext context) {
		try (Jedis jedis = RedisCache.getCache().getJedisPool().getResource()) {
			jedis.set("serverless::blob::name",
					"Blob name : " + blobname + " ; size = " + (content == null ? "0" : content.length));
		}
	}

}
