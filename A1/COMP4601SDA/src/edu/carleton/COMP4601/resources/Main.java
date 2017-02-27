package edu.carleton.comp4601.resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.spi.resource.Singleton;

import edu.carleton.comp4601.crawler.Crawler;
import edu.carleton.comp4601.dao.Document;
import edu.carleton.comp4601.dao.DocumentCollection;
import edu.carleton.comp4601.db.Database;
import edu.carleton.comp4601.utility.SDAConstants;
import edu.carleton.comp4601.utility.SearchResult;
import edu.carleton.comp4601.utility.SearchServiceManager;

@Singleton
@Path("/sda")
public class Main {
	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo

	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	private static final String name = "COMP4601 Searchable Document Archive V2.1: Elisa Kazan and Jack McCracken";
	public static final String INDEX_DIR = "/Users/jackmccracken/index";

	public Main() {
		System.out.println("Hello!");
		try {
			SearchServiceManager.getInstance().register();
		} catch (IOException e) {
			System.out.println("Exception :(: " + e.getMessage());
		}
		
		DocumentCollection.getInstance();
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
	@GET
	public String getQuery(@PathParam("tags") String tags) {
		return DocumentCollection.getInstance().displayDocList(doQuery(tags));
	}
	
	@Path("search/{tags}")
	@GET
	public String searchForDocs(@PathParam("tags") String tags) {
		SearchResult sr = SearchServiceManager.getInstance().query(tags);
		
		List<Document> results = doQuery(tags);
		
		try {
			sr.await(SDAConstants.TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			
		}
		
		results.addAll(sr.getDocs());
		
		return DocumentCollection.getInstance().displayDocList(results);
	}
	
	
	public List<Document> doQuery(String query) {
		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Crawler.INDEX_DIR).toPath()));
			
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer();
			
			QueryParser parser = new QueryParser("contents", analyzer);
			
			Query q = parser.parse(query);
			TopDocs hitsDoc = searcher.search(q, 100);
			
			ScoreDoc[] hits = hitsDoc.scoreDocs;
			
			
			List<Document> docs = new ArrayList<Document>();
			
			for (ScoreDoc doc : hits) {
				org.apache.lucene.document.Document d = searcher.doc(doc.doc);
				
				String id = d.get("docId");
				
				edu.carleton.comp4601.dao.Document newDoc = DocumentCollection.getInstance().findOne(Integer.valueOf(id));

				newDoc.setScore(doc.score);
				docs.add(newDoc);
			}
			reader.close();
		
			return docs;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void indexDb() throws IOException {
		FSDirectory dir = FSDirectory.open(new File(INDEX_DIR).toPath());
		
		Analyzer analyzer = new StandardAnalyzer();
		
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, config);

		for (Document doc : DocumentCollection.getInstance().getDocuments()) {
			org.apache.lucene.document.Document docLucene = new org.apache.lucene.document.Document();
			Field idField = new TextField("docId", String.valueOf(doc.getId()), Store.YES);
			Field urlField = new StringField("url", doc.getUrl(), Store.YES);
			Field contentField = new TextField("contents", doc.getText(), Store.YES);
			Field indexedByField = new TextField("i", String.join(" ", doc.getTags()), Store.YES);
			indexedByField.setBoost(2.0f);

			docLucene.add(idField);
			docLucene.add(urlField);
			docLucene.add(contentField);
			docLucene.add(indexedByField);
			writer.addDocument(docLucene);
		}

		writer.close();
	}
	
    @Path("delete/{tags}")
	public DeleteAction getDelete(@PathParam("tags") String tags) {
		return new DeleteAction(uriInfo, request, tags);
	}
    
    @GET
    @Path("reindex")
    public String reindex() {
    	try {
			indexDb();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Index failed: " + e.getMessage();
		}
    	return "Index successful";
    }
    
    @GET
    @Path("crawl")
    public Response crawl(@QueryParam("url") String url) {
    	try {
			Crawler.crawl(url);
			System.out.println("Starting index");
			indexDb();
			System.out.println("Created index");
		} catch (Exception e) {
			return Response.status(500).entity("Crawl failed " + e.getMessage()).build();
		}
    	return Response.ok("Crawl successful!").build();
    }
    
    @GET
    @Path("reset")
    public String reset() {
    	Database.getCollection("documents").drop();
    	try {
			FileUtils.deleteDirectory(new File(INDEX_DIR));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Reset unsuccessful";
		}
    	
    	return "Reset successful";
    }

    @GET
    @Path("list")
    public String list() {
    	String list = SearchServiceManager.list();
    	String render = "";
    	try {
			JSONArray arr = new JSONArray(list);
			
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = arr.getJSONObject(i);
				
				render += obj.getString("name");
				render += ", ";
			}
		} catch (JSONException e) {
			return "Failed to parse JSON: " + e.getMessage();
		}
    	
    	
    	return render;
    }
    
    @GET
    @Path("noboost")
    public String noboost() throws IOException {
    	reindex();
		
		return "Noboost successful";
    }
}
