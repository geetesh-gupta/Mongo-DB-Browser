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

import com.gg.plugins.mongo.action.edition.AddKeyAction
import com.gg.plugins.mongo.action.edition.AddValueAction
import com.gg.plugins.mongo.action.edition.DeleteKeyAction
import com.gg.plugins.mongo.model.JsonTreeNode
import com.gg.plugins.mongo.model.JsonTreeUtils
import com.gg.plugins.mongo.utils.MongoUtils
import com.gg.plugins.mongo.view.JsonTreeTableView
import com.gg.plugins.mongo.view.MongoPanel
import com.gg.plugins.mongo.view.MongoResultPanel
import com.gg.plugins.mongo.view.nodedescriptor.MongoKeyValueDescriptor
import com.gg.plugins.mongo.view.nodedescriptor.MongoNodeDescriptor
import com.gg.plugins.mongo.view.nodedescriptor.MongoValueDescriptor
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.tree.TreeUtil
import org.apache.commons.lang.StringUtils
import org.bson.Document
import java.awt.BorderLayout
import java.util.Enumeration
import java.util.LinkedList
import javax.swing.JPanel
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class MongoEditionPanel internal constructor(
    private val mongoDocumentOperations: MongoPanel.MongoDocumentOperations,
    private val actionCallback: MongoResultPanel.ActionCallback
) : JPanel(BorderLayout()) {
    private var editTableView: JsonTreeTableView? = null
    fun save(): Boolean {
        return try {
            val mongoDocument = buildMongoDocument()
            mongoDocumentOperations.updateMongoDocument(mongoDocument)
            actionCallback.onOperationSuccess(
                "Document saved",
                "Document " + mongoDocument.toJson(MongoUtils.DOCUMENT_CODEC) + " saved."
            )
            true
        } catch (exception: Exception) {
            actionCallback.onOperationFailure(exception)
            false
        }
    }

    private fun buildMongoDocument(): Document {
        val rootNode = editTableView!!.tree.model.root as JsonTreeNode
        return JsonTreeUtils.buildDocumentObject(rootNode)
    }

    fun updateEditionTree(mongoDocument: Document?) {
        editTableView = JsonTreeTableView(
            JsonTreeUtils.buildJsonTree(mongoDocument),
            JsonTreeTableView.COLUMNS_FOR_WRITING
        )
        editTableView!!.name = "editionTreeTable"
        TreeUtil.expand(editTableView!!.tree, 2)
        add(JBScrollPane(editTableView), BorderLayout.CENTER)
        buildPopupMenu()
    }

    private fun buildPopupMenu() {
        val actionPopupGroup = DefaultActionGroup("MongoEditorPopupGroup", true)
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(AddKeyAction(this))
            actionPopupGroup.add(AddValueAction(this))
            actionPopupGroup.add(DeleteKeyAction(this))
        }
        PopupHandler.installPopupMenu(editTableView!!, actionPopupGroup, "POPUP")
    }

    fun containsKey(key: String?): Boolean {
        val parentNode = parentNode ?: return false
        val children: Enumeration<*> = parentNode.children()
        while (children.hasMoreElements()) {
            val childNode = children.nextElement() as JsonTreeNode
            val descriptor = childNode.descriptor
            if (descriptor is MongoKeyValueDescriptor) {
                if (StringUtils.equals(key, descriptor.key)) {
                    return true
                }
            }
        }
        return false
    }

    private val parentNode: JsonTreeNode?
        get() {
            val lastPathComponent = selectedNode ?: return null
            return lastPathComponent.parent as JsonTreeNode
        }
    val selectedNode: JsonTreeNode?
        get() = editTableView!!.tree.lastSelectedPathComponent as JsonTreeNode?

    fun addKey(key: String?, value: Any?) {
        val node: MutableList<TreeNode> = LinkedList()
        val treeNode = JsonTreeNode(MongoKeyValueDescriptor.createDescriptor(key, value))
        if (value is Document) {
            JsonTreeUtils.processDocument(treeNode, value as Document?)
        } else if (value is List<*>) {
            JsonTreeUtils.processObjectList(treeNode, value)
        }
        node.add(treeNode)
        val treeModel = editTableView!!.tree.model as DefaultTreeModel
        var parentNode = parentNode
        if (parentNode == null) {
            parentNode = treeModel.root as JsonTreeNode
        }
        TreeUtil.addChildrenTo(parentNode, node)
        treeModel.reload(parentNode)
    }

    fun addValue(value: Any?) {
        val node: MutableList<TreeNode> = LinkedList()
        val currentSelectionNode = selectedNode ?: return
        val nodeToAttach: JsonTreeNode = if (doesKeyDescriptionHaveEmptyArrayValue(currentSelectionNode.descriptor)) {
            currentSelectionNode
        } else {
            currentSelectionNode.parent as JsonTreeNode
        }
        val treeNode = JsonTreeNode(MongoValueDescriptor.createDescriptor(nodeToAttach.childCount, value))
        if (value is Document) {
            JsonTreeUtils.processDocument(treeNode, value as Document?)
        }
        node.add(treeNode)
        val treeModel = editTableView!!.tree.model as DefaultTreeModel
        TreeUtil.addChildrenTo(nodeToAttach, node)
        treeModel.reload(nodeToAttach)
    }

    private fun doesKeyDescriptionHaveEmptyArrayValue(descriptor: MongoNodeDescriptor?): Boolean {
        if (descriptor is MongoKeyValueDescriptor) {
            val value = descriptor.value
            if (value is List<*>) {
                return value.isEmpty()
            }
            return false
        }
        return false
    }

    fun canAddValue(): Boolean {
        val selectedNode = selectedNode ?: return false
        val descriptor = selectedNode.descriptor
        return descriptor is MongoValueDescriptor || doesKeyDescriptionHaveEmptyArrayValue(descriptor)
    }

    fun removeSelectedKey() {
        selectedNode ?: return
        TreeUtil.removeSelected(editTableView!!.tree)
    }
}