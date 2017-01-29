package edu.carleton.COMP4601.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.carleton.COMP4601.dao.Document;
import edu.carleton.COMP4601.dao.DocumentCollection;

public class DocumentAction {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	int id;

	public DocumentAction(UriInfo uriInfo, Request request, String id) {
		this.uriInfo = uriInfo;
		this.request = request;
		try {
			this.id = new Integer(id).intValue();
		} catch (NumberFormatException e) {
			this.id = -1; // TODO: This will cause a Document Not Found error. Should we do something better?
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getDocumentInfo() {
		StringBuilder builder = new StringBuilder("<html><body>");
		
		Document doc = DocumentCollection.getInstance().findOne(id);
		
		if (doc == null) {
			return Response.status(404).entity("Document not Found.").build();
		}
		
		builder.append("<ul>");
		
		builder.append("<li>Name: " + doc.getName() + "</li>");

		builder.append("<li>ID: " + doc.getId() + "</li>");
		
		builder.append("<li>Tags: </li>");
		builder.append("<ul>");
		
		for (String tag : doc.getTags()) {
			builder.append("<li>" + tag + "</li>");
		}
		
		builder.append("</ul>");
		
		builder.append("<li>Links: </li>");
		builder.append("<ul>");
		
		for (String link : doc.getLinks()) {
			builder.append("<li> <a href=\"" + link + "\">" + link + "</a></li>");
		}

		builder.append("</ul>");

		builder.append("</ul>");
		
		builder.append("</body></html>");
		
		return Response.ok(builder.toString()).build();
	}
}
