/*
 * Copyright (c) 2018 David Boissier.
 * Modifications Copyright (c) 2022 Geetesh Gupta.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
