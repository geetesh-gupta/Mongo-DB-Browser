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
package com.gg.plugins.mongo.action.result

import com.gg.plugins.mongo.view.MongoResultPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.SystemInfo
import java.awt.event.KeyEvent

class AddMongoDocumentAction(resultPanel: MongoResultPanel) :
    AnAction("Add", "Add com.gg.plugins.mongo document", AllIcons.General.Add), DumbAware {
    private val resultPanel: MongoResultPanel

    init {
        if (SystemInfo.isMac) {
            registerCustomShortcutSet(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, resultPanel)
        } else {
            registerCustomShortcutSet(KeyEvent.VK_INSERT, KeyEvent.ALT_DOWN_MASK, resultPanel)
        }
        this.resultPanel = resultPanel
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        resultPanel.addMongoDocument()
    }
}