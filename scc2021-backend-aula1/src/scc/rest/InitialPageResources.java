package scc.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import data.Entity;

@Path("/initial_page")
public class InitialPageResources {

	@GET
	@Path("/popular")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Entity getPopularServices() {

		return null;
	}
	
	@GET
	@Path("/recent/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Entity getRecentService() {

		return null;
	}
}
