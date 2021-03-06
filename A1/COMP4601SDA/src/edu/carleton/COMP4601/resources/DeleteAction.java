package edu.carleton.comp4601.resources;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;
import edu.carleton.comp4601.exceptions.DocumentNotFoundException;

public class DeleteAction {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	String[] tags;
	
	private class TagSearchPredicate implements DocumentCollection.DocumentPredicate {
		@Override
		public boolean matches(Document d) {
			for (String tag : tags) {
				if (!d.getTags().contains(tag)) {
					return false;
				}
			}

			return true;
		}
	}

	public DeleteAction(UriInfo uriInfo, Request request, String tags) {
		this.uriInfo = uriInfo;
		this.request = request;
		
		// : separated tags.
		this.tags = tags.split(":");
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response doDelete() throws DocumentNotFoundException {
		List<Document> documents = DocumentCollection.getInstance().findAll(new TagSearchPredicate());
		
		// TODO: This doesn't quite work. Needs to be discovered by jersey.
		if (documents.size() == 0) {
			throw new DocumentNotFoundException(true);
		}
		
		for (Document d : documents) {
			DocumentCollection.getInstance().delete(d.getId());
		}
		return Response.ok().entity("Deleted documents successfully.").build();
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	public List<Document> doDeleteXML() throws DocumentNotFoundException {
		List<Document> documents = DocumentCollection.getInstance().findAll(new TagSearchPredicate());
		
		if (documents.size() == 0) {
			throw new DocumentNotFoundException(true);
		}
		
		return documents;
	}
}
