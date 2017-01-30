package edu.carleton.COMP4601.resources;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
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
	
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteDocument() {
		if (!DocumentCollection.getInstance().delete(id)) {
			return Response.status(204).entity("Document not found.").build();
		}
		
		return Response.ok().entity("Document deleted successfully.").build();
	}
	
	@PUT
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("application/x-www-form-urlencoded")
	public Response updateDocPut(MultivaluedMap<String, String> formParams) {
		return updateDocPostOrPut(formParams);
	}
	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("application/x-www-form-urlencoded")
	public Response updateDocPost(MultivaluedMap<String, String> formParams) {
		return updateDocPostOrPut(formParams);
	}
	
	private Response updateDocPostOrPut(MultivaluedMap<String, String> formParams) {
		if (!(formParams.containsKey("name") || formParams.containsKey("id") || 
				formParams.containsKey("tags") || formParams.containsKey("links") || formParams.containsKey("text"))) {
			return Response.status(204).entity("Not all necessary parameters provided.").build();
		}

		ArrayList<String> tags = new ArrayList<String>();
		tags.addAll(formParams.get("tags"));
		ArrayList<String> links = new ArrayList<String>();
		links.addAll(formParams.get("links"));
		String name = formParams.getFirst("name");
		String text = formParams.getFirst("text");
		int id;
		try {
			id = new Integer(formParams.getFirst("id")).intValue();
		} catch (NumberFormatException e) {
			return Response.status(204).entity("Id must be an integer").build();
		}

		Document d = new Document(id, name, text, tags, links);

		if (!DocumentCollection.getInstance().update(d)) {
			return Response.status(204).entity("Cannot update doc that doesn't exist.").build();
		}

		return Response.ok().entity("Document updated successfully.").build();
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getDocumentInfo() {
		Document doc = DocumentCollection.getInstance().findOne(id);

		if (doc == null) {
			return Response.status(404).entity("Document not Found.").build();
		}

		return Response.ok().entity("<html><body>" + doc.getDocFormatLong() + "</body></html>").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Document getDocumentXML() {
		Document doc = DocumentCollection.getInstance().findOne(id);
		
		return doc;
	}
}
