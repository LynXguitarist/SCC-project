package scc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/forum")
public class ForumResource {

	/**
	 * Vai ter de ter uma entity para Forum (CosmoDB)
	 */
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addMessage() {

	}
	
	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addReply() {
		
	}
	
}
