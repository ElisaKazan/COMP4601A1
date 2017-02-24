package edu.carleton.comp4601.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.carleton.comp4601.crawler.Crawler;
import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;
import edu.carleton.comp4601.dao.DocumentHelper;
import edu.carleton.comp4601.utility.SDAConstants;
import edu.carleton.comp4601.utility.SearchResult;
import edu.carleton.comp4601.utility.SearchServiceManager;


@Path("/sda")
public class Main {
	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	private static final String name = "COMP4601 Searchable Document Archive";

	public Main() {
		try {
			Crawler.crawl(new String[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@GET
	public String printName() {
		return name;
	}

	@GET
	@Produces(MediaType.TEXT_XML)
	public String sendXML() {
		return "<?xml version=\"1.0\"?>" + "<main> " + name + " </main>";
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String sendHTML() {
		return "<html> " + "<title>" + name + "</title>" + "<body><h1>" + name
				+ "</h1></body>" + "</html> ";
	}

	@GET
	@Path("documents")
	@Produces(MediaType.APPLICATION_XML)
	public List<Document> getDocumentsXML() {
		return DocumentCollection.getInstance().getDocuments();
	}

	@GET
	@Path("documents")
	@Produces(MediaType.TEXT_HTML)
	public String getDocumentsHTML() {
		return DocumentCollection.getInstance().displayDocList(DocumentCollection.getInstance().getDocuments());
	}

	@POST
	@Produces(MediaType.TEXT_HTML)
	@Consumes("application/x-www-form-urlencoded")
	public Response createDocument(MultivaluedMap<String, String> formParams) {
		if (!(formParams.containsKey("name") || formParams.containsKey("id") || 
				formParams.containsKey("tags") || formParams.containsKey("links") || 
				formParams.containsKey("text"))) {
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
		
		Document d = new Document(id);
		
		d.setName(name);
		d.setText(text);
		d.setLinks(links);
		d.setTags(tags);
		
		// Returns false for already existing Document
		if (!DocumentCollection.getInstance().add(d)) {
			// 204 from spec
			return Response.status(204).entity("Document with that ID already exists").build();
		}

		return Response.ok().entity("Created Document successfully!").build();
	}

	@Path("{id}")
	public DocumentAction getDocument(@PathParam("id") String id) {
		return new DocumentAction(uriInfo, request, id);
	}

	@Path("query/{tags}")
	public SearchAction getQuery(@PathParam("tags") String tags) {
		return new SearchAction(uriInfo, request, tags);
	}
	
	@Path("search/{tags}")
	@GET
	public String searchForDocs(@PathParam("tags") String tags) {
		SearchResult sr = SearchServiceManager.getInstance().query(tags);
		
		List<Document> results = DocumentCollection.getInstance().findAll(new SearchAction.TagSearchPredicate(tags.split(":")));
		
		try {
			sr.await(SDAConstants.TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			
		}
		
		results.addAll(sr.getDocs());
		
		return DocumentCollection.getInstance().displayDocList(results);
	}
	
    @Path("delete/{tags}")
	public DeleteAction getDelete(@PathParam("tags") String tags) {
		return new DeleteAction(uriInfo, request, tags);
	}
}
