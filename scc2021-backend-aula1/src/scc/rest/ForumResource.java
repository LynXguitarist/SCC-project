package scc.rest;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import cosmos.EntityDBLayer;
import cosmos.ForumDBLayer;
import data.Entity;
import data.Forum;
import data.ForumMessage;

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
        CosmosPagedIterable<Forum> items = ForumDBLayer.getInstance().getForumById(forum.getId());
        Forum ent = null;
        for (Forum item : items) {
            ent = item;
        }
        if (ent != null)
            throw new WebApplicationException(Response.Status.CONFLICT);

        CosmosItemResponse<Forum> cosmos_response = ForumDBLayer.getInstance().createForum(forum);
        int response = cosmos_response.getStatusCode();
        if (response != 200)
            throw new WebApplicationException(response);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateForum(@PathParam("id") String id, Forum forum) {
        ForumDBLayer dbLayer = ForumDBLayer.getInstance();
        CosmosPagedIterable<Forum> items = dbLayer.getForumById(id);
        Forum ent = null;
        for (Forum item : items) {
            ent = item;
        }
        if (ent == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        CosmosItemResponse<Forum> cosmos_response = dbLayer.putForum(ent);
        int response = cosmos_response.getStatusCode();
        if (response != 200)
            throw new WebApplicationException(response);
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Forum getForum(@PathParam("id") String id) {
        CosmosPagedIterable<Forum> items = ForumDBLayer.getInstance().getForumById(id);
        Forum entity = null;
        for (Forum item : items) {
            entity = item;
        }
        if (entity == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return entity;
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addMessage(@PathParam("id") String id, ForumMessage message) {
        CosmosPagedIterable<Forum> items = ForumDBLayer.getInstance().getForumById(id);
        Forum entity = null;
        for (Forum item : items) {
            entity = item;
        }
        if (entity == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        else {

            ForumMessage[] arr = entity.getMessages();

            List<ForumMessage> arrlist
                    = new ArrayList<ForumMessage>(
                    Arrays.asList(arr));
            arrlist.add(message);

            arr = arrlist.toArray(arr);

            entity.setMessages(arr);
        }
    }

    @PUT
    @Path("/{id}/{idMessage}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addReply(@PathParam("id") String id, @PathParam("idMessage") String idMessage, String reply) {
        CosmosPagedIterable<Forum> items = ForumDBLayer.getInstance().getForumById(id);
        Forum entity = null;
        for (Forum item : items) {
            entity = item;
        }
        if (entity == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        else {

            ForumMessage[] arr = entity.getMessages();

            List<ForumMessage> arrlist
                    = new ArrayList<ForumMessage>(
                    Arrays.asList(arr));

            for (ForumMessage message:
                    arrlist) {
                if(message.getId().equals(idMessage)){
                    message.setReply(reply);
                }
            }
        }
    }

}
