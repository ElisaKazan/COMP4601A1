package edu.carleton.COMP4601.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import edu.carleton.COMP4601.dao.Document;
import edu.carleton.COMP4601.dao.DocumentCollection;


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
		DocumentCollection documents = DocumentCollection.getInstance();
		String html = "<html> "+ "<title>" + name + "</title>" + "<body>";

		for(Document d : documents.getDocuments()) {
			html += d.getDocFormat() + "<br>";
		}

		html += "</body>" + "</html> ";

		return html;
	}

	@Path("{id}")
	public DocumentAction getDocument(@PathParam("id") String id) {
		return new DocumentAction(uriInfo, request, id);
	}
}
