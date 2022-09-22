/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;

public class MongoDatabase implements Comparable<MongoDatabase> {
	private final String name;

	private final MongoServer parentServer;

	private Set<MongoCollection> collections = new TreeSet<>();

	public MongoDatabase(String name, MongoServer mongoServer) {
		this.name = name;
		this.parentServer = mongoServer;
	}

	public Set<MongoCollection> getCollections() {
		return collections;
	}

	public void setCollections(Set<MongoCollection> collections) {
		this.collections = collections;
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

	@Override
	public int compareTo(@NotNull MongoDatabase o) {
		return name.compareTo(o.getName());
	}

}
