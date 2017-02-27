package edu.carleton.comp4601.dao;

import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.mongodb.client.model.FindOneAndReplaceOptions;

import edu.carleton.comp4601.db.Database;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentCollection {
	private static DocumentCollection collection;
	@XmlElement(name="documents")
	private List<Document> documents;
	private static String DOC_COLLECTION_NAME = "documents";
	
	public interface DocumentPredicate {
		boolean matches(Document d);
	}
	
	public DocumentCollection() {
		documents = new ArrayList<Document>();
		loadAll();
	}
	
	public void loadAll() {
		for (org.bson.Document d : Database.getDB().getCollection(DOC_COLLECTION_NAME).find()) {
			d.put("id", d.get("_id"));
			documents.add(new Document(d));
		}
	}
	
	public void saveAll() {
		for (Document d : documents) {
			saveOne(d);
		}
	}
	
	public Document findOne(DocumentPredicate predicate) {
		List<Document> docs = findAll(predicate);
		
		if (docs.size() == 0) {
			return null;
		}
		
		return docs.get(0);
	}
	
	public Document findOne(int id) {
		return findOne(new DocumentPredicate() {
			
			@Override
			public boolean matches(Document d) {
				// TODO Auto-generated method stub
				return d.getId() == id;
			}
		});
	}
	
	public List<Document> findAll(DocumentPredicate predicate) {
		List<Document> docs = new ArrayList<Document>();
		for (Document d : documents) {
			if (predicate.matches(d)) {
				docs.add(d);
			}
		}
		
		return docs;
	}
	
	public void saveOne(Document d) {
		org.bson.Document converted = DocumentHelper.save(d);

		Database.getCollection(DOC_COLLECTION_NAME).findOneAndReplace(eq("_id", converted.get("_id")), converted, new FindOneAndReplaceOptions().upsert(true));
	}
	
	public void dbRemove(Document d) {
		Database.getCollection(DOC_COLLECTION_NAME).deleteOne(eq("_id", d.getId()));
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public static DocumentCollection getInstance() {
		if (collection == null) {
			collection = new DocumentCollection();
		}
		
		return collection;
	}

	public boolean add(Document d) {
		if (documents.contains(d)) {
			return false;
		}
		
		documents.add(d);

		saveOne(d);
		
		return true;
	}

	public boolean delete(int id) {
		Document d = findOne(id);
		if (d == null) {
			return false;
		}
		dbRemove(d);
		
		return documents.remove(d);
	}

	public boolean update(Document d) {
		if (!documents.contains(d)) {
			return false;
		}
		
		Document oldDoc = findOne(d.getId());
		
		oldDoc.setName(d.getName());
		oldDoc.setLinks(d.getLinks());
		oldDoc.setTags(d.getTags());
		oldDoc.setText(d.getText());
		
		saveOne(oldDoc);
		
		return true;
	}

	public String displayDocList(List<Document> docs) {
		String html = "<html> "+ "<title>" + "COMP4601 Searchable Document Archive" + "</title>" + "<body>";

		for(Document d : docs) {
			html += DocumentHelper.getDocFormat(d) + "<br>";
		}

		html += "</body>" + "</html> ";

		return html;
	}
}