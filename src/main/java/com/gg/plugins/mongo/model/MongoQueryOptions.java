/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

public class MongoQueryOptions {

	public static final int NO_LIMIT = 0;

	public static final int DEFAULT_RESULT_LIMIT = 10;

	public static final Document EMPTY_DOCUMENT = new Document();

	private final List<BasicDBObject> operations = new LinkedList<>();

	private Document filter = EMPTY_DOCUMENT;

	private Document projection = EMPTY_DOCUMENT;

	private Document sort = EMPTY_DOCUMENT;

	private int resultLimit = DEFAULT_RESULT_LIMIT;

	public boolean isAggregate() {
		return !operations.isEmpty();
	}

	public List<BasicDBObject> getOperations() {
		return operations;
	}

	public void setOperations(String aggregateQuery) {
		operations.clear();
		BasicDBList operations = (BasicDBList) JSON.parse(aggregateQuery);
		for (Object operation1 : operations) {
			BasicDBObject operation = (BasicDBObject) operation1;
			this.operations.add(operation);
		}
	}

	public Document getFilter() {
		return filter;
	}

	public void setFilter(String query) {
		if (!StringUtils.isBlank(query)) {
			filter = Document.parse(query);
		}
	}

	public MongoQueryOptions setFilter(Document filter) {
		this.filter = filter;
		return this;
	}

	public Document getProjection() {
		return projection;
	}

	public void setProjection(String query) {
		if (!StringUtils.isBlank(query)) {
			projection = Document.parse(query);
		}
	}

	public Document getSort() {
		return sort;
	}

	public void setSort(String query) {
		if (!StringUtils.isBlank(query)) {
			sort = Document.parse(query);
		}
	}

	public int getResultLimit() {
		return resultLimit;
	}

	public void setResultLimit(int resultLimit) {
		this.resultLimit = resultLimit;
	}
}
