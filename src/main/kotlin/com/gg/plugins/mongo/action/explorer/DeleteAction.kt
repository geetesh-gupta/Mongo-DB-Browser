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

import com.gg.plugins.mongo.config.MongoConfiguration
import com.gg.plugins.mongo.model.MongoServer
import com.gg.plugins.mongo.view.MongoExplorerPanel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.SystemInfo
import java.awt.event.KeyEvent
import javax.swing.JOptionPane

class DeleteAction(private val mongoExplorerPanel: MongoExplorerPanel) :
    AnAction("Delete...", "Delete selected item", null), DumbAware {
    init {
        if (SystemInfo.isMac) {
            registerCustomShortcutSet(KeyEvent.VK_BACK_SPACE, 0, mongoExplorerPanel)
        } else {
            registerCustomShortcutSet(KeyEvent.VK_DELETE, 0, mongoExplorerPanel)
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isVisible = mongoExplorerPanel.selectedNode != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val selectedNode = mongoExplorerPanel.selectedNode
        deleteItem(mongoExplorerPanel.getTypeOfNode(selectedNode), selectedNode.toString()) {
            if (selectedNode is MongoServer) {
                val mongoConfiguration: MongoConfiguration =
                    MongoConfiguration.getInstance(event.project!!)
                mongoConfiguration.removeServerConfiguration(mongoExplorerPanel.getServerConfiguration(selectedNode)!!)
            }
            mongoExplorerPanel.removeNode(selectedNode!!)
        }
    }

    private fun deleteItem(itemTypeLabel: String?, itemLabel: String, deleteOperation: Runnable) {
        val result = JOptionPane.showConfirmDialog(
            null, String.format("Do you REALLY want to remove the '%s' %s?", itemLabel, itemTypeLabel),
            "Warning",
            JOptionPane.YES_NO_OPTION
        )
        if (result == JOptionPane.YES_OPTION) {
            deleteOperation.run()
        }
    }
}