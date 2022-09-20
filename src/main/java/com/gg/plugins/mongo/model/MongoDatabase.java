/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class MongoDatabase {
	private final String name;

	private final MongoServer parentServer;

	private final SortedSet<MongoCollection> collections = new TreeSet<>();

	public MongoDatabase(String name, MongoServer mongoServer) {
		this.name = name;
		this.parentServer = mongoServer;
	}

	public Set<MongoCollection> getCollections() {
		return collections;
	}

	public void addCollection(MongoCollection mongoCollection) {
		collections.add(mongoCollection);
	}

	public MongoServer getParentServer() {
		return parentServer;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return name;
	}
}
