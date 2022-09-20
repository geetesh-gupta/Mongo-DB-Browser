/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MongoCollection implements Comparable<MongoCollection> {

	private final String name;

	private final MongoDatabase parentDatabase;

	public MongoCollection(String name, MongoDatabase mongoDatabase) {
		this.name = name;
		this.parentDatabase = mongoDatabase;
	}

	public MongoDatabase getParentDatabase() {
		return parentDatabase;
	}

	@Override
	public int compareTo(@NotNull MongoCollection otherCollection) {
		return this.name.compareTo(otherCollection.getName());
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, parentDatabase);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MongoCollection))
			return false;
		MongoCollection that = (MongoCollection) o;
		return Objects.equals(name, that.name) && Objects.equals(parentDatabase, that.parentDatabase);
	}

	@Override
	public String toString() {
		return "MongoCollection{" + "name='" + name + '\'' + '}';
	}
}
