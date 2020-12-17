package mongoDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import data.Calendar;
import data.Entity;
import data.Forum;
import scc.utils.AzureProperties;
import scc.utils.TableName;

public class MongoDBLayer {

	private static MongoDBLayer instance;
	private MongoClient mongoClient;
	private MongoDatabase db;

	public MongoDBLayer() {
		mongoClient = MongoClients.create();
		// need to create db???
		db = mongoClient.getDatabase(AzureProperties.getProperty(AzureProperties.COSMOSDB_DATABASE));
	}

	public static synchronized MongoDBLayer getInstance() {
		Locale.setDefault(Locale.US);
		if (instance != null)
			return instance;

		instance = new MongoDBLayer();
		return instance;
	}
	
	/*-----------------------------------General------------------------*/
	
	public void addItem(String tableName, Document document) {
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());
		table.insertOne(document);
	}

	public void updateItem(String tableName, String id, Document newDocument) {
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		table.updateOne(Filters.eq("id", id), newDocument);
	}

	public void deleteItem(String id, String tableName) {
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		table.deleteOne(Filters.eq("id", id));
	}

	/*-----------------------------------Entity------------------------*/

	public List<Entity> getEntities() {
		String tableName = TableName.ENTITY.getName();
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Entity> items = table.find(Entity.class).into(new ArrayList<Entity>());
		return items;
	}

	public Entity getEntityById(String id) {
		String tableName = TableName.ENTITY.getName();
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Entity> item = table.find(Filters.eq("id", id), Entity.class).into(new ArrayList<Entity>());
		return item.get(0);
	}

	public List<Entity> getEntitiesBySpecialQuery(Bson query) {
		String tableName = TableName.ENTITY.getName();
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Entity> item = table.find(query, Entity.class).into(new ArrayList<Entity>());
		return item;
	}

	/*-----------------------------------Forum------------------------*/

	public List<Forum> getForums() {
		String tableName = TableName.FORUM.getName();
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Forum> items = table.find(Forum.class).into(new ArrayList<Forum>());
		return items;
	}

	public Forum getForumById(String id) {
		String tableName = TableName.FORUM.getName();
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Forum> item = table.find(Filters.eq("id", id), Forum.class).into(new ArrayList<Forum>());
		return item.get(0);
	}

	public List<Forum> getForumsBySpecialQuery(Bson query) {
		String tableName = TableName.FORUM.getName();
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Forum> item = table.find(query, Forum.class).into(new ArrayList<Forum>());
		return item;
	}

	/*-----------------------------------Calendar------------------------*/
	public List<Calendar> getCalendars() {
		String tableName = TableName.CALENDAR.getName();
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Calendar> items = table.find(Calendar.class).into(new ArrayList<Calendar>());
		return items;
	}

	public Calendar getCalendarById(String id) {
		String tableName = TableName.CALENDAR.getName();
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Calendar> item = table.find(Filters.eq("id", id), Calendar.class).into(new ArrayList<Calendar>());
		return item.get(0);
	}

	public List<Calendar> getCalendarsBySpecialQuery(Bson query) {
		String tableName = TableName.CALENDAR.getName();
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Calendar> item = table.find(query, Calendar.class).into(new ArrayList<Calendar>());
		return item;
	}
}
