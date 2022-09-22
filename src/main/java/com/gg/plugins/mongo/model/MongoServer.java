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

import com.gg.plugins.mongo.config.ServerConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MongoServer implements Comparable<MongoServer> {

	private Set<MongoDatabase> databases = new TreeSet<>();

	private ServerConfiguration configuration;

	private Status status = Status.OK;

	public MongoServer(ServerConfiguration configuration) {
		this.configuration = configuration;
	}

	public List<String> getServerUrls() {
		return configuration.getServerUrls();
	}

	public boolean isConnected() {
		return !databases.isEmpty();
	}

	public Set<MongoDatabase> getDatabases() {
		return databases;
	}

	public void setDatabases(Set<MongoDatabase> databases) {
		this.databases = databases;
	}

	public void updateDatabase(MongoDatabase database) {
		this.databases.remove(database);
		this.databases.add(database);
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public ServerConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ServerConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String toString() {
		return getLabel();
	}

	public String getLabel() {
		return configuration.getLabel();
	}

	@Override
	public int compareTo(@NotNull MongoServer o) {
		return getLabel().compareTo(o.getLabel());
	}

	public enum Status {
		OK, LOADING, ERROR
	}
}
