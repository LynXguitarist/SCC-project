package scc.rest;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import cosmos.CosmosDBLayer;
import data.Entity;
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
			dbLayer.putItem(id, entity, TableName.ENTITY.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
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

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void deleteEntity(@PathParam("id") String id) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		try {
			dbLayer.delItem(id, TableName.ENTITY.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@PUT
	@Path("/likes/{liked}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void likeOrdislike(@PathParam("liked") boolean liked, Entity entity) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Entity.class);
		try {
			String id = entity.getId();
			entity.setLiked(liked);
			dbLayer.putItem(id, entity, TableName.ENTITY.getName());
		} catch (CosmosException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

}
