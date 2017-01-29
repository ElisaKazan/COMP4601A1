package edu.carleton.COMP4601.dao;

import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Document {
	private Integer id;
	private Float score;
	private String name;
	private String text;
	private ArrayList<String> tags;
	private ArrayList<String> links;

	public Document() {
		tags = new ArrayList<String>();
		links = new ArrayList<String>();
	}

	public Document(Integer id) {
		this();
		this.id = id;
	}
	
	@SuppressWarnings("unchecked")
	public Document(org.bson.Document doc) {
		this.id = doc.getInteger("_id");
		this.name = doc.getString("text");
		this.tags = (ArrayList<String>) doc.get("tags");
		this.links = (ArrayList<String>) doc.get("links");
	}
	
	public Document(int id, String name, ArrayList<String> tags, ArrayList<String> links) {
		this.id = id;
		this.name = name;
		this.tags = tags;
		this.links = links;
	}

	public org.bson.Document save() {
		org.bson.Document doc = new org.bson.Document();
		
		doc.put("_id", id);
		doc.put("name", name);
		doc.put("tags", tags);
		doc.put("links", links);
		
		return doc;
	}
	
	@SuppressWarnings("unchecked")
	public Document(Map<?, ?> map) {
		this();
		this.id = (Integer) map.get("id");
		this.score = (Float) map.get("score");
		this.name = (String) map.get("name");
		this.text = (String) map.get("text");
		this.tags = (ArrayList<String>) map.get("tags");
		this.links = (ArrayList<String>) map.get("links");
	}
	
	public String getDocFormat() {
		// ID - Name (tag, tag, tag)
		String format = this.getId() + " - " + this.getName() + " (";
		for(String tag : this.getTags()) {
			format += tag + ", ";
		}
		return format.substring(0, format.length()-3) + ")";
	}

	public Integer getId() {
		return id;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public Float getScore() {
		return score;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}

	public ArrayList<String> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<String> links) {
		this.links = links;
	}

	public void addTag(String tag) {
		tags.add(tag);
	}

	public void removeTag(String tag) {
		tags.remove(tag);
	}

	public void addLink(String link) {
		links.add(link);
	}

	public void removeLink(String link) {
		links.remove(link);
	}
	
	@Override
	public boolean equals(Object d) {
		return d == null || !(d instanceof Document) ? false : ((Document)d).id == this.id;
	}
}