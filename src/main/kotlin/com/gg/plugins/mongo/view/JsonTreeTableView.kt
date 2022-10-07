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

import com.gg.plugins.mongo.model.JsonTreeNode
import com.gg.plugins.mongo.view.nodedescriptor.MongoNodeDescriptor
import com.gg.plugins.mongo.view.renderer.MongoKeyCellRenderer
import com.gg.plugins.mongo.view.renderer.MongoValueCellRenderer
import com.gg.plugins.mongo.view.table.MongoDatePickerCellEditor
import com.gg.plugins.mongo.view.table.MongoValueCellEditor
import com.intellij.ui.TreeTableSpeedSearch
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns
import com.intellij.ui.treeStructure.treetable.TreeTable
import com.intellij.ui.treeStructure.treetable.TreeTableModel
import com.intellij.util.ui.ColumnInfo
import com.mongodb.lang.Nullable
import org.bson.Document
import org.bson.types.ObjectId
import java.util.Date
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

class JsonTreeTableView(rootNode: TreeNode?, private val columns: Array<ColumnInfo<JsonTreeNode, out Any>>) :
    TreeTable(ListTreeTableModelOnColumns(rootNode, columns)) {
    init {
        val tree = tree
        tree.showsRootHandles = true
        tree.isRootVisible = false
        setTreeCellRenderer(MongoKeyCellRenderer())
        TreeTableSpeedSearch(this) { path: TreePath ->
            val node = path.lastPathComponent as JsonTreeNode
            val descriptor = node.descriptor
            descriptor.key
        }
    }

    override fun getCellRenderer(row: Int, column: Int): TableCellRenderer {
        val treePath = tree.getPathForRow(row) ?: return super.getCellRenderer(row, column)
        val node = treePath.lastPathComponent as JsonTreeNode
        val renderer = columns[column].getRenderer(node)
        return renderer ?: super.getCellRenderer(row, column)
    }

    override fun getCellEditor(row: Int, column: Int): TableCellEditor {
        val treePath = tree.getPathForRow(row) ?: return super.getCellEditor(row, column)
        val node = treePath.lastPathComponent as JsonTreeNode
        val editor = columns[column].getEditor(node)
        return editor ?: super.getCellEditor(row, column)
    }

    private class ReadOnlyValueColumnInfo :
        ColumnInfo<JsonTreeNode, MongoNodeDescriptor?>("Value") {
        private val myRenderer: TableCellRenderer = MongoValueCellRenderer()
        override fun valueOf(treeNode: JsonTreeNode): MongoNodeDescriptor {
            return treeNode.descriptor
        }

        override fun isCellEditable(o: JsonTreeNode): Boolean {
            return false
        }

        override fun getRenderer(o: JsonTreeNode): TableCellRenderer {
            return myRenderer
        }
    }

    private class WritableColumnInfo : ColumnInfo<JsonTreeNode, Any?>("Value") {
        private val myRenderer: TableCellRenderer = MongoValueCellRenderer()
        private val defaultEditor: TableCellEditor = MongoValueCellEditor()
        override fun valueOf(treeNode: JsonTreeNode): Any {
            return treeNode.descriptor
        }

        override fun isCellEditable(treeNode: JsonTreeNode): Boolean {
            val value = treeNode.descriptor.value
            return if (value is Document || value is List<*>) {
                false
            } else value !is ObjectId
        }

        override fun setValue(treeNode: JsonTreeNode, value: Any?) {
            treeNode.descriptor.value = value
        }

        override fun getRenderer(o: JsonTreeNode): TableCellRenderer {
            return myRenderer
        }

        @Nullable
        override fun getEditor(treeNode: JsonTreeNode): TableCellEditor {
            val value = treeNode.descriptor.value
            return if (value is Date) {
                buildDateCellEditor(treeNode)
            } else defaultEditor
        }

        companion object {
            private fun buildDateCellEditor(treeNode: JsonTreeNode): MongoDatePickerCellEditor {
                val dateEditor = MongoDatePickerCellEditor()
                //  Note from dev: Quite ugly because when clicking on the button to open popup calendar, stopCellEdition
                //  is invoked.
                //                 From that point, impossible to set the selected data in the node description
                dateEditor.addActionListener {
                    treeNode.descriptor.value = dateEditor.cellEditorValue
                }
                return dateEditor
            }
        }
    }

    companion object {
        private val KEY: ColumnInfo<JsonTreeNode, *> = object : ColumnInfo<JsonTreeNode, Any?>("Key") {
            override fun valueOf(obj: JsonTreeNode): Any {
                return obj.descriptor
            }

            override fun getColumnClass(): Class<*> {
                return TreeTableModel::class.java
            }

            override fun isCellEditable(o: JsonTreeNode): Boolean {
                return false
            }
        }
        private val READONLY_VALUE: ColumnInfo<JsonTreeNode, *> = ReadOnlyValueColumnInfo()
        val COLUMNS_FOR_READING = arrayOf(KEY, READONLY_VALUE)
        private val WRITABLE_VALUE: ColumnInfo<JsonTreeNode, *> = WritableColumnInfo()
        val COLUMNS_FOR_WRITING = arrayOf(KEY, WRITABLE_VALUE)
    }
}