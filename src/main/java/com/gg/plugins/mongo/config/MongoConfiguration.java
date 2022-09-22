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
