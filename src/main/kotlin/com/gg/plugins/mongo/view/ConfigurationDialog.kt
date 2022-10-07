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
package com.gg.plugins.mongo.view

import com.gg.plugins.mongo.config.ConfigurationException
import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.service.MongoService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent

class ConfigurationDialog(
    private val project: Project,
    parent: MongoExplorerPanel?,
    configuration: ServerConfiguration
) : DialogWrapper(
    parent!!, true
) {
    private val mongoService: MongoService = MongoService.getInstance(project)
    private val configuration: ServerConfiguration
    private var serverConfigurationPanel: ServerConfigurationPanel? = null

    init {
        this.configuration = configuration
        init()
    }

    override fun createCenterPanel(): JComponent? {
        serverConfigurationPanel = ServerConfigurationPanel(project, mongoService)
        serverConfigurationPanel!!.loadConfigurationData(configuration)
        return serverConfigurationPanel
    }

    override fun doOKAction() {
        try {
            serverConfigurationPanel!!.applyConfigurationData(configuration)
            super.doOKAction()
        } catch (confEx: ConfigurationException) {
            serverConfigurationPanel!!.setErrorMessage(confEx.message)
        }
    }
}