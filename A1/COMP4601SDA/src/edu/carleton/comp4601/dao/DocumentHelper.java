package edu.carleton.comp4601.dao;

public class DocumentHelper {
	private static String PREFIX = "/COMP4601SDA/rest/sda/";

	public static String getDocFormatLong(Document doc) {
		StringBuilder builder = new StringBuilder("<html><body>");
		
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
	
		builder.append("Text: <br><pre>");
	
		builder.append(doc.getText());
	
		builder.append("</pre>");
	
		return builder.toString();
	}

	public static String getDocFormat(Document doc) {
		// ID - Name (tag, tag, tag)
		String format = "<a href=\"" + PREFIX + doc.getId() + "\">" + doc.getId() + " - " + doc.getName() + " (";
		
		for(String tag : doc.getTags()) {
			format += tag + ", ";
		}
		
		format = format.substring(0, format.length()-2) + ")";
		
		format += "</a>";
		
		return format;
	}
	
	public static org.bson.Document save(Document d) {
		org.bson.Document doc = new org.bson.Document();
		
		doc.put("_id", d.getId());
		doc.put("name", d.getName());
		doc.put("tags", d.getTags());
		doc.put("text", d.getText());
		doc.put("links", d.getLinks());
		doc.put("score", d.getScore());
		
		return doc;
	}
}
