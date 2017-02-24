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

public class SearchAction {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	String[] tags;
	
	public static class TagSearchPredicate implements DocumentCollection.DocumentPredicate {
		String[] tags;

		public TagSearchPredicate(String[] tags) {
			this.tags = tags;
		}
		
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

	public SearchAction(UriInfo uriInfo, Request request, String tags) {
		this.uriInfo = uriInfo;
		this.request = request;
		
		// : separated tags.
		this.tags = tags.split(":");
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response doSearch() throws DocumentNotFoundException {
		List<Document> documents = DocumentCollection.getInstance().findAll(new TagSearchPredicate(tags));
		
		if (documents.size() == 0) {
			return Response.status(204).entity("Documents not found.").build();
		}
		
		return Response.ok().entity(DocumentCollection.getInstance().displayDocList(documents)).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public List<Document> doSearchXML() throws DocumentNotFoundException {
		List<Document> documents = DocumentCollection.getInstance().findAll(new TagSearchPredicate(tags));
		
		if (documents.size() == 0) {
			return null;
		}
		
		return documents;
	}
}
