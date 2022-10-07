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
package com.gg.plugins.mongo.view.table

import com.gg.plugins.mongo.utils.DateUtils
import com.gg.plugins.mongo.view.nodedescriptor.MongoNodeDescriptor
import org.jdesktop.swingx.table.DatePickerCellEditor
import java.awt.event.ActionListener
import java.util.Date
import java.util.Locale
import javax.swing.BorderFactory

class MongoDatePickerCellEditor : DatePickerCellEditor() {
    init {
        dateFormat = DateUtils.utcDateTime(Locale.getDefault())
        datePicker = DateTimePicker.create()
        datePicker.editor.border = BorderFactory.createEmptyBorder(0, 1, 0, 1)
        datePicker.editor.isEditable = false
    }

    override fun getValueAsDate(value: Any): Date {
        val descriptor = value as MongoNodeDescriptor
        return super.getValueAsDate(descriptor.value)
    }

    fun addActionListener(actionListener: ActionListener?) {
        datePicker.addActionListener(actionListener)
    }
}