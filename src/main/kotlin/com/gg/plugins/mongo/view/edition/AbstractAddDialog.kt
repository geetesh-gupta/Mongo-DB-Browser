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
import com.gg.plugins.mongo.utils.StringUtils.parseNumber
import com.gg.plugins.mongo.view.table.DateTimePicker
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBCheckBox
import org.apache.commons.lang.StringUtils
import org.bson.Document
import java.awt.BorderLayout
import java.util.Date
import java.util.EnumMap
import java.util.GregorianCalendar
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JTextField

abstract class AbstractAddDialog(val mongoEditionPanel: MongoEditionPanel) : DialogWrapper(
    mongoEditionPanel, true
) {
    var currentEditor: TextFieldWrapper<*, *>? = null
    fun initCombo(combobox: ComboBox<JsonDataType>, parentPanel: JPanel) {
        combobox.model = DefaultComboBoxModel(JsonDataType.values())
        combobox.renderer = object : ColoredListCellRenderer<Any?>() {
            override fun customizeCellRenderer(jList: JList<*>, o: Any?, i: Int, b: Boolean, b2: Boolean) {
                append((o as JsonDataType?)!!.type)
            }
        }
        combobox.selectedItem = null
        combobox.addItemListener {
            val selectedType = combobox.selectedItem as JsonDataType
            currentEditor = UI_COMPONENT_BY_JSON_DATATYPE[selectedType]
            currentEditor!!.reset()
            parentPanel.invalidate()
            parentPanel.removeAll()
            parentPanel.add(currentEditor!!.component!!, BorderLayout.CENTER)
            parentPanel.validate()
        }
        combobox.selectedItem = JsonDataType.STRING
    }

    abstract val value: Any?

    abstract class TextFieldWrapper<T : JComponent?, V>(val component: T) {
        abstract val value: V
        abstract fun reset()
        open fun validate() {
            require(isValueSet) { "Value is not set" }
        }

        open val isValueSet: Boolean
            get() = true
    }

    private class StringFieldWrapper : TextFieldWrapper<JTextField?, String>(JTextField()) {
        override val value: String
            get() = component!!.text

        override fun reset() {
            component!!.text = ""
        }

        override val isValueSet: Boolean
            get() = StringUtils.isNotBlank(component!!.text)
    }

    private open class JsonFieldWrapper : TextFieldWrapper<JTextField?, Document>(JTextField()) {
        override val value: Document
            get() = Document.parse(component!!.text)

        override fun reset() {
            component!!.text = ""
        }

        override val isValueSet: Boolean
            get() = StringUtils.isNotBlank(component!!.text)
    }

    private class ArrayFieldWrapper : JsonFieldWrapper() {
        override val value: Document
            get() {
                val arrayInDoc = String.format("{\"array\": %s}", component!!.text)
                return Document.parse(arrayInDoc)["array"] as Document
            }
    }

    private class NumberFieldWrapper : TextFieldWrapper<JTextField?, Number?>(JTextField()) {
        override val value: Number
            get() = parseNumber(component!!.text)

        override fun reset() {
            component!!.text = ""
        }

        override val isValueSet: Boolean
            get() = StringUtils.isNotBlank(component!!.text)

        override fun validate() {
            super.validate()
            value
        }
    }

    private class BooleanFieldWrapper : TextFieldWrapper<JBCheckBox?, Boolean>(JBCheckBox()) {
        override val value: Boolean
            get() = component!!.isSelected

        override fun reset() {
            component!!.isSelected = false
        }
    }

    private class NullFieldWrapper : TextFieldWrapper<JLabel?, Any?>(JLabel("null")) {
        override val value: Any?
            get() = null

        override fun reset() {}
    }

    private class DateTimeFieldWrapper : TextFieldWrapper<DateTimePicker?, Date>(DateTimePicker.create()) {
        init {
            component!!.editor.isEditable = false
        }

        override val value: Date
            get() = component!!.date

        override fun reset() {
            component!!.date = GregorianCalendar.getInstance().time
        }

        override val isValueSet: Boolean
            get() = component!!.date != null
    }

    companion object {
        private val UI_COMPONENT_BY_JSON_DATATYPE: MutableMap<JsonDataType, TextFieldWrapper<*, *>> = EnumMap(
            JsonDataType::class.java
        )

        init {
            UI_COMPONENT_BY_JSON_DATATYPE[JsonDataType.STRING] = StringFieldWrapper()
            UI_COMPONENT_BY_JSON_DATATYPE[JsonDataType.BOOLEAN] = BooleanFieldWrapper()
            UI_COMPONENT_BY_JSON_DATATYPE[JsonDataType.NUMBER] = NumberFieldWrapper()
            UI_COMPONENT_BY_JSON_DATATYPE[JsonDataType.NULL] = NullFieldWrapper()
            UI_COMPONENT_BY_JSON_DATATYPE[JsonDataType.DATE] =
                DateTimeFieldWrapper()
            UI_COMPONENT_BY_JSON_DATATYPE[JsonDataType.OBJECT] =
                JsonFieldWrapper()
            UI_COMPONENT_BY_JSON_DATATYPE[JsonDataType.ARRAY] = ArrayFieldWrapper()
        }
    }
}