package scc.rest;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.util.CosmosPagedIterable;
import cosmos.CosmosDBLayer;
import data.Forum;
import data.ForumMessage;
import data.TableName;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/forum")
public class ForumResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void createForum(Forum forum) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Forum.class);
		try {
			dbLayer.createItem(forum, TableName.FORUM.getName());
		} catch (CosmosException e) {
			// tambem tem de ver se o owner existe(Entity)
			throw new WebApplicationException(Response.Status.CONFLICT);
		}
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateForum(@PathParam("id") String id, Forum forum) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Forum.class);
		try {
			dbLayer.putItem(id, forum, TableName.FORUM.getName());
		} catch (CosmosException e) {
			// tambem tem de ver se o owner existe(Entity)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

	@GET
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Forum getForum(@PathParam("id") String id) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Forum.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.FORUM.getName());
		Forum forum = null;
		for (Object item : items) {
			forum = (Forum) item;
		}
		if (forum == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		return forum;
	}

	// ------------------------------------FORUM_MESSAGE---------------------//

	@POST
	@Path("/message/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addMessage(@PathParam("id") String id, ForumMessage message) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Forum.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.FORUM.getName());
		Forum forum = null;
		for (Object item : items) {
			forum = (Forum) item;
		}
		if (forum == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		else {

			ForumMessage[] arr = forum.getMessages();

			List<ForumMessage> arrlist = new ArrayList<ForumMessage>(Arrays.asList(arr));
			arrlist.add(message);

			arr = arrlist.toArray(arr);

			forum.setMessages(arr);

			try {
				dbLayer.putItem(id, forum, TableName.FORUM.getName());
			} catch (CosmosException e) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
		}
	}

	@PUT
	@Path("/message/{id}/{idMessage}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addReply(@PathParam("id") String id, @PathParam("idMessage") String idMessage, String reply) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Forum.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.FORUM.getName());
		Forum forum = null;
		for (Object item : items) {
			forum = (Forum) item;
		}
		if (forum == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		else {
			ForumMessage[] arr = forum.getMessages();

			List<ForumMessage> arrlist = new ArrayList<ForumMessage>(Arrays.asList(arr));

			for (ForumMessage message : arrlist) {
				if (message.getId().equals(idMessage)) {
					message.setReply(reply);
				}
			}

			try {
				dbLayer.putItem(id, forum, TableName.FORUM.getName());
			} catch (CosmosException e) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
		}
	}

}
