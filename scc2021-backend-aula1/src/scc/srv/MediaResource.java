package scc.srv;

import scc.utils.Hash;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import java.io.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/media")
public class MediaResource {

	// BLOB CONNECTION
	private static final String CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sc42764;AccountKey=A/ACsdA6CYx4UrS0D2Z329z9AykGT2MJicbcxqPbH/fTnEXKVKxJLhE4csZyfsWKHZpLG4cchjspctwEMBq+oA==;EndpointSuffix=core.windows.net";
	private static CloudBlobContainer container;

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		String id = Hash.of(contents);

		if (container == null)
			connections();
		try {
			// Get reference to blob
			CloudBlob blob = container.getBlockBlobReference(id);
			// Upload contents from byte array (check documentation for other alternatives)
			blob.uploadFromByteArray(contents, 0, contents.length);
		} catch (StorageException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		// Get reference to blob
		CloudBlob blob;
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		if (container == null)
			connections();
		try {
			blob = container.getBlobReferenceFromServer(id);
			blob.download(out);
			out.close();
		} catch (URISyntaxException | StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] contents = out.toByteArray();
		return contents;
	}

	// ---------------------------Connections-----------------------------//

	private synchronized void connections() {
		// Get connection string in the storage access keys page
		String storageConnectionString = CONNECTION_STRING;
		CloudStorageAccount storageAccount;
		try {
			storageAccount = CloudStorageAccount.parse(storageConnectionString);
			CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
			container = blobClient.getContainerReference("images");
		} catch (InvalidKeyException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// -------------------------------Cosmo_Client------------------------------//


	/*
	private Entity writeItem(Entity entity) {
		CosmosItemResponse<Entity> res = entities.createItem(entity);
		if (res.getStatusCode() < 300)
			return res.getItem();
		else
			throw new NotFoundException();
	}

	private void readItems(String id) {
		CosmosPagedIterable<Entity> res = entities.queryItems("SELECT * FROM entities WHERE entities.id=\"" + id + "\"",
				new CosmosQueryRequestOptions(), Entity.class);
		for (Entity e : res) {
			System.out.println(e);
		}
	}
	*/

}
