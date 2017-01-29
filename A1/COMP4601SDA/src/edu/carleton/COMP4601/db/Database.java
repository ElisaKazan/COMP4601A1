package edu.carleton.COMP4601.db;

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

		return client.getDatabase("sda");
	}

	public static MongoCollection<Document> getCollection(String name) {
		return getDB().getCollection(name);
	}
}
