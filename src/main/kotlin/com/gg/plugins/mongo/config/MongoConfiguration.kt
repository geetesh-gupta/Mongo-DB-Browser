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
package com.gg.plugins.mongo.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.util.TreeSet

@State(name = "MongoConfiguration", storages = [Storage("\$PROJECT_CONFIG_DIR$/mongoSettings.xml")])
class MongoConfiguration : PersistentStateComponent<MongoConfiguration> {
    var serverConfigurations: MutableSet<ServerConfiguration> = TreeSet()
    var shellPath: String? = null

    override fun getState(): MongoConfiguration {
        return this
    }

    override fun loadState(mongoConfiguration: MongoConfiguration) {
        XmlSerializerUtil.copyBean(mongoConfiguration, this)
    }

    fun addServerConfiguration(serverConfiguration: ServerConfiguration) {
        serverConfigurations.add(serverConfiguration)
    }

    fun updateServerConfiguration(updatedConfiguration: ServerConfiguration) {
        serverConfigurations.remove(updatedConfiguration)
        serverConfigurations.add(updatedConfiguration)
    }

    fun removeServerConfiguration(configuration: ServerConfiguration) {
        serverConfigurations.remove(configuration)
    }

    companion object {
        fun getInstance(project: Project): MongoConfiguration {
            return project.getService(MongoConfiguration::class.java)
        }
    }
}