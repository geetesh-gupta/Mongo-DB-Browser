/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.config;

public class ConfigurationException extends RuntimeException {
	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(Exception ex) {
		super(ex);
	}
}
