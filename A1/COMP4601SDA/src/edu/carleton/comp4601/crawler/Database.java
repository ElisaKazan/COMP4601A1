package edu.carleton.comp4601.crawler;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class Database {
	private static MongoClient client;

	public static MongoDatabase getDB() {
		if (client == null) {
			client = new MongoClient("localhost", 27017);
		}

		return client.getDatabase("crawler");
	}

	public static MongoCollection<Document> getCollection(String name) {
		return getDB().getCollection(name);
	}
}
