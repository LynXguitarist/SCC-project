package scc.rest;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.util.CosmosPagedIterable;
import cosmos.CosmosDBLayer;
import data.Entity;
import data.Forum;
import data.ForumMessage;
import scc.utils.TableName;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Path("/forum")
public class ForumResource {

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createForum(Forum forum) {
        if (!ownerExists(forum.getOwnerId())) {
            CosmosDBLayer<?> dbLayerForum = CosmosDBLayer.getInstance(Forum.class);
            try {
                forum.setId(UUID.randomUUID().toString());
                dbLayerForum.createItem(forum, TableName.FORUM.getName());
            } catch (CosmosException e) {
                throw new WebApplicationException(Response.Status.CONFLICT);
            }
        } else {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateForum(@PathParam("id") String id, Forum forum) {
        CosmosDBLayer<?> dbLayerForum = CosmosDBLayer.getInstance(Forum.class);

        if (ownerExists(forum.getOwnerId())) {
            try {
                dbLayerForum.putItem(id, forum, TableName.FORUM.getName());
            } catch (CosmosException e) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        } else {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Forum getForum(@PathParam("id") String id) {
        CosmosDBLayer<?> dbLayerForum = CosmosDBLayer.getInstance(Forum.class);
        CosmosPagedIterable<?> items = dbLayerForum.getItemById(id, TableName.FORUM.getName());
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

        if (getForum(id) != null) {
            CosmosDBLayer<?> dbLayerMessages = CosmosDBLayer.getInstance(ForumMessage.class);
            String messageId = UUID.randomUUID().toString();
            CosmosDBLayer<?> dbLayerForum = CosmosDBLayer.getInstance(Forum.class);
            CosmosPagedIterable<?> items = dbLayerForum.getItemById(id, TableName.FORUM.getName());
            Forum forum = null;
            for (Object item : items) {
                forum = (Forum) item;
            }

            try {
                message.setId(messageId);
                message.setReply(null);
                message.setForumId(id);
                message.setReplyTime(null);
                dbLayerMessages.createItem(message, TableName.FORUMMESSAGE.getName());
            } catch (CosmosException e) {
                throw new WebApplicationException(Response.Status.CONFLICT);
            }

            if (forumMessageExists(messageId)) {
                String[] arr = forum.getMessageIds();

                List<String> arrlist = new ArrayList<String>(Arrays.asList(arr));
                arrlist.add(messageId);

                arr = arrlist.toArray(arr);

                forum.setMessages(arr);

                try {
                    dbLayerForum.putItem(id, forum, TableName.FORUM.getName());
                } catch (CosmosException e) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
            }
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @GET
    @Path("/message/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ForumMessage getForumMessage(@PathParam("id") String id) {
        CosmosDBLayer<?> dbLayerMessage = CosmosDBLayer.getInstance(ForumMessage.class);
        CosmosPagedIterable<?> items = dbLayerMessage.getItemById(id, TableName.FORUMMESSAGE.getName());
        ForumMessage message = null;
        for (Object item : items) {
            message = (ForumMessage) item;
        }
        if (message == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        return message;
    }

    @PUT
    @Path("/message/{id}/{idMessage}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void addReply(@PathParam("id") String id, @PathParam("idMessage") String idMessage, String reply) {

        if (getForum(id) != null) {

            ForumMessage message = getForumMessage(idMessage);

            if (message != null) {
                message.setReply(reply);
                message.setReplyTime(LocalDateTime.now(ZoneOffset.UTC).toString());

                CosmosDBLayer<?> dbLayerMessages = CosmosDBLayer.getInstance(ForumMessage.class);
                dbLayerMessages.putItem(id,message, TableName.FORUMMESSAGE.getName());
            }else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
//            if (forumMessageExists(idMessage)) {
//                //Só existe uma resposta e será do owner
//                if (!forumMessageReplyFromOwnerExists(idMessage)) {
//                    CosmosDBLayer<?> dbLayerMessages = CosmosDBLayer.getInstance(ForumMessage.class);
//                    String messageId = UUID.randomUUID().toString();
//
//                    try {
//                        message.setId(messageId);
//                        dbLayerMessages.createItem(message, TableName.FORUMMESSAGE.getName());
//                    } catch (CosmosException e) {
//                        throw new WebApplicationException(Response.Status.CONFLICT);
//                    }
//                } else {
//                    throw new WebApplicationException(Response.Status.CONFLICT);
//                }
//            } else {
//                throw new WebApplicationException(Response.Status.NOT_FOUND);
//            }
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    private boolean ownerExists(String id) {
        CosmosDBLayer<?> dbLayerEntity = CosmosDBLayer.getInstance(Entity.class);
        CosmosPagedIterable<?> items = dbLayerEntity.getItemById(id, TableName.ENTITY.getName());
        Entity entity = null;
        for (Object item : items) {
            entity = (Entity) item;
        }
        if (entity == null)
            return false;
        else {
            return true;
        }
    }

    private boolean forumMessageExists(String id) {
        CosmosDBLayer<?> dbLayerMessage = CosmosDBLayer.getInstance(ForumMessage.class);
        CosmosPagedIterable<?> items = dbLayerMessage.getItemById(id, TableName.FORUMMESSAGE.getName());
        ForumMessage forumMessage = null;
        for (Object item : items) {
            forumMessage = (ForumMessage) item;
        }
        if (forumMessage == null)
            return false;
        else {
            return true;
        }
    }

    private boolean forumMessageReplyFromOwnerExists(String id) {
        CosmosDBLayer<?> dbLayerMessage = CosmosDBLayer.getInstance(ForumMessage.class);
        CosmosPagedIterable<?> items = dbLayerMessage.getItemsBySpecialQuery("SELECT * FROM " + TableName.FORUMMESSAGE.getName() +
                " WHERE " + TableName.FORUMMESSAGE.getName() + ".id=\"" + id + "\" AND" + TableName.FORUMMESSAGE.getName() + ".reply IS NULL" +
                " OR " + TableName.FORUMMESSAGE.getName() + ".reply=\"\"", TableName.FORUMMESSAGE.getName());
        ForumMessage forumMessage = null;
        for (Object item : items) {
            forumMessage = (ForumMessage) item;
        }
        if (forumMessage == null)
            return false;
        else {
            return true;
//            if (forumMessage.getReply() == null || !forumMessage.getReply().equals("")) {
//                return true;
//            } else {
//                return false;
//            }
        }
    }

}
