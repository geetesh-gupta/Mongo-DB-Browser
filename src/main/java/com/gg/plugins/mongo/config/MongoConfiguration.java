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

import java.util.LinkedList;
import java.util.List;

@State(name = "MongoConfiguration", storages = {@Storage("$PROJECT_CONFIG_DIR$/mongoSettings.xml")})
public class MongoConfiguration implements PersistentStateComponent<MongoConfiguration> {

	private List<ServerConfiguration> serverConfigurations = new LinkedList<>();

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

	public List<ServerConfiguration> getServerConfigurations() {
		return serverConfigurations;
	}

	public void setServerConfigurations(List<ServerConfiguration> serverConfigurations) {
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

	public void updateServerConfiguration(ServerConfiguration previousConfiguration,
			ServerConfiguration updatedConfiguration) {
		if (previousConfiguration.equals(updatedConfiguration)) {
			return;
		}

		int index = getServerConfigurationIndex(previousConfiguration);
		serverConfigurations.set(index, updatedConfiguration);
	}

	private int getServerConfigurationIndex(ServerConfiguration configuration) {
		int index = 0;
		for (ServerConfiguration serverConfiguration : serverConfigurations) {
			if (serverConfiguration.equals(configuration)) {
				return index;
			}
			index++;
		}

		throw new IllegalArgumentException("Unable to find the configuration to updated");
	}

	public void removeServerConfiguration(ServerConfiguration configuration) {
		serverConfigurations.remove(configuration);
	}
}
