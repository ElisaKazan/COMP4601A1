package edu.carleton.COMP4601.resources;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

public class DocumentAction {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	String id;

	public DocumentAction(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.id = id;
	}
}
