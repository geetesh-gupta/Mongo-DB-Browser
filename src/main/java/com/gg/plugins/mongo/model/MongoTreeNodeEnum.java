/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

public enum MongoTreeNodeEnum {

	ROOT("ROOT"),

	MongoServer("MongoServer"),

	MongoDatabase("MongoDatabase"),

	MongoCollection("MongoCollection");

	public final String type;

	MongoTreeNodeEnum(String type) {
		this.type = type;
	}
}
