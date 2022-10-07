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
package com.gg.plugins.mongo.action.pagination

import com.gg.plugins.mongo.model.Pagination
import com.gg.plugins.mongo.model.ResultsPerPage
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import javax.swing.Icon

abstract class PaginationAction internal constructor(
    val pagination: Pagination,
    toolTip: String?,
    description: String?,
    icon: Icon?
) : AnAction(toolTip, description, icon), DumbAware {
    class Next(pagination: Pagination) :
        PaginationAction(pagination, "See next results", "Next page", AllIcons.Actions.Forward) {
        override fun update(event: AnActionEvent) {
            event.presentation.isEnabled = ResultsPerPage.ALL != pagination.resultsPerPage &&
                    pagination.pageNumber < pagination.totalPageNumber
        }

        override fun actionPerformed(e: AnActionEvent) {
            pagination.next()
        }
    }

    class Previous(pagination: Pagination) :
        PaginationAction(pagination, "See previous results", "Previous page", AllIcons.Actions.Back) {
        override fun update(event: AnActionEvent) {
            event.presentation.isEnabled = ResultsPerPage.ALL != pagination.resultsPerPage && pagination.startIndex > 0
        }

        override fun actionPerformed(e: AnActionEvent) {
            pagination.previous()
        }
    }
}