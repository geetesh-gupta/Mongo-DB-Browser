/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.TreeSet;

@State(name = "MongoConfiguration", storages = {@Storage("$PROJECT_CONFIG_DIR$/mongoSettings.xml")})
public class MongoConfiguration implements PersistentStateComponent<MongoConfiguration> {
	private Set<ServerConfiguration> serverConfigurations = new TreeSet<>();

	private String shellPath;

	public static MongoConfiguration getInstance(Project project) {
		return project.getService(MongoConfiguration.class);
	}

	public MongoConfiguration getState() {
		return this;
	}

	public void loadState(@NotNull MongoConfiguration mongoConfiguration) {
		XmlSerializerUtil.copyBean(mongoConfiguration, this);
	}

	public Set<ServerConfiguration> getServerConfigurations() {
		return serverConfigurations;
	}

	public void setServerConfigurations(Set<ServerConfiguration> serverConfigurations) {
		this.serverConfigurations = serverConfigurations;
	}

	public String getShellPath() {
		return shellPath;
	}

	public void setShellPath(String shellPath) {
		this.shellPath = shellPath;
	}

	public void addServerConfiguration(ServerConfiguration serverConfiguration) {
		serverConfigurations.add(serverConfiguration);
	}

	public void updateServerConfiguration(ServerConfiguration updatedConfiguration) {
		serverConfigurations.remove(updatedConfiguration);
		serverConfigurations.add(updatedConfiguration);
	}

	public void removeServerConfiguration(ServerConfiguration configuration) {
		serverConfigurations.remove(configuration);
	}

}
