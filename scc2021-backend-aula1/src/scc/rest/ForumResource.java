package scc.rest;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import cosmos.CosmosDBLayer;
import data.Forum;
import data.ForumMessage;
import scc.utils.TableName;

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
			// TODO tambem tem de ver se o owner existe(Entity)
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
			// TODO tambem tem de ver se o owner existe(Entity)
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

	@PUT
	@Path("/message/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addMessage(@PathParam("id") String id, ForumMessage message) {
		CosmosDBLayer<?> dbLayerForum = CosmosDBLayer.getInstance(Forum.class);
		CosmosDBLayer<?> dbLayerMessages = CosmosDBLayer.getInstance(ForumMessage.class);
		CosmosPagedIterable<?> items = dbLayerForum.getItemById(id, TableName.FORUM.getName());
		Forum forum = null;
		for (Object item : items) {
			forum = (Forum) item;
		}
		if (forum == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		else {
			CosmosItemResponse<ForumMessage> response = null;
			try {
				response = dbLayerMessages.createItem(message, TableName.FORUMMESSAGES.getName());
			} catch (CosmosException e) {
				// TODO tambem tem de ver se o owner existe(Entity)
				throw new WebApplicationException(Response.Status.CONFLICT);
			}

			String[] arr = forum.getMessageIds();

			List<String> arrlist = new ArrayList<String>(Arrays.asList(arr));
			arrlist.add(response.getItem().getId());

			arr = arrlist.toArray(arr);

			forum.setMessages(arr);

			try {
				dbLayerForum.putItem(id, forum, TableName.FORUM.getName());
			} catch (CosmosException e) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
		}
	}

	@PUT
	@Path("/message/{id}/{idMessage}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void addReply(@PathParam("id") String id, @PathParam("idMessage") String idMessage, ForumMessage message) {
		CosmosDBLayer<?> dbLayer = CosmosDBLayer.getInstance(Forum.class);
		CosmosPagedIterable<?> items = dbLayer.getItemById(id, TableName.FORUM.getName());
		Forum forum = null;
		for (Object item : items) {
			forum = (Forum) item;
		}
		if (forum == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		else {
			String[] arr = forum.getMessageIds();

			List<String> arrlist = new ArrayList<String>(Arrays.asList(arr));
			arrlist.add(idMessage);
			//TODO create new forummessage entry anch check if original exists
			arr = arrlist.toArray(arr);

			forum.setMessages(arr);

			try {
				dbLayer.putItem(id, forum, TableName.FORUM.getName());
			} catch (CosmosException e) {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
		}
	}

}
