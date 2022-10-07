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

import com.gg.plugins.mongo.config.MongoConfiguration
import com.gg.plugins.mongo.utils.MongoUtils
import com.intellij.execution.ExecutionException
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.BaseConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import org.apache.commons.lang.StringUtils
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class MongoConfigurable(private val project: Project) : BaseConfigurable(), SearchableConfigurable {
    private val configuration: MongoConfiguration = MongoConfiguration.getInstance(project)
    private var mainPanel: JPanel?
    private var shellPathField: LabeledComponent<TextFieldWithBrowseButton>? = null
    private var testMongoPathFeedbackLabel: JLabel? = null

    init {
        mainPanel = JPanel(BorderLayout())
    }

    override fun getHelpTopic(): String {
        return "preferences.mongoOptions"
    }

    override fun getDisplayName(): @Nls String {
        return PLUGIN_SETTINGS_NAME
    }

    override fun createComponent(): JComponent? {
        val mongoShellOptionsPanel = JPanel()
        mongoShellOptionsPanel.layout = BoxLayout(mongoShellOptionsPanel, BoxLayout.X_AXIS)
        shellPathField = createShellPathField()
        mongoShellOptionsPanel.add(JLabel("Path to Mongo Shell:"))
        mongoShellOptionsPanel.add(shellPathField)
        mongoShellOptionsPanel.add(createTestButton())
        mongoShellOptionsPanel.add(createFeedbackLabel())
        mainPanel!!.add(mongoShellOptionsPanel, BorderLayout.NORTH)
        return mainPanel
    }

    private fun createShellPathField(): LabeledComponent<TextFieldWithBrowseButton> {
        val shellPathField = LabeledComponent<TextFieldWithBrowseButton>()
        val component = TextFieldWithBrowseButton()
        component.childComponent.name = "shellPathField"
        shellPathField.component = component
        shellPathField.component
            .addBrowseFolderListener(
                "Mongo Shell Configuration",
                "",
                null,
                FileChooserDescriptor(true, false, false, false, false, false)
            )
        shellPathField.component.text = configuration.shellPath.toString()
        return shellPathField
    }

    private fun createTestButton(): JButton {
        val testButton = JButton("Test")
        testButton.addActionListener { testPath() }
        return testButton
    }

    private fun createFeedbackLabel(): JLabel {
        testMongoPathFeedbackLabel = JLabel()
        return testMongoPathFeedbackLabel!!
    }

    private fun testPath() {
        try {
            testMongoPathFeedbackLabel!!.icon = null
            if (MongoUtils.checkMongoShellPath(shellPath)) {
                testMongoPathFeedbackLabel!!.icon = ServerConfigurationPanel.SUCCESS
            } else {
                testMongoPathFeedbackLabel!!.icon = ServerConfigurationPanel.FAIL
            }
        } catch (e: ExecutionException) {
            Messages.showErrorDialog(mainPanel, e.message, "Error During Mongo Shell Path Checking...")
        }
    }

    private val shellPath: String?
        get() {
            val shellPath = shellPathField!!.component.text
            return if (StringUtils.isNotBlank(shellPath)) {
                shellPath
            } else null
        }

    override fun apply() {
        if (isShellPathModified) {
            configuration.shellPath = shellPath
        }
    }

    override fun reset() {}
    override fun disposeUIResources() {
        mainPanel = null
        shellPathField = null
    }

    private val isShellPathModified: Boolean
        get() {
            val existingShellPath: String? = MongoConfiguration.getInstance(project).shellPath
            return !StringUtils.equals(existingShellPath, shellPath)
        }

    override fun isModified(): Boolean {
        return isShellPathModified
    }

    override fun getId(): String {
        return "preferences.mongoOptions"
    }

    override fun enableSearch(option: String): Runnable? {
        return null
    }

    companion object {
        const val PLUGIN_SETTINGS_NAME = "Mongo DB Browser"
    }
}