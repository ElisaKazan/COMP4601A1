package edu.carleton.comp4601.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.bson.Document;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;

public class Graph {
	Multigraph<Vertex, DefaultEdge> graph;
	Map<String, Vertex> vertices;
	
	public Graph() {
		this.graph = new Multigraph<>(DefaultEdge.class);
		this.vertices = new HashMap<>();
	}
	
	@SuppressWarnings("unchecked")
	public void addDocument(Document d) {
		Vertex v = new Vertex(d.getString("url"), (List<String>)d.get("links"));
		
		graph.addVertex(v);

		vertices.put(v.getLabel(), v);
	}
	
	public void attachDocuments() {
		for (Vertex v : vertices.values()) {
			for (String s : v.getLinks()) {
				Vertex target = null;
				if (vertices.containsKey(s)) {
					target = vertices.get(s);
				}
				else {
					target = new Vertex(s, new ArrayList<>());
					graph.addVertex(target);
				}
				graph.addEdge(v, target);
			}
		}
	}
	
	public void print() {
		for(DefaultEdge e : graph.edgeSet()){
		    System.out.println(graph.getEdgeSource(e) + " <--> " + graph.getEdgeTarget(e));
		}
	}
}
