package edu.carleton.COMP4601.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

@Path("/sda")
public class Main {
	// Allows to insert contextual objects into the class,
		// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@GET
	public String printName() {
		return "COMP4601 Searchable Document Archive";
	}
}
