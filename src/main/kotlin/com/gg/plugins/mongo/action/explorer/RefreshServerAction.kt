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
package com.gg.plugins.mongo.action.explorer

import com.gg.plugins.mongo.model.MongoDatabase
import com.gg.plugins.mongo.model.MongoServer
import com.gg.plugins.mongo.utils.GuiUtils
import com.gg.plugins.mongo.view.MongoExplorerPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class RefreshServerAction(private val mongoExplorerPanel: MongoExplorerPanel) : AnAction(REFRESH_TEXT), DumbAware {
    override fun update(event: AnActionEvent) {
        when (val selectedNode = mongoExplorerPanel.selectedNode) {
            is MongoServer, is MongoDatabase -> {
                event.presentation.isVisible = true
                when (selectedNode) {
                    is MongoServer -> {
                        val isLoading = MongoServer.Status.LOADING == selectedNode.status
                        event.presentation.isEnabled = !isLoading
                        val isConnected = selectedNode.isConnected()
                        event.presentation.icon =
                            when {
                                isConnected -> REFRESH_ICON
                                else -> CONNECT_ICON
                            }
                        event.presentation.text =
                            when {
                                isConnected -> REFRESH_TEXT
                                else -> CONNECT_TEXT
                            }
                    }

                    else -> {
                        event.presentation.text = "Refresh This Database"
                        event.presentation.isEnabled = true
                    }
                }
            }

            else -> {
                event.presentation.isEnabled = false
                event.presentation.isVisible = false
            }
        }
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        when (val selectedNode = mongoExplorerPanel.selectedNode) {
            is MongoServer -> mongoExplorerPanel.openServer(selectedNode)
            is MongoDatabase -> mongoExplorerPanel.loadDatabase(
                selectedNode
            )
        }
    }

    companion object {
        private val CONNECT_ICON = GuiUtils.loadIcon("connector.png", "connector_dark.png")
        private val REFRESH_ICON = AllIcons.Actions.Refresh
        private const val REFRESH_TEXT = "Refresh this server"
        private const val CONNECT_TEXT = "Connect to this server"
    }
}