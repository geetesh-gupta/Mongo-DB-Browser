/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import com.gg.plugins.mongo.config.ServerConfiguration;

import java.util.LinkedList;
import java.util.List;

public class MongoServer {

	private List<MongoDatabase> databases = new LinkedList<>();

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

	public List<MongoDatabase> getDatabases() {
		return databases;
	}

	public void setDatabases(List<MongoDatabase> databases) {
		this.databases = databases;
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

	public enum Status {
		OK, LOADING, ERROR
	}
}
