package scc.rest;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.google.gson.Gson;

import cosmos.CosmosDBLayer;
import data.Entity;
import scc.redis.CacheKeyNames;
import scc.redis.RedisCache;
import scc.utils.AdvanceFeatures;
import scc.utils.TableName;

@Path("/entity")
public class EntityResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createEntity(Entity entity) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		try {
			entity.setId(UUID.randomUUID().toString());
			dbLayer.createItem(entity, TableName.ENTITY.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.CONFLICT);
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateEntity(@PathParam("id") String id, Entity entity) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		try {
			// This method handles the update to isDeleted too
			// The remove of the entity is done via Timer Function
			dbLayer.putItem(id, entity, TableName.ENTITY.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Entity getEntity(@PathParam("id") String id) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.ENTITY.getName());
		Entity entity = null;
		for (Object item : items) {
			entity = (Entity) item;
		}
		if (entity == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		return entity;
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Entity> getEntities() {
		List<Entity> entities = new LinkedList<>();
		// Cache
		String key = CacheKeyNames.MR_ENTITY.getName();
		List<String> values = RedisCache.getCache().getListFromCache(key);
		// Verifies if there is a value for the key in cache
		if (values.isEmpty() || !AdvanceFeatures.getProperty("Redis")) {
			// Calls the Service(CosmosDB)
			CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
			CosmosPagedIterable<?> items = dbLayer.getItems(TableName.ENTITY.getName());
			for (Object item : items) {
				Entity entity = (Entity) item;
				entities.add(entity);
			}
			if (entities.isEmpty())
				throw new WebApplicationException(Status.NOT_FOUND);

			RedisCache.getCache().addListToCache(key, entities, 120);
		} else {
			// Retrieves from cache
			for (String v : values) {
				Gson gson = new Gson();
				Entity e = gson.fromJson(v, Entity.class);
				entities.add(e);
			}
		}
		return entities;
	}

	@PUT
	@Path("/likes/{liked}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void likeOrdislike(@PathParam("liked") boolean liked, Entity entity) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		try {
			String id = entity.getId();
			int inc = -1;
			if (liked)
				inc = 1;
			entity.setNumberOfLikes(inc);
			dbLayer.putItem(id, entity, TableName.ENTITY.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

}
