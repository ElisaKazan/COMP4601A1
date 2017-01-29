package edu.carleton.COMP4601.dao;

import static com.mongodb.client.model.Filters.eq;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.mongodb.client.model.FindOneAndReplaceOptions;

import edu.carleton.COMP4601.dao.Document;
import edu.carleton.COMP4601.db.Database;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentCollection {
	private DocumentCollection collection;
	@XmlElement(name="documents")
	private List<Document> documents;
	private static String DOC_COLLECTION_NAME = "documents";
	
	public DocumentCollection() {
		loadAll();
	}
	
	public void loadAll() {
		for (org.bson.Document d : Database.getDB().getCollection(DOC_COLLECTION_NAME).find()) {
			documents.add(new Document(d));
		}
	}
	
	public void saveAll() {
		for (Document d : documents) {
			saveOne(d);
		}
	}
	
	public void saveOne(Document d) {
		org.bson.Document converted = d.save();

		Database.getCollection(DOC_COLLECTION_NAME).findOneAndReplace(eq("_id", converted.get("_id")), converted, new FindOneAndReplaceOptions().upsert(true));
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public DocumentCollection getInstance() {
		if (collection == null) {
			collection = new DocumentCollection();
		}
		
		return collection;
	}
}