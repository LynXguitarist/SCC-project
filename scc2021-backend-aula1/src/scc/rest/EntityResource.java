package scc.rest;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bson.Document;

import cosmos.CosmosDBLayer;
import data.Entity;
import mongoDB.MongoDBLayer;
import scc.redis.CacheKeyNames;
import scc.redis.RedisCache;
import scc.utils.AdvancedFeatures;
import scc.utils.TableName;

@Path("/entity")
public class EntityResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createEntity(Entity entity) {
		entity.setId(UUID.randomUUID().toString());
		entity.setDeleted(false);
		if (Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.MONGODB))) {
			createEntityMongo(entity);
		} else {
			createEntityCosmos(entity);
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateEntity(@PathParam("id") String id, Entity entity) {
		// This method handles the update to isDeleted too
		// The remove of the entity is done via Timer Function

		if (entity.isDeleted()) // If isDeleted, deleteDate = currDate
			entity.setDeletionDate(LocalDate.now(ZoneOffset.UTC).toString());

		entity.setId(id);
		if (Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.MONGODB))) {
			updateEntityMongo(entity);
		} else {
			updateEntityCosmos(entity);
		}
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Entity getEntity(@PathParam("id") String id) {
		Entity entity = null;
		if (Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.MONGODB))) {
			entity = getEntityMongo(id);
		} else {
			entity = getEntityCosmos(id);
		}
		return entity;
	}

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Entity> getEntities() {
		// entities to return
		List<Entity> entities = new LinkedList<>();

		String key = CacheKeyNames.MR_ENTITY.getName();
		boolean hasCache = Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.REDIS));
		List<String> values = new ArrayList<>();
		if (hasCache)
			values = RedisCache.getCache().getListFromCache(key);
		// Verifies if there is a value for the key in cache
		if (values.isEmpty() || !hasCache) {
			if (Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.MONGODB))) {
				entities = getEntitiesMongo();
			} else {
				// Calls the Service(CosmosDB)
				entities = getEntitiesCosmos();
			}
			if (hasCache)
				RedisCache.getCache().addListToCache(key, entities, 120);

		} else {
			// Retrieves from cache
			for (String v : values) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					Entity e = mapper.readValue(v, Entity.class);
					entities.add(e);
				} catch (JsonProcessingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		return entities;
	}

	@PUT
	@Path("/likes/{liked}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void likeOrdislike(@PathParam("liked") boolean liked, @PathParam("id") String id, Entity entity) {
		int inc = -1;
		if (liked)
			inc = 1;
		entity.setId(id);
		entity.setNumberOfLikes(inc);
		if (Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.MONGODB))) {
			updateEntityMongo(entity);
		} else {
			CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
			try {
				dbLayer.putItem(id, entity, TableName.ENTITY.getName());
			} catch (CosmosException e) {
				throw new WebApplicationException(Status.NOT_FOUND);
			}
		}
	}

	/*-------------------------------------------------MongoDB------------------------------------------------ */
	public void createEntityMongo(Entity entity) {
		MongoDBLayer mongo = MongoDBLayer.getInstance();

		Document document = new Document();
		document.append("id", entity.getId());
		document.append("name", entity.getName());
		document.append("description", entity.getDescription());
		document.append("mediaIds", entity.getMediaIds());
		document.append("numberOfLikes", entity.getNumberOfLikes());
		document.append("deleted", entity.isDeleted());
		document.append("deletionDate", entity.getDeletionDate());

		mongo.addItem(TableName.ENTITY.getName(), document);
	}

	public void updateEntityMongo(Entity entity) {
		MongoDBLayer mongo = MongoDBLayer.getInstance();

		Document document = new Document();
		document.append("id", entity.getId());
		document.append("name", entity.getName());
		document.append("description", entity.getDescription());
		document.append("mediaIds", entity.getMediaIds());
		document.append("numberOfLikes", entity.getNumberOfLikes());
		document.append("deleted", entity.isDeleted());
		document.append("deletionDate", entity.getDeletionDate());

		mongo.updateItem(TableName.ENTITY.getName(), entity.getId(), document);
	}

	public Entity getEntityMongo(String id) {
		MongoDBLayer mongo = MongoDBLayer.getInstance();

		return mongo.getEntityById(id);
	}

	public List<Entity> getEntitiesMongo() {
		MongoDBLayer mongo = MongoDBLayer.getInstance();

		return mongo.getEntities();
	}

	/*-------------------------------------------------CosmosDB------------------------------------------------ */

	public void createEntityCosmos(Entity entity) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		try {
			dbLayer.createItem(entity, TableName.ENTITY.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.CONFLICT);
		}
	}

	public void updateEntityCosmos(Entity entity) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		try {
			dbLayer.putItem(entity.getId(), entity, TableName.ENTITY.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	public Entity getEntityCosmos(String id) {
		Entity entity = null;

		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.ENTITY.getName());

		for (Object item : items) {
			entity = (Entity) item;
		}
		if (entity == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		return entity;
	}

	public List<Entity> getEntitiesCosmos() {
		List<Entity> entities = new LinkedList<>();

		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		CosmosPagedIterable<?> items = dbLayer.getItems(TableName.ENTITY.getName());
		for (Object item : items) {
			Entity entity = (Entity) item;
			entities.add(entity);
		}
		if (entities.isEmpty())
			throw new WebApplicationException(Status.NOT_FOUND);

		return entities;
	}
}
