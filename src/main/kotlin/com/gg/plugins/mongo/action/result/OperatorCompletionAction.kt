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

import com.gg.plugins.mongo.model.MongoAggregateOperator
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.ui.components.JBList
import com.mongodb.QueryOperators
import java.awt.event.KeyEvent
import java.util.LinkedList
import javax.swing.JList

class OperatorCompletionAction(private val project: Project, private val editor: Editor) : AnAction(), Disposable,
                                                                                           DumbAware {
    init {
        registerCustomShortcutSet(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK, editor.contentComponent)
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val document = editor.document
        val caretModel = editor.caretModel
        val offset = caretModel.offset
        PopupChooserBuilder(QUERY_OPERATOR_LIST).setMovable(false)
            .setCancelKeyEnabled(true)
            .setItemChoosenCallback {
                val selectedQueryOperator = QUERY_OPERATOR_LIST.selectedValue
                    ?: return@setItemChoosenCallback
                WriteCommandAction.writeCommandAction(project)
                    .withName(MONGO_OPERATOR_COMPLETION)
                    .run<RuntimeException> {
                        document.insertString(
                            offset,
                            selectedQueryOperator
                        )
                    }
            }
            .createPopup()
            .showInBestPositionFor(editor)
    }

    override fun dispose() {
        unregisterCustomShortcutSet(editor.contentComponent)
    }

    companion object {
        private const val MONGO_OPERATOR_COMPLETION = "MONGO_OPERATOR_COMPLETION"
        private var QUERY_OPERATOR_LIST: JList<String>
        private val operator = LinkedList<String>()

        init {
            MongoAggregateOperator.values().forEach { aggregateOperator ->
                operator.add(aggregateOperator.label)
            }
            for (field in QueryOperators::class.java.fields) {
                try {
                    operator.add(
                        QueryOperators::class.java.getDeclaredField(field.name)[String::class.java] as String
                    )
                } catch (ex: IllegalAccessException) {
                    throw IllegalStateException(ex)
                } catch (ex: NoSuchFieldException) {
                    throw IllegalStateException(ex)
                }
            }
            QUERY_OPERATOR_LIST = JBList(operator)
        }
    }
}