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

import com.gg.plugins.mongo.action.pagination.PaginationAction
import com.gg.plugins.mongo.action.result.AddMongoDocumentAction
import com.gg.plugins.mongo.action.result.CloseFindEditorAction
import com.gg.plugins.mongo.action.result.CopyAllAction
import com.gg.plugins.mongo.action.result.CopyNodeAction
import com.gg.plugins.mongo.action.result.DeleteMongoDocumentAction
import com.gg.plugins.mongo.action.result.EditMongoDocumentAction
import com.gg.plugins.mongo.action.result.EnableAggregateAction
import com.gg.plugins.mongo.action.result.ExecuteQuery
import com.gg.plugins.mongo.action.result.GoToMongoDocumentAction
import com.gg.plugins.mongo.action.result.NavigateBackwardAction
import com.gg.plugins.mongo.action.result.OpenFindAction
import com.gg.plugins.mongo.action.result.ViewAsTableAction
import com.gg.plugins.mongo.action.result.ViewAsTreeAction
import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.model.MongoCollection
import com.gg.plugins.mongo.model.MongoCollectionResult
import com.gg.plugins.mongo.model.MongoDatabase
import com.gg.plugins.mongo.model.MongoQueryOptions
import com.gg.plugins.mongo.model.MongoServer
import com.gg.plugins.mongo.model.Pagination
import com.gg.plugins.mongo.model.ResultsPerPage
import com.gg.plugins.mongo.model.navigation.Navigation
import com.gg.plugins.mongo.service.MongoService
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.TreeExpander
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.LoadingDecorator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Disposer
import com.intellij.ui.GuiUtils
import com.intellij.ui.NumberDocument
import com.intellij.ui.PopupHandler
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.util.ui.UIUtil
import org.bson.Document
import org.bson.types.ObjectId
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class MongoPanel(
    private val project: Project,
    private val mongoService: MongoService,
    val configuration: ServerConfiguration,
    private val navigation: Navigation
) : JPanel(), Disposable {
    private val loadingDecorator: LoadingDecorator
    private val rowLimitField = JTextField()
    private val rowCountLabel = JBLabel()
    private val pageNumberLabel = JBLabel()
    val resultPanel: MongoResultPanel
    private val queryPanel: QueryPanel
    private val pagination: Pagination = Pagination()
    lateinit var rootPanel: JPanel
    lateinit var splitter: Splitter
    lateinit var toolBar: JPanel
    lateinit var errorPanel: JPanel
    lateinit var paginationPanel: JPanel
    private var currentResults: MongoCollectionResult?

    init {
        currentResults = MongoCollectionResult(navigation.currentWayPoint?.label)
        errorPanel.layout = BorderLayout()
        queryPanel = QueryPanel(project)
        queryPanel.isVisible = false
        resultPanel = createResultPanel(project)
        loadingDecorator = LoadingDecorator(resultPanel, this, 0)
        splitter.orientation = true
        splitter.proportion = 0.2f
        splitter.secondComponent = loadingDecorator.component
        layout = BorderLayout()
        add(rootPanel)
        initToolBar()
        initPaginationPanel()
        pagination.addSetPageListener { showResults(true) }
        pagination.addSetPageListener {
            currentResults?.let {
                pagination.setTotalDocuments(it.totalDocumentNumber)
                if (ResultsPerPage.ALL == pagination.resultsPerPage) {
                    pageNumberLabel.isVisible = false
                } else {
                    pageNumberLabel.text = String.format(
                        "Page %d/%d",
                        pagination.pageNumber,
                        pagination.totalPageNumber
                    )
                    pageNumberLabel.isVisible = true
                }
            }
        }
    }

    private fun createResultPanel(project: Project): MongoResultPanel {
        return MongoResultPanel(project, object : MongoDocumentOperations {
            override fun getMongoDocument(_id: ObjectId): Document? {
                return mongoService.findMongoDocument(
                    configuration,
                    navigation.currentWayPoint?.collection!!,
                    _id
                )
            }

            override fun deleteMongoDocument(_id: ObjectId) {
                mongoService.delete(configuration, navigation.currentWayPoint?.collection!!, _id)
                executeQuery()
            }

            override fun updateMongoDocument(mongoDocument: Document) {
                mongoService.update(configuration, navigation.currentWayPoint?.collection!!, mongoDocument)
                executeQuery()
            }

            override fun getReferenceDocument(collection: String, _id: Any, database: String?): Document? {
                return mongoService.findMongoDocument(
                    configuration,
                    MongoCollection(
                        collection,
                        when {
                            database.isNullOrEmpty() -> navigation.currentWayPoint?.collection!!.parentDatabase
                            else -> MongoDatabase(database, MongoServer(configuration))
                        }
                    ),
                    _id
                )
            }
        })
    }

    private fun initToolBar() {
        toolBar.layout = BorderLayout()
        val rowLimitPanel = createRowLimitPanel()
        toolBar.add(rowLimitPanel, BorderLayout.WEST)
        val actionToolBarComponent = createResultActionsComponent()
        toolBar.add(actionToolBarComponent, BorderLayout.CENTER)
        val viewToolbarComponent = createSelectViewActionsComponent()
        toolBar.add(viewToolbarComponent, BorderLayout.EAST)
    }

    private fun initPaginationPanel() {
        paginationPanel.layout = BorderLayout()
        val actionToolbarComponent = createPaginationActionsComponent()
        paginationPanel.add(actionToolbarComponent, BorderLayout.CENTER)
        val panel = JPanel()
        panel.add(pageNumberLabel)
        panel.add(GuiUtils.createVerticalStrut())
        panel.add(rowCountLabel)
        paginationPanel.add(panel, BorderLayout.EAST)
    }

    private fun createRowLimitPanel(): JPanel {
        rowLimitField.text = configuration.defaultRowLimit.toString()
        rowLimitField.columns = 5
        rowLimitField.document = NumberDocument()
        rowLimitField.text = configuration.defaultRowLimit.toString()
        val rowLimitPanel: JPanel = NonOpaquePanel()
        rowLimitPanel.add(JLabel("Row limit:"), BorderLayout.WEST)
        rowLimitPanel.add(rowLimitField, BorderLayout.CENTER)
        rowLimitPanel.add(Box.createHorizontalStrut(5), BorderLayout.EAST)
        return rowLimitPanel
    }

    private fun createResultActionsComponent(): JComponent {
        val actionResultGroup = DefaultActionGroup("MongoResultGroup", true)
        actionResultGroup.add(ExecuteQuery(this))
        actionResultGroup.add(OpenFindAction(this))
        actionResultGroup.add(EnableAggregateAction(queryPanel))
        actionResultGroup.addSeparator()
        actionResultGroup.add(AddMongoDocumentAction(resultPanel))
        actionResultGroup.add(EditMongoDocumentAction(resultPanel))
        actionResultGroup.add(DeleteMongoDocumentAction(resultPanel))
        actionResultGroup.add(CopyAllAction(resultPanel))
        actionResultGroup.addSeparator()
        actionResultGroup.add(NavigateBackwardAction(this))
        addBasicTreeActions(actionResultGroup)
        actionResultGroup.add(CloseFindEditorAction(this))
        //TODO Duplicate
        val actionToolBar =
            ActionManager.getInstance().createActionToolbar("MongoResultGroupActions", actionResultGroup, true)
        actionToolBar.targetComponent = actionToolBar.component
        actionToolBar.layoutPolicy = ActionToolbar.AUTO_LAYOUT_POLICY
        val actionToolBarComponent = actionToolBar.component
        actionToolBarComponent.border = null
        actionToolBarComponent.isOpaque = false
        return actionToolBarComponent
    }

    private fun createPaginationActionsComponent(): JComponent {
        val actionResultGroup = DefaultActionGroup("MongoPaginationGroup", false)
        actionResultGroup.add(ChangeNbPerPageActionComponent { PaginationPopupComponent(pagination).initUi() })
        actionResultGroup.add(PaginationAction.Previous(pagination))
        actionResultGroup.add(PaginationAction.Next(pagination))
        //TODO Duplicate
        val actionToolBar = ActionManager.getInstance().createActionToolbar(
            "MongoPaginationGroupActions", actionResultGroup,
            true
        )
        actionToolBar.targetComponent = actionToolBar.component
        actionToolBar.layoutPolicy = ActionToolbar.AUTO_LAYOUT_POLICY
        val actionToolBarComponent = actionToolBar.component
        actionToolBarComponent.border = null
        actionToolBarComponent.isOpaque = false
        return actionToolBarComponent
    }

    private fun createSelectViewActionsComponent(): JComponent {
        val viewSelectGroup = DefaultActionGroup("MongoViewSelectGroup", false)
        viewSelectGroup.add(ViewAsTreeAction(this))
        viewSelectGroup.add(ViewAsTableAction(this))
        //TODO Duplicate
        val viewToolbar =
            ActionManager.getInstance().createActionToolbar("MongoViewSelectedActions", viewSelectGroup, true)
        viewToolbar.targetComponent = viewToolbar.component
        viewToolbar.layoutPolicy = ActionToolbar.AUTO_LAYOUT_POLICY
        val viewToolbarComponent = viewToolbar.component
        viewToolbarComponent.border = null
        viewToolbarComponent.isOpaque = false
        return viewToolbarComponent
    }

    private fun addBasicTreeActions(actionResultGroup: DefaultActionGroup) {
        val treeExpander: TreeExpander = object : TreeExpander {
            override fun expandAll() {
                resultPanel.expandAll()
            }

            override fun canExpand(): Boolean {
                return true
            }

            override fun collapseAll() {
                resultPanel.collapseAll()
            }

            override fun canCollapse(): Boolean {
                return true
            }
        }
        val actionsManager = CommonActionsManager.getInstance()
        val expandAllAction = actionsManager.createExpandAllAction(treeExpander, resultPanel)
        val collapseAllAction = actionsManager.createCollapseAllAction(treeExpander, resultPanel)
        Disposer.register(this) {
            collapseAllAction.unregisterCustomShortcutSet(resultPanel)
            expandAllAction.unregisterCustomShortcutSet(resultPanel)
        }
        actionResultGroup.addSeparator()
        actionResultGroup.add(expandAllAction)
        actionResultGroup.add(collapseAllAction)
    }

    val currentWayPoint: Navigation.WayPoint?
        get() = navigation.currentWayPoint

    fun showResults() {
        showResults(false)
    }

    private fun showResults(cached: Boolean) {
        executeQuery(cached, navigation.currentWayPoint)
    }

    //TODO refactor
    private fun executeQuery(useCachedResults: Boolean, wayPoint: Navigation.WayPoint?) {
        errorPanel.isVisible = false
        validateQuery()
        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(project, "Get documents from " + wayPoint?.label) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        UIUtil.invokeLaterIfNeeded { loadingDecorator.startLoading(false) }
                        val queryOptions = wayPoint?.queryOptions
                        if (!useCachedResults) {
                            currentResults = mongoService.findMongoDocuments(
                                configuration,
                                wayPoint?.collection!!,
                                queryOptions
                            )
                        }
                        UIUtil.invokeLaterIfNeeded {
                            currentResults?.let {
                                resultPanel.updateResultView(it, pagination)
                                rowCountLabel.text = String.format(
                                    "%s documents",
                                    it.documents.size
                                )
                            }
                            initActions(resultPanel.resultTreeTableView)
                        }
                    } catch (ex: Exception) {
                        UIUtil.invokeLaterIfNeeded {
                            errorPanel.invalidate()
                            errorPanel.removeAll()
                            errorPanel.add(ErrorPanel(ex), BorderLayout.CENTER)
                            errorPanel.validate()
                            errorPanel.isVisible = true
                        }
                    } finally {
                        UIUtil.invokeLaterIfNeeded { loadingDecorator.stopLoading() }
                    }
                }
            })
    }

    private fun validateQuery() {
        queryPanel.validateQuery()
    }

    private fun initActions(resultTreeTableView: JsonTreeTableView?) {
        resultTreeTableView!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(mouseEvent: MouseEvent) {
                if (mouseEvent.clickCount == 2 && resultPanel.isSelectedNodeId) {
                    resultPanel.editSelectedMongoDocument()
                }
            }
        })
        val actionPopupGroup = DefaultActionGroup("MongoResultPopupGroup", true)
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(EditMongoDocumentAction(resultPanel))
            actionPopupGroup.add(DeleteMongoDocumentAction(resultPanel))
            actionPopupGroup.add(CopyNodeAction(resultPanel))
            actionPopupGroup.add(GoToMongoDocumentAction(this))
        }
        PopupHandler.installPopupMenu(resultTreeTableView, actionPopupGroup, "POPUP")
    }

    fun goToReferencedDocument() {
        val selectedDBRef = resultPanel.selectedDBRef
        val referencedDocument = resultPanel.getReferencedDocument(selectedDBRef)
        if (referencedDocument == null) {
            Messages.showErrorDialog(this, "Referenced document was not found")
            return
        }
        if (selectedDBRef != null) {
            navigation.addNewWayPoint(
                MongoCollection(
                    selectedDBRef.collectionName,
                    if (selectedDBRef.databaseName.isNullOrEmpty())
                        navigation.currentWayPoint?.collection!!.parentDatabase
                    else
                        MongoDatabase(
                            selectedDBRef.databaseName as String, MongoServer(
                                configuration
                            )
                        )
                ),
                MongoQueryOptions().setFilter(Document("_id", selectedDBRef.id))
            )
        }
        executeQuery(
            false, navigation.currentWayPoint
        )
    }

    //TODO refactor
    fun executeQuery() {
        navigation.currentWayPoint?.let {
            it.queryOptions = queryPanel.getQueryOptions(rowLimitField.text)
            val queryOptions = queryPanel.getQueryOptions(rowLimitField.text)
            it.queryOptions = queryOptions
            executeQuery(false, it)
        }
    }

    override fun dispose() {
        resultPanel.dispose()
    }

    fun openFindEditor() {
        queryPanel.isVisible = true
        splitter.firstComponent = queryPanel
        UIUtil.invokeLaterIfNeeded { focusOnEditor() }
    }

    fun focusOnEditor() {
        queryPanel.requestFocusOnEditor()
    }

    fun closeFindEditor() {
        splitter.firstComponent = null
        queryPanel.isVisible = false
    }

    val isFindEditorOpened: Boolean
        get() = splitter.firstComponent === queryPanel

    fun setViewMode(viewMode: MongoResultPanel.ViewMode) {
        if (resultPanel.currentViewMode == viewMode) {
            return
        }
        resultPanel.currentViewMode = viewMode
        executeQuery(true, navigation.currentWayPoint)
    }

    fun navigateBackward() {
        navigation.moveBackward()
        executeQuery(false, navigation.currentWayPoint)
    }

    fun hasNavigationHistory(): Boolean {
        return navigation.wayPoints.size > 1
    }

    interface MongoDocumentOperations {
        fun getMongoDocument(_id: ObjectId): Document?
        fun deleteMongoDocument(_id: ObjectId)
        fun updateMongoDocument(mongoDocument: Document)
        fun getReferenceDocument(collection: String, _id: Any, database: String?): Document?
    }

    private class ChangeNbPerPageActionComponent(private val myComponentCreator: Computable<JComponent>) :
        DumbAwareAction(), CustomComponentAction {
        override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
            return myComponentCreator.compute()
        }

        override fun actionPerformed(e: AnActionEvent) {}
    }
}