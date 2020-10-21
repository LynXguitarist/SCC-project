package scc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.azure.core.annotation.PathParam;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;

import cosmos.EntityDBLayer;
import data.Entity;

@Path("/entity")
public class EntityResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createEntity(Entity entity) {
		CosmosItemResponse<Entity> cosmos_response = EntityDBLayer.getInstance().createEntity(entity);
		int response = cosmos_response.getStatusCode();
		if (response != 200)
			throw new WebApplicationException(response);
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateEntity(Entity entity) {
		CosmosItemResponse<Entity> cosmos_response = EntityDBLayer.getInstance().putEntity(entity);
		int response = cosmos_response.getStatusCode();
		if (response != 200)
			throw new WebApplicationException(response);
	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Entity getEntity(@PathParam("id") String id) {
		CosmosPagedIterable<Entity> items = EntityDBLayer.getInstance().getEntityById(id);
		Entity entity = null;
		for (Entity item : items) {
			entity = item;
		}
		if (entity == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		return entity;
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void deleteEntity(@PathParam("id") String id) {
		CosmosItemResponse<Object> cosmos_response = EntityDBLayer.getInstance().delEntity(id);
		int response = cosmos_response.getStatusCode();
		if (response != 200)
			throw new WebApplicationException(response);
	}

	@PUT
	@Path("/likes/{likes}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void likeOrdislike(@PathParam("id") String id, @PathParam("liked") boolean liked) {
		CosmosPagedIterable<Entity> items = EntityDBLayer.getInstance().getEntityById(id);
		Entity entity = null;
		for (Entity item : items) {
			entity = item;
		}
		if (entity == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		entity.setLiked(liked);
		EntityDBLayer.getInstance().putEntity(entity);
	}

}
