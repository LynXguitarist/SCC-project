package scc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.azure.core.annotation.PathParam;
import com.azure.cosmos.util.CosmosPagedIterable;

import cosmos.EntityDBLayer;
import data.Entity;

@Path("/entity")
public class EntityResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createEntity(Entity entity) {
		EntityDBLayer.getInstance().createEntity(entity);
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateEntity(Entity entity) {
		EntityDBLayer.getInstance().putEntity(entity);
	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Entity getEntity(@PathParam("id") String id) {
		CosmosPagedIterable<Entity> items = EntityDBLayer.getInstance().getEntityById(id);
		Entity entity = null;
		for( Entity item: items) {
			entity = item;
		}
		return entity;
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void deleteEntity(@PathParam("id") String id) {
		EntityDBLayer.getInstance().delEntity(id);
	}

	@PUT
	@Path("/likes/{likes}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void likeOrdislike(@PathParam("id") String id, @PathParam("liked") boolean liked) {
		CosmosPagedIterable<Entity> items = EntityDBLayer.getInstance().getEntityById(id);
		Entity entity = null;
		for( Entity item: items) {
			entity = item;
		}
		entity.setLiked(liked);
		EntityDBLayer.getInstance().putEntity(entity);
	}
	
}
