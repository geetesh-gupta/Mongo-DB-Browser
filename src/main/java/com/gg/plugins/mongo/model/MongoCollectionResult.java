/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

public class MongoCollectionResult {

	private final String collectionName;

	private final List<Document> mongoObjects = new LinkedList<>();

	public MongoCollectionResult(String collectionName) {
		this.collectionName = collectionName;
	}

	public void add(Document document) {
		mongoObjects.add(document);
	}

	public List<Document> getDocuments() {
		return mongoObjects;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public int getTotalDocumentNumber() {
		return mongoObjects.size();
	}
}
