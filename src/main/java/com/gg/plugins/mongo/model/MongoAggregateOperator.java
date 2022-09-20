/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

public enum MongoAggregateOperator {

	MATCH("$match"),
	PROJECT("$project"),
	GROUP("$group"),
	SORT("$sort"),
	LIMIT("$limit"),
	SKIP("$skip"),
	UNWIND("$unwind");

	private final String operator;

	MongoAggregateOperator(String operator) {
		this.operator = operator;
	}

	public String getLabel() {
		return operator;
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
