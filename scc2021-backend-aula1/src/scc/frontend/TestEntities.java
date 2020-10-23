package scc.frontend;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;

import cosmos.EntityDBLayer;
import data.Entity;

public class TestEntities {
	public static void main(String[] args) {

		try {
			EntityDBLayer db = EntityDBLayer.getInstance();
			String id = "0" + System.currentTimeMillis();
			CosmosItemResponse<Entity> res = null;
			Entity ent = new Entity();
			ent.setId(id);
			ent.setName("SCC " + id);
			ent.setDescription("The best hairdresser");
			ent.setListed(true);
			ent.setLiked(true);
			ent.setForumId(id);
			ent.setNumberOfLikes(0);
			ent.setMediaIds(new String[] { "456" });
			ent.setCalendarIds(new String[] { "456" });

			//res = db.createEntity(ent);
			res = db.putEntity(ent);
			System.out.println("Put result");
			System.out.println(res.getStatusCode());
			System.out.println(res.getItem());

			System.out.println("Get for id = " + id);
			CosmosPagedIterable<Entity> resGet = db.getEntityById(id);
			for (Entity e : resGet) {
				System.out.println(e);
			}

			System.out.println("Get for all ids");
			resGet = db.getEntities();
			for (Entity e : resGet) {
				System.out.println(e);
			}
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
