/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

public enum JsonDataType {

	STRING("String"),
	NUMBER("Number"),
	BOOLEAN("Boolean"),
	ARRAY("Array"),
	OBJECT("Object"),
	NULL("Null"),
	DATE("Date");

	public final String type;

	JsonDataType(String type) {
		this.type = type;
	}
}
