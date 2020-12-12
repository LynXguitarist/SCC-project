package mongoDB;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import data.Entity;
import scc.utils.AzureProperties;
import scc.utils.TableName;

public class MongoDBLayer {

	private MongoClient mongoClient;
	private MongoDatabase db;

	public MongoDBLayer() {
		mongoClient = MongoClients.create();
		db = mongoClient.getDatabase(AzureProperties.getProperty(AzureProperties.COSMOSDB_DATABASE));
	}

	public void addItem(String tableName, Entity entity) {
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());
		Document document = new Document();
		document.append("id", entity.getId());
		document.append("name", entity.getName());
		document.append("description", entity.getDescription());
		document.append("mediaIds", entity.getMediaIds());
		document.append("numberOfLikes", entity.getNumberOfLikes());
		document.append("deleted", entity.isDeleted());
		document.append("deletionDate", entity.getDeletionDate());

		table.insertOne(document);
	}

	public void updateItem(String tableName, Entity entity) {
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		Document newDocument = new Document();
		newDocument.append("id", entity.getId());
		newDocument.append("name", entity.getName());
		newDocument.append("description", entity.getDescription());
		newDocument.append("mediaIds", entity.getMediaIds());
		newDocument.append("numberOfLikes", entity.getNumberOfLikes());
		newDocument.append("deleted", entity.isDeleted());
		newDocument.append("deletionDate", entity.getDeletionDate());

		table.updateOne(Filters.eq("id", entity.getId()), newDocument);
	}

	public void deleteItem(String tableName, String id) {
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		table.deleteOne(Filters.eq("id", id));
	}

	public List<Entity> getItems(String tableName) {
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Entity> items = table.find(Entity.class).into(new ArrayList<Entity>());
		return items;
	}

	public Entity getItemById(String tableName, String id) {
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());

		List<Entity> item = table.find(Filters.eq("id", id), Entity.class).into(new ArrayList<Entity>());
		return item.get(0);
	}
	
	public List<Entity> getItemsBySpecialQuery(String tableName, Bson query) {
		MongoCollection<Document> table = db.getCollection(TableName.valueOf(tableName).getName());
		
		List<Entity> item = table.find(query, Entity.class).into(new ArrayList<Entity>());
		return item;
	}

}
