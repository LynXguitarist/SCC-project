package scc.rest;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cosmos.CosmosDBLayer;
import data.Entity;
import data.Forum;
import data.ForumMessage;
import scc.redis.RedisCache;
import scc.utils.AdvancedFeatures;
import scc.utils.TableName;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/forum")
public class ForumResource {

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createForum(Forum forum) {
        if (ownerExists(forum.getOwnerId())) {
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
                forum.setId(id);
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
        boolean hasCache = Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.REDIS));
        String cacheItem = new String();
        String key = TableName.FORUM.getName() + id;
        Forum forum = null;

        if (hasCache)
            cacheItem = RedisCache.getCache().getItemFromCache(key);
        if (cacheItem == null || !hasCache) {
            forum = getForumFromDB(id);
            if (hasCache) {
                RedisCache.getCache().addItemToCache(key, forum, 120);
            }
        } else { //retrieves from cache
            ObjectMapper mapper = new ObjectMapper();
            try {
                Forum cal = mapper.readValue(cacheItem, Forum.class);
                forum = cal;
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
        }

        return forum;
    }

    @GET
    @Path("/entity/{id}/forums")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Forum> getEntityForums(@PathParam("id") String id) {
        boolean hasCache = Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.REDIS));
        List<String> values = new ArrayList<>();
        String key = TableName.ENTITY.getName() + "-" + TableName.FORUM.getName() + id;
        List<Forum> list = new ArrayList<>();

        if (hasCache)
            values = RedisCache.getCache().getListFromCache(key);
        if (values.isEmpty() || !hasCache) {
            CosmosDBLayer<?> dbLayerForum = CosmosDBLayer.getInstance(Forum.class);
            String query = "SELECT * FROM " + TableName.FORUM.getName() + " e WHERE e.ownerId=\"" + id + "\"";
            CosmosPagedIterable<?> items = dbLayerForum.getItemsBySpecialQuery(query, TableName.FORUM.getName());

            for (Object item : items) {
                Forum forum = (Forum) item;
                list.add(forum);
            }
            if (list.isEmpty())
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            if (hasCache) {
                RedisCache.getCache().addListToCache(key, list, 120);
            }
        } else { //retrieves from cache
            for (String v : values) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Forum e = mapper.readValue(v, Forum.class);
                    list.add(e);
                } catch (JsonProcessingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }

        return list;
    }

    // ------------------------------------FORUM_MESSAGE---------------------//

    @PUT
    @Path("/message/{forumId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void addMessage(@PathParam("forumId") String forumId, ForumMessage message) {

        Forum forum = getForumFromDB(forumId);
        if (forum != null) {
            CosmosDBLayer<?> dbLayerMessages = CosmosDBLayer.getInstance(ForumMessage.class);
            String messageId = UUID.randomUUID().toString();

            try {
                message.setId(messageId);
                message.setReply(null);
                message.setForumId(forumId);
                message.setReplyTime(null);
                dbLayerMessages.createItem(message, TableName.FORUMMESSAGE.getName());
            } catch (CosmosException e) {
                throw new WebApplicationException(Response.Status.CONFLICT);
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
        ForumMessage message = null;

        boolean hasCache = Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.REDIS));
        String cacheItem = new String();
        String key = TableName.FORUMMESSAGE.getName() + id;

        if (hasCache) {
            cacheItem = RedisCache.getCache().getItemFromCache(key);
        }
        if (cacheItem == null || !hasCache) {
           message = getForumMessageFromDB(id);
            if (hasCache) {
                RedisCache.getCache().addItemToCache(key, message, 120);
            }

        } else { //retrieves from cache
            ObjectMapper mapper = new ObjectMapper();
            try {
                ForumMessage msg = mapper.readValue(cacheItem, ForumMessage.class);
                message = msg;
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
        }
        return message;

    }

    @GET
    @Path("/{forumId}/message")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ForumMessage> getForumMessages(@PathParam("forumId") String forumId) {
        List<ForumMessage> list = new ArrayList<>();

        boolean hasCache = Boolean.parseBoolean(AdvancedFeatures.getProperty(AdvancedFeatures.REDIS));
        List<String> values = new ArrayList<>();
        String key = TableName.FORUMMESSAGE.getName() + "-" + TableName.FORUM.getName() + forumId;

        if (hasCache) {
            values = RedisCache.getCache().getListFromCache(key);
        }

        if (values.isEmpty() || !hasCache) {
            String query = "SELECT * FROM " + TableName.FORUMMESSAGE.getName() + " f WHERE f.forumId=\"" + forumId + "\"";
            CosmosDBLayer<?> dbLayerMessages = CosmosDBLayer.getInstance(ForumMessage.class);
            CosmosPagedIterable<?> items = dbLayerMessages.getItemsBySpecialQuery(query, TableName.FORUMMESSAGE.getName());

            for (Object item : items) {
                ForumMessage forumMessage = (ForumMessage) item;
                list.add(forumMessage);
            }

            if (list.isEmpty())
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            if (hasCache) {
                RedisCache.getCache().addListToCache(key, list, 120);
            }

        } else { //retrieves from cache
            for (String v : values) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    ForumMessage e = mapper.readValue(v, ForumMessage.class);
                    list.add(e);
                } catch (JsonProcessingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        return list;

    }

    @POST
    @Path("/message/{id}/{idMessage}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void addReply(@PathParam("id") String id, @PathParam("idMessage") String idMessage, String reply) {

        if (getForumFromDB(id) != null) {

            ForumMessage message = getForumMessageFromDB(idMessage);

            if (message != null) {
                message.setReply(reply);
                message.setReplyTime(LocalDateTime.now(ZoneOffset.UTC).toString());

                CosmosDBLayer<?> dbLayerMessages = CosmosDBLayer.getInstance(ForumMessage.class);
                dbLayerMessages.putItem(idMessage, message, TableName.FORUMMESSAGE.getName());
            } else {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
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
        }
    }

    private Forum getForumFromDB(String id) {

        Forum forum = null;

        CosmosDBLayer<?> dbLayerForum = CosmosDBLayer.getInstance(Forum.class);
        CosmosPagedIterable<?> items = dbLayerForum.getItemById(id, TableName.FORUM.getName());

        for (Object item : items) {
            forum = (Forum) item;
        }
        if (forum == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);


        return forum;
    }

    public ForumMessage getForumMessageFromDB(String id) {
        ForumMessage message = null;

        CosmosDBLayer<?> dbLayerMessage = CosmosDBLayer.getInstance(ForumMessage.class);
        CosmosPagedIterable<?> items = dbLayerMessage.getItemById(id, TableName.FORUMMESSAGE.getName());

        for (Object item : items) {
            message = (ForumMessage) item;
        }
        if (message == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);


        return message;

    }

}
