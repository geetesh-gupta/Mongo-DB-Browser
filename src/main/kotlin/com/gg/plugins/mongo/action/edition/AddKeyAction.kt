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
package com.gg.plugins.mongo.action.edition

import com.gg.plugins.mongo.view.edition.AddKeyDialog
import com.gg.plugins.mongo.view.edition.MongoEditionPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import java.awt.event.KeyEvent

class AddKeyAction(mongoEditionPanel: MongoEditionPanel) : AnAction("Add a Key", "Add a key", AllIcons.General.Add),
                                                           DumbAware {
    private val mongoEditionPanel: MongoEditionPanel

    init {
        registerCustomShortcutSet(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK, mongoEditionPanel)
        this.mongoEditionPanel = mongoEditionPanel
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val dialog: AddKeyDialog = AddKeyDialog.createDialog(mongoEditionPanel)
        dialog.show()
        if (!dialog.isOK) {
            return
        }
        mongoEditionPanel.addKey(dialog.key, dialog.value)
    }
}