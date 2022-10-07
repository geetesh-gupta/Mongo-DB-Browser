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
package com.gg.plugins.mongo.view.edition

import com.gg.plugins.mongo.view.MongoPanel
import com.gg.plugins.mongo.view.MongoResultPanel
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import org.bson.Document
import javax.swing.JComponent

class MongoEditionDialog private constructor(
    project: Project?,
    operations: MongoPanel.MongoDocumentOperations,
    actionCallback: MongoResultPanel.ActionCallback
) : DialogWrapper(project, true) {
    private val editionPanel: MongoEditionPanel

    init {
        editionPanel = MongoEditionPanel(operations, actionCallback)
        init()
    }

    override fun doValidate(): ValidationInfo? {
        return if (!editionPanel.save()) {
            ValidationInfo("Unable to save the document", editionPanel)
        } else null
    }

    override fun createCenterPanel(): JComponent {
        return editionPanel
    }

    fun initDocument(mongoDocument: Document?): MongoEditionDialog {
        var dialogTitle = "New Document"
        if (mongoDocument != null) {
            dialogTitle = "Edition"
        }
        title = dialogTitle
        editionPanel.updateEditionTree(mongoDocument)
        return this
    }

    companion object {
        fun create(
            project: Project?,
            operations: MongoPanel.MongoDocumentOperations,
            actionCallback: MongoResultPanel.ActionCallback
        ): MongoEditionDialog {
            return MongoEditionDialog(project, operations, actionCallback)
        }
    }
}