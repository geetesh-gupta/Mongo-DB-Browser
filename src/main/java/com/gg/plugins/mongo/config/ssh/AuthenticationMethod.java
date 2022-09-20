/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.config.ssh;

public enum AuthenticationMethod {

	PASSWORD("Password"), PRIVATE_KEY("Private key");

	private final String label;

	AuthenticationMethod(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}
}
