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

import com.gg.plugins.mongo.model.JsonDataType
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

class AddValueDialog private constructor(mongoEditionPanel: MongoEditionPanel) : AbstractAddDialog(mongoEditionPanel) {
    lateinit var typeComboBox: ComboBox<JsonDataType>
    lateinit var valuePanel: JPanel
    lateinit var mainPanel: JPanel

    init {
        valuePanel.layout = BorderLayout()
        typeComboBox.name = "valueType"
        typeComboBox.requestFocus()
    }

    override fun doValidate(): ValidationInfo? {
        try {
            currentEditor!!.validate()
        } catch (ex: Exception) {
            return ValidationInfo(ex.message!!)
        }
        return null
    }

    override fun createCenterPanel(): JComponent {
        return mainPanel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return typeComboBox
    }

    override fun init() {
        super.init()
        initCombo(typeComboBox, valuePanel)
    }

    override val value: Any?
        get() = currentEditor!!.value

    companion object {
        fun createDialog(parentPanel: MongoEditionPanel): AddValueDialog {
            val dialog = AddValueDialog(parentPanel)
            dialog.init()
            dialog.title = "Add A Value"
            return dialog
        }
    }
}