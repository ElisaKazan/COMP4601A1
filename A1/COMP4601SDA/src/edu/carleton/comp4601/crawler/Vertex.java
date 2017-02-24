package edu.carleton.comp4601.crawler;

import java.util.List;

public class Vertex {
	public String label;
	public List<String> links;
	
	public Vertex(String label, List<String> links) {
		this.label = label;
		this.links = links;
	}
	
	public String getLabel() {
		return label;
	}
	
	
	public List<String> getLinks() {
		return links;
	}
	
	public String toString() {
		return label;
	}
}
