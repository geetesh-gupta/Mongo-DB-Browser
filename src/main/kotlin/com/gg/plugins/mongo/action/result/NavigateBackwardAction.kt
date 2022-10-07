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

import com.gg.plugins.mongo.view.MongoPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class NavigateBackwardAction(private val mongoPanel: MongoPanel) :
    AnAction("Navigate Backward", "Navigate backward", AllIcons.Actions.Back), DumbAware {
    override fun update(event: AnActionEvent) {
        event.presentation.isVisible = mongoPanel.hasNavigationHistory()
    }

    override fun actionPerformed(e: AnActionEvent) {
        mongoPanel.navigateBackward()
    }
}