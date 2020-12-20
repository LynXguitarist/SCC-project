package scc.srv;

import scc.utils.AzureProperties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/ctrl")
public class ControlResource
{

	@Path("/version")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		StringBuilder str = new StringBuilder();
		str.append("AzureProperties: \n");
		str.append("RedisKey: " + AzureProperties.getProperties().getProperty(AzureProperties.REDIS_KEY));
		str.append("\n");
		str.append("COSMOSDB_URL: " + AzureProperties.getProperty(AzureProperties.COSMOSDB_URL));
		str.append("\n");
		str.append("COSMOSDB_KEY: " + AzureProperties.getProperty(AzureProperties.COSMOSDB_KEY));
		str.append("\n");
		str.append("COSMOSDB_DATABASE: " + AzureProperties.getProperty(AzureProperties.COSMOSDB_DATABASE));
		str.append("\n");
		str.append("BLOB_KEY: " + AzureProperties.getProperty(AzureProperties.BLOB_KEY));
		str.append("\n");
		str.append("v: 0001");
		return str.toString();
	}

}
