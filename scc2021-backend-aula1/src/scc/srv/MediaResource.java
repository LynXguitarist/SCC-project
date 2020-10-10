package scc.srv;

import scc.utils.Hash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/media")
public class MediaResource {

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public String upload(byte[] contents) {
		String id = Hash.of(contents);
		File file = new File(id);
		try {
			FileOutputStream fop = new FileOutputStream(file);
			fop.write(contents);
			fop.flush();
			fop.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return id;
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public byte[] download(@PathParam("id") String id) {
		File file = new File(id);
		return "Some text".getBytes();
	}

}
