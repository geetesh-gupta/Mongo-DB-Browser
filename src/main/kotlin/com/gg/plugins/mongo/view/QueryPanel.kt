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

import com.gg.plugins.mongo.action.result.OperatorCompletionAction
import com.gg.plugins.mongo.model.MongoQueryOptions
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.intellij.lang.Language
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorSettings
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.PlainTextSyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.util.Alarm
import com.intellij.util.ui.UIUtil
import org.apache.commons.lang.StringUtils
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Font
import java.awt.Point
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane

class QueryPanel(private val project: Project) : JPanel(), Disposable {
    private val queryCardLayout: CardLayout
    private val filterPanel: OperatorPanel
    private val aggregationPanel: OperatorPanel
    private var myUpdateAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD)
    lateinit var mainPanel: JPanel
    lateinit var queryContainerPanel: JPanel

    init {
        layout = BorderLayout()
        add(mainPanel)
        queryCardLayout = CardLayout()
        queryContainerPanel.layout = queryCardLayout
        filterPanel = createFilterPanel()
        queryContainerPanel.add(filterPanel, FILTER_PANEL)
        aggregationPanel = createAggregationPanel()
        queryContainerPanel.add(aggregationPanel, AGGREGATION_PANEL)
        toggleToFind()
        //        Disposer.register(project, this)
    }

    private fun createFilterPanel(): OperatorPanel {
        return FilterPanel()
    }

    private fun createAggregationPanel(): OperatorPanel {
        return AggregatorPanel()
    }

    fun toggleToFind() {
        queryCardLayout.show(queryContainerPanel, FILTER_PANEL)
    }

    fun requestFocusOnEditor() { // Code from requestFocus of EditorImpl
        val focusManager = IdeFocusManager.getInstance(project)
        val editorContentComponent: JComponent = currentOperatorPanel.requestFocusComponent
        if (focusManager.focusOwner !== editorContentComponent) {
            focusManager.requestFocus(editorContentComponent, true)
        }
    }

    private val currentOperatorPanel: OperatorPanel
        get() = if (filterPanel.isVisible) filterPanel else aggregationPanel

    private fun attachHighlighter(editor: EditorEx) {
        val scheme = editor.colorsScheme
        scheme.setColor(EditorColors.CARET_ROW_COLOR, null)
        editor.highlighter = createHighlighter(scheme)
    }

    private fun createHighlighter(settings: EditorColorsScheme): EditorHighlighter {
        var language = Language.findLanguageByID("JSON")
        if (language == null) {
            language = Language.ANY
        }
        return LexerEditorHighlighter(
            PlainTextSyntaxHighlighterFactory.getSyntaxHighlighter(language!!, null, null),
            settings
        )
    }

    fun getQueryOptions(rowLimit: String): MongoQueryOptions {
        return currentOperatorPanel.buildQueryOptions(rowLimit)
    }

    override fun dispose() {
        myUpdateAlarm.cancelAllRequests()
        filterPanel.dispose()
        aggregationPanel.dispose()
    }

    fun toggleToAggregation() {
        queryCardLayout.show(queryContainerPanel, AGGREGATION_PANEL)
    }

    fun validateQuery() {
        currentOperatorPanel.validateQuery()
    }

    private inner class AggregatorPanel : OperatorPanel() {
        private val editor: Editor = createEditor()
        private val operatorCompletionAction: OperatorCompletionAction

        init {
            layout = BorderLayout()
            val headPanel = NonOpaquePanel()
            val operatorLabel = JLabel("Aggregation")
            headPanel.add(operatorLabel, BorderLayout.WEST)
            add(headPanel, BorderLayout.NORTH)
            add(editor.component, BorderLayout.CENTER)
            operatorCompletionAction = OperatorCompletionAction(project, editor)
            myUpdateAlarm = Alarm(editor.component, this)
            //			myUpdateAlarm.setActivationComponent(this.editor.getComponent());
        }

        override val requestFocusComponent: JComponent
            get() = editor.contentComponent

        override fun validateQuery() {
            try {
                val query = query
                if (StringUtils.isEmpty(query)) {
                    return
                }
                JsonParser.parseString(query)
                //				JSON.parse(query);
            } catch (ex: JsonParseException) {
                notifyOnErrorForOperator(editor.component, ex)
            } catch (ex: NumberFormatException) {
                notifyOnErrorForOperator(editor.component, ex)
            }
        }

        private val query: String
            get() = String.format("[%s]", StringUtils.trim(editor.document.text))

        override fun buildQueryOptions(rowLimit: String): MongoQueryOptions {
            val mongoQueryOptions = MongoQueryOptions()
            try {
                mongoQueryOptions.setOperations(query)
            } catch (ex: JsonParseException) {
                notifyOnErrorForOperator(editor.component, ex)
            }
            if (StringUtils.isNotBlank(rowLimit)) {
                mongoQueryOptions.resultLimit = rowLimit.toInt()
            }
            return mongoQueryOptions
        }

        override fun dispose() {
            operatorCompletionAction.dispose()
            EditorFactory.getInstance().releaseEditor(editor)
        }
    }

    private inner class FilterPanel : OperatorPanel() {
        private val selectEditor: Editor
        private val operatorCompletionAction: OperatorCompletionAction
        private val projectionEditor: Editor
        private val sortEditor: Editor

        init {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            selectEditor = createEditor()
            operatorCompletionAction = OperatorCompletionAction(project, selectEditor)
            add(createSubOperatorPanel("Filter", selectEditor))
            projectionEditor = createEditor()
            add(createSubOperatorPanel("Projection", projectionEditor))
            sortEditor = createEditor()
            add(createSubOperatorPanel("Sort", sortEditor))
        }

        private fun createSubOperatorPanel(title: String, subOperatorEditor: Editor): JPanel {
            val selectPanel = JPanel()
            selectPanel.layout = BorderLayout()
            val headPanel = NonOpaquePanel()
            val operatorLabel = JLabel(title)
            headPanel.add(operatorLabel, BorderLayout.WEST)
            selectPanel.add(headPanel, BorderLayout.NORTH)
            selectPanel.add(subOperatorEditor.component, BorderLayout.CENTER)
            myUpdateAlarm = Alarm(subOperatorEditor.component, this)
            //			myUpdateAlarm.setActivationComponent(subOperatorEditor.getComponent());
            return selectPanel
        }

        override val requestFocusComponent: JComponent
            get() = selectEditor.contentComponent

        override fun validateQuery() {
            validateEditorQuery(selectEditor)
            validateEditorQuery(projectionEditor)
            validateEditorQuery(sortEditor)
        }

        override fun buildQueryOptions(rowLimit: String): MongoQueryOptions {
            val mongoQueryOptions = MongoQueryOptions()
            try {
                mongoQueryOptions.setFilter(getQueryFrom(selectEditor))
                mongoQueryOptions.setProjection(getQueryFrom(projectionEditor))
                mongoQueryOptions.setSort(getQueryFrom(sortEditor))
            } catch (ex: JsonParseException) {
                notifyOnErrorForOperator(selectEditor.component, ex)
            }
            if (StringUtils.isNotBlank(rowLimit)) {
                mongoQueryOptions.resultLimit = rowLimit.toInt()
            } else {
                mongoQueryOptions.resultLimit = MongoQueryOptions.NO_LIMIT
            }
            return mongoQueryOptions
        }

        private fun validateEditorQuery(editor: Editor) {
            try {
                val query = getQueryFrom(editor)
                if (StringUtils.isEmpty(query)) {
                    return
                }
                JsonParser.parseString(query)
                //				JSON.parse(query);
            } catch (ex: JsonParseException) {
                notifyOnErrorForOperator(editor.component, ex)
            } catch (ex: NumberFormatException) {
                notifyOnErrorForOperator(editor.component, ex)
            }
        }

        private fun getQueryFrom(editor: Editor): String {
            return StringUtils.trim(editor.document.text)
        }

        override fun dispose() {
            operatorCompletionAction.dispose()
            EditorFactory.getInstance().releaseEditor(selectEditor)
            EditorFactory.getInstance().releaseEditor(projectionEditor)
            EditorFactory.getInstance().releaseEditor(sortEditor)
        }
    }

    private abstract inner class OperatorPanel : JPanel(), Disposable {
        abstract val requestFocusComponent: JComponent
        abstract fun validateQuery()
        abstract fun buildQueryOptions(rowLimit: String): MongoQueryOptions
        fun notifyOnErrorForOperator(component: JComponent?, ex: Exception) {
            val message: String = if (ex is JsonParseException) {
                StringUtils.removeStart(ex.message, "\n")
            } else {
                String.format("%s: %s", ex.javaClass.simpleName, ex.message)
            }
            val nonOpaquePanel = NonOpaquePanel()
            val textPane = Messages.configureMessagePaneUi(JTextPane(), message)
            textPane.font = COURIER_FONT
            textPane.background = MessageType.ERROR.popupBackground
            nonOpaquePanel.add(textPane, BorderLayout.CENTER)
            nonOpaquePanel.add(JLabel(MessageType.ERROR.defaultIcon), BorderLayout.WEST)
            UIUtil.invokeLaterIfNeeded {
                JBPopupFactory.getInstance()
                    .createBalloonBuilder(nonOpaquePanel)
                    .setFillColor(MessageType.ERROR.popupBackground)
                    .createBalloon()
                    .show(
                        RelativePoint(component!!, Point(0, 0)),
                        Balloon.Position.above
                    )
            }
        }

        fun createEditor(): Editor {
            val editorFactory = EditorFactory.getInstance()
            val editorDocument = editorFactory.createDocument("")
            val editor = editorFactory.createEditor(editorDocument, project)
            fillEditorSettings(editor.settings)
            val editorEx = editor as EditorEx
            attachHighlighter(editorEx)
            return editor
        }
    }

    companion object {
        private val COURIER_FONT = Font("Courier", Font.PLAIN, UIUtil.getLabelFont().size)
        private const val FILTER_PANEL = "FilterPanel"
        private const val AGGREGATION_PANEL = "AggregationPanel"
        private fun fillEditorSettings(editorSettings: EditorSettings) {
            editorSettings.isWhitespacesShown = true
            editorSettings.isLineMarkerAreaShown = false
            editorSettings.isIndentGuidesShown = false
            editorSettings.isLineNumbersShown = false
            editorSettings.isAllowSingleLogicalLineFolding = true
            editorSettings.additionalColumnsCount = 0
            editorSettings.additionalLinesCount = 1
            editorSettings.isUseSoftWraps = true
            editorSettings.setUseTabCharacter(false)
            editorSettings.isCaretInsideTabs = false
            editorSettings.isVirtualSpace = false
        }
    }
}