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

import com.gg.plugins.mongo.model.JsonTreeNode
import com.intellij.ui.treeStructure.treetable.TreeTable
import java.awt.Component
import javax.swing.DefaultCellEditor
import javax.swing.JTable
import javax.swing.JTextField

class MongoValueCellEditor : DefaultCellEditor(JTextField()) {
    override fun getCellEditorValue(): Any {
        return (component as JTextField).text
    }

    override fun getTableCellEditorComponent(
        table: JTable,
        value: Any,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        val stringEditor = component as JTextField
        val jsonNode = (table as TreeTable).tree.getPathForRow(row).lastPathComponent as JsonTreeNode
        stringEditor.text = jsonNode.descriptor.value.toString()
        return stringEditor
    }
}