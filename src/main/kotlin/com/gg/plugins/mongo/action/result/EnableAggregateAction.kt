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

import com.gg.plugins.mongo.utils.GuiUtils
import com.gg.plugins.mongo.view.QueryPanel
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware

class EnableAggregateAction(private val queryPanel: QueryPanel) :
    ToggleAction(ENABLE_AGGREGATION_MODE, QUERY_FIND_SAMPLE, AGGREGATION_ICON), DumbAware {
    private var enableAggregation = false
    override fun isSelected(anActionEvent: AnActionEvent): Boolean {
        return enableAggregation
    }

    override fun setSelected(event: AnActionEvent, enableAggregation: Boolean) {
        this.enableAggregation = enableAggregation
        if (enableAggregation) {
            queryPanel.toggleToAggregation()
        } else {
            queryPanel.toggleToFind()
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.text =
            if (isSelected(event)) ENABLE_FIND_MODE else ENABLE_AGGREGATION_MODE
        event.presentation.description =
            if (isSelected(event)) QUERY_AGGREGATION_SAMPLE else QUERY_FIND_SAMPLE
        event.presentation.isVisible = queryPanel.isVisible
    }

    companion object {
        private const val ENABLE_FIND_MODE = "Toggle to Find Mode"
        private const val ENABLE_AGGREGATION_MODE = "Toggle to Aggregation Mode"
        private val AGGREGATION_ICON = GuiUtils.loadIcon("sqlGroupByType.png")
        private const val QUERY_FIND_SAMPLE = "ex: {'name': 'foo'}"
        private const val QUERY_AGGREGATION_SAMPLE = "ex: [{'\$match': {'name': 'foo'}, {'\$project': {'address': 1}}]"
    }
}