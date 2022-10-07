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

import com.gg.plugins.mongo.model.JsonTableUtils
import com.gg.plugins.mongo.model.JsonTreeNode
import com.gg.plugins.mongo.model.JsonTreeUtils
import com.gg.plugins.mongo.model.MongoCollectionResult
import com.gg.plugins.mongo.model.Pagination
import com.gg.plugins.mongo.model.ResultsPerPage
import com.gg.plugins.mongo.service.Notifier
import com.gg.plugins.mongo.view.edition.MongoEditionDialog
import com.gg.plugins.mongo.view.nodedescriptor.MongoKeyValueDescriptor
import com.gg.plugins.mongo.view.nodedescriptor.MongoNodeDescriptor
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.JBCardLayout
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import com.mongodb.DBRef
import org.apache.commons.lang.StringUtils
import org.bson.Document
import org.bson.types.ObjectId
import java.awt.BorderLayout
import java.util.LinkedList
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode

class MongoResultPanel(
    private val project: Project,
    private val mongoDocumentOperations: MongoPanel.MongoDocumentOperations
) : JPanel(), Disposable {
    private val notifier: Notifier = Notifier.getInstance(project)
    private val resultTreePanel: JPanel
    private val actionCallback: ActionCallback
    var resultTreeTableView: JsonTreeTableView? = null
    lateinit var mainPanel: JPanel
    lateinit var containerPanel: JPanel
    var currentViewMode = ViewMode.TREE
    val selectedDBRef: DBRef?
        get() {
            val tree = resultTreeTableView!!.tree
            val treeNode = tree.lastSelectedPathComponent as JsonTreeNode
            val descriptor: MongoNodeDescriptor = treeNode.descriptor
            var selectedDBRef: DBRef? = null
            if (descriptor is MongoKeyValueDescriptor) {
                if (descriptor.value is DBRef) {
                    selectedDBRef = descriptor.value as DBRef?
                } else {
                    val parentNode = treeNode.parent as JsonTreeNode
                    val parentDescriptor: MongoNodeDescriptor = parentNode.descriptor
                    if (parentDescriptor.value is DBRef) {
                        selectedDBRef = parentDescriptor.value as DBRef?
                    }
                }
            }
            return selectedDBRef
        }

    init {
        layout = BorderLayout()
        add(mainPanel, BorderLayout.CENTER)
        resultTreePanel = JPanel(BorderLayout())
        containerPanel.layout = JBCardLayout()
        containerPanel.add(resultTreePanel)
        actionCallback = object : ActionCallback {
            override fun onOperationSuccess(label: String?, message: String?) {
                notifier.notifyInfo(message)
            }

            override fun onOperationFailure(exception: Exception) {
                notifier.notifyError(exception.message)
            }
        }
        // Disposer.register(project, this)
    }

    fun updateResultView(mongoCollectionResult: MongoCollectionResult, pagination: Pagination) {
        if (ViewMode.TREE == currentViewMode) {
            updateResultTreeTable(mongoCollectionResult, pagination)
        } else {
            updateResultTable(mongoCollectionResult)
        }
    }

    private fun updateResultTreeTable(mongoCollectionResult: MongoCollectionResult, pagination: Pagination) {
        resultTreeTableView = JsonTreeTableView(
            JsonTreeUtils.buildJsonTree(
                mongoCollectionResult.collectionName,
                extractDocuments(pagination, mongoCollectionResult.documents),
                pagination.startIndex
            ), JsonTreeTableView.COLUMNS_FOR_READING
        )
        resultTreeTableView!!.name = "resultTreeTable"
        displayResult(resultTreeTableView!!)
        UIUtil.invokeAndWaitIfNeeded(Runnable { TreeUtil.expand(resultTreeTableView!!.tree, 2) })
    }

    private fun updateResultTable(mongoCollectionResult: MongoCollectionResult) {
        displayResult(JsonTableView(JsonTableUtils.buildJsonTable(mongoCollectionResult)))
    }

    private fun displayResult(tableView: JComponent) {
        resultTreePanel.invalidate()
        resultTreePanel.removeAll()
        resultTreePanel.add(JBScrollPane(tableView))
        resultTreePanel.validate()
    }

    fun editSelectedMongoDocument() {
        val mongoDocument = selectedMongoDocument ?: return
        MongoEditionDialog.create(project, mongoDocumentOperations, actionCallback)
            .initDocument(mongoDocument).show()
    }

    private val selectedMongoDocument: Document?
        get() {
            val tree = resultTreeTableView!!.tree
            val treeNode = tree.lastSelectedPathComponent as JsonTreeNode
            val descriptor = treeNode.descriptor
            if (descriptor is MongoKeyValueDescriptor) {
                if (StringUtils.equals(descriptor.key, "_id")) {
                    return mongoDocumentOperations.getMongoDocument(descriptor.value as ObjectId)
                }
            }
            return null
        }

    fun addMongoDocument() {
        MongoEditionDialog.create(project, mongoDocumentOperations, actionCallback).initDocument(null).show()
    }

    val isSelectedNodeId: Boolean
        get() = objectIdDescriptorFromSelectedDocument != null
    private val objectIdDescriptorFromSelectedDocument: MongoKeyValueDescriptor?
        get() {
            if (resultTreeTableView == null) {
                return null
            }
            val tree = resultTreeTableView!!.tree
            val treeNode = tree.lastSelectedPathComponent as? JsonTreeNode
            val descriptor = treeNode?.descriptor as? MongoKeyValueDescriptor ?: return null
            return if ("_id" != descriptor.key && descriptor.value !is ObjectId) {
                null
            } else descriptor
        }

    fun expandAll() {
        TreeUtil.expandAll(resultTreeTableView!!.tree)
    }

    fun collapseAll() {
        val tree = resultTreeTableView!!.tree
        TreeUtil.collapseAll(tree, 1)
    }

    val stringifiedResult: String
        get() {
            val rootNode = resultTreeTableView!!.tree.model.root as JsonTreeNode
            return stringifyResult(rootNode)
        }

    private fun stringifyResult(selectedResultNode: DefaultMutableTreeNode): String {
        return IntStream.range(0, selectedResultNode.childCount)
            .mapToObj { i: Int -> getDescriptor(i, selectedResultNode).pretty() }
            .collect(Collectors.joining(",", "[", "]"))
    }

    val selectedNodeStringifiedValue: String?
        get() {
            val lastSelectedResultNode = selectedNode
            val userObject = lastSelectedResultNode?.descriptor
            return userObject?.pretty()
        }
    val selectedNode: JsonTreeNode?
        get() = resultTreeTableView!!.tree.lastSelectedPathComponent as JsonTreeNode?

    override fun dispose() {
        resultTreeTableView = null
    }

    fun getReferencedDocument(selectedDBRef: DBRef?): Document? {
        return mongoDocumentOperations.getReferenceDocument(
            selectedDBRef!!.collectionName,
            selectedDBRef.id,
            selectedDBRef.databaseName
        )
    }

    fun deleteSelectedMongoDocument() {
        val descriptor = objectIdDescriptorFromSelectedDocument ?: return
        val objectId = descriptor.value as ObjectId
        mongoDocumentOperations.deleteMongoDocument(objectId)
        notifier.notifyInfo("Document with _id=$objectId deleted.")
    }

    enum class ViewMode {
        TREE, TABLE
    }

    interface ActionCallback {
        fun onOperationSuccess(label: String?, message: String?)
        fun onOperationFailure(exception: Exception)
    }

    companion object {
        private fun extractDocuments(pagination: Pagination, documents: List<Document?>?): List<Document?>? {
            if (ResultsPerPage.ALL == pagination.resultsPerPage) {
                return documents
            }
            if (pagination.countPerPage >= documents!!.size) {
                return documents
            }
            val startIndex = pagination.startIndex
            val endIndex = startIndex + pagination.countPerPage
            return IntStream.range(startIndex, endIndex)
                .mapToObj { index: Int -> documents[index] }
                .collect(
                    Collectors.toCollection { LinkedList() }
                )
        }

        private fun getDescriptor(i: Int, parentNode: DefaultMutableTreeNode): MongoNodeDescriptor {
            val childNode = parentNode.getChildAt(i) as DefaultMutableTreeNode
            return childNode.userObject as MongoNodeDescriptor
        }
    }
}