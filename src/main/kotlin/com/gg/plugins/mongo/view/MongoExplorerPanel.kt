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

import com.gg.plugins.mongo.action.explorer.AddServerAction
import com.gg.plugins.mongo.action.explorer.DeleteAction
import com.gg.plugins.mongo.action.explorer.DuplicateServerAction
import com.gg.plugins.mongo.action.explorer.EditServerAction
import com.gg.plugins.mongo.action.explorer.MongoConsoleAction
import com.gg.plugins.mongo.action.explorer.OpenPluginSettingsAction
import com.gg.plugins.mongo.action.explorer.RefreshServerAction
import com.gg.plugins.mongo.action.explorer.ViewCollectionValuesAction
import com.gg.plugins.mongo.config.ConfigurationException
import com.gg.plugins.mongo.config.MongoConfiguration
import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.editor.MongoFileSystem
import com.gg.plugins.mongo.editor.MongoObjectFile
import com.gg.plugins.mongo.model.MongoCollection
import com.gg.plugins.mongo.model.MongoConstants
import com.gg.plugins.mongo.model.MongoDatabase
import com.gg.plugins.mongo.model.MongoQueryOptions
import com.gg.plugins.mongo.model.MongoServer
import com.gg.plugins.mongo.model.MongoTreeModel
import com.gg.plugins.mongo.model.navigation.Navigation
import com.gg.plugins.mongo.service.MongoService
import com.gg.plugins.mongo.service.Notifier
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.TreeExpander
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.util.Disposer
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.GuiUtils
import com.intellij.ui.PopupHandler
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.containers.Convertor
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Graphics
import java.awt.event.MouseEvent
import java.net.URL
import java.util.TreeSet
import java.util.function.Consumer
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.plaf.basic.BasicTreeUI
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class MongoExplorerPanel(private val project: Project) : JPanel(), Disposable {
    private val mongoService: MongoService = MongoService.getInstance(project)
    private val notifier: Notifier = Notifier.getInstance(project)
    private val treePanel: JPanel = JPanel(BorderLayout())
    private val mongoTreeModel: MongoTreeModel
    private val mongoTree: Tree
    lateinit var rootPanel: JPanel
    lateinit var toolBarPanel: JPanel
    lateinit var containerPanel: JPanel

    init {
        treePanel.layout = BorderLayout()
        mongoTree = createTree()
        mongoTreeModel = MongoTreeModel()
        mongoTree.model = mongoTreeModel
        layout = BorderLayout()
        treePanel.add(JBScrollPane(mongoTree), BorderLayout.CENTER)
        val splitter = Splitter(true, 0.6f)
        splitter.firstComponent = treePanel
        containerPanel.add(splitter, BorderLayout.CENTER)
        add(rootPanel, BorderLayout.CENTER)
        toolBarPanel.layout = BorderLayout()

        loadAllServerConfigurations()
        installActions()
        doubleClickEventHandler()
    }

    private fun createTree(): Tree {
        val tree: Tree = object : Tree() {
            private val myLabel = JLabel(
                String.format(
                    "<html><center>No Mongo server available<br><br>You may use <img src=\"%s\"> to add " +
                            "configuration</center></html>",
                    pluginSettingsUrl
                )
            )

            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                if (serverConfigurations.isNotEmpty()) return
                myLabel.font = font
                myLabel.background = background
                myLabel.foreground = foreground
                val bounds = bounds
                val size = myLabel.preferredSize
                myLabel.setBounds(0, 0, size.width, size.height)
                val x = (bounds.width - size.width) / 2
                val g2 = g.create(bounds.x + x, bounds.y + 20, bounds.width, bounds.height)
                try {
                    myLabel.paint(g2)
                } finally {
                    g2.dispose()
                }
            }
        }
        tree.setUI(object : BasicTreeUI() {
            override fun shouldPaintExpandControl(
                path: TreePath,
                row: Int,
                isExpanded: Boolean,
                hasBeenExpanded: Boolean,
                isLeaf: Boolean
            ): Boolean {
                val node = path.lastPathComponent
                return mongoTreeModel.getChildren(node).isNotEmpty()
            }
        })
        tree.emptyText.clear()
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        tree.name = "mongoTree"
        tree.isRootVisible = false
        tree.cellRenderer = object : ColoredTreeCellRenderer() {
            override fun customizeCellRenderer(
                tree: JTree,
                value: Any,
                selected: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ) {
                when (value) {
                    is MongoServer -> {
                        icon = MongoConstants.MONGO_SERVER
                        toolTipText = "Server: " + value.label
                        append(value.label)
                    }

                    is MongoDatabase -> {
                        icon = MongoConstants.MONGO_DATABASE
                        toolTipText = "Database: " + value.name
                        append(value.name)
                    }

                    is MongoCollection -> {
                        icon = MongoConstants.MONGO_COLLECTION
                        toolTipText = "Collection: " + value.name
                        append(value.name)
                    }
                }
            }
        }
        TreeSpeedSearch(tree, Convertor { treePath: TreePath ->
            val node = treePath.lastPathComponent
            if (node is MongoDatabase) {
                return@Convertor node.name
            }
            if (node is MongoCollection) {
                return@Convertor node.name
            }
            "<empty>"
        })
        return tree
    }

    private val serverConfigurations: Set<ServerConfiguration>
        get() = MongoConfiguration.getInstance(project).serverConfigurations

    private fun loadAllServerConfigurations() {
        mongoService.cleanUpServers()
        for (serverConfiguration in serverConfigurations) {
            addConfiguration(serverConfiguration)
        }
    }

    private fun installActions() {
        val actions = Actions()
        Disposer.register(this) {
            actions.collapseAllAction.unregisterCustomShortcutSet(rootPanel)
            actions.expandAllAction.unregisterCustomShortcutSet(rootPanel)
        }
        val actionGroup = DefaultActionGroup("MongoExplorerGroup", false)
        if (ApplicationManager.getApplication() != null) {
            actionGroup.add(actions.addServerAction)
            actionGroup.add(actions.duplicateServerAction)
            actionGroup.addSeparator()
            actionGroup.add(actions.refreshServerAction)
            actionGroup.add(actions.mongoConsoleAction)
            actionGroup.addSeparator()
            actionGroup.add(actions.expandAllAction)
            actionGroup.add(actions.collapseAllAction)
            actionGroup.addSeparator()
            actionGroup.add(actions.openPluginSettingsAction)
        }
        com.gg.plugins.mongo.utils.GuiUtils.installActionGroupInToolBar(
            actionGroup,
            toolBarPanel,
            ActionManager.getInstance(),
            "MongoExplorerActions",
            true
        )
        val actionPopupGroup = DefaultActionGroup("MongoExplorerPopupGroup", true)
        if (ApplicationManager.getApplication() != null) {
            actionPopupGroup.add(actions.refreshServerAction)
            actionPopupGroup.add(actions.editServerAction)
            actionPopupGroup.add(actions.duplicateServerAction)
            actionPopupGroup.add(actions.deleteAction)
            actionPopupGroup.addSeparator()
            actionPopupGroup.add(actions.viewCollectionValuesAction)
        }
        PopupHandler.installPopupMenu(mongoTree, actionPopupGroup, "POPUP")
    }

    private fun doubleClickEventHandler() {
        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                if (event.source !is JTree) {
                    return false
                }
                val node: Any? = selectedNode
                if (node is MongoServer && node.databases.isEmpty()) {
                    openServer(node)
                } else if (node is MongoDatabase && node.collections.isEmpty()) {
                    loadDatabase(node)
                } else if (node is MongoCollection) {
                    loadSelectedCollectionValues(node)
                }
                return false
            }
        }.installOn(mongoTree)
    }

    fun addConfiguration(serverConfiguration: ServerConfiguration) {
        val mongoServer = mongoTreeModel.addConfiguration(serverConfiguration)
        mongoService.registerServer(mongoServer)
    }

    val selectedNode: Any?
        get() = mongoTree.lastSelectedPathComponent

    fun openServer(mongoServer: MongoServer) {
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project, "Connecting to " + mongoServer.label
        ) {
            override fun run(indicator: ProgressIndicator) {
                mongoTree.setPaintBusy(true)
                ApplicationManager.getApplication().invokeLater {
                    try {
                        val mongoDatabases = mongoService.loadDatabases(
                            mongoServer,
                            mongoServer.configuration
                        )
                        if (mongoDatabases.isEmpty()) {
                            return@invokeLater
                        }
                        mongoServer.databases = mongoDatabases
                        mongoTreeModel.fireTreeStructureChanged(mongoServer)
                    } catch (confEx: ConfigurationException) {
                        mongoServer.status = MongoServer.Status.ERROR
                        val errorMessage = String.format("Error when connecting to %s", mongoServer.label)
                        notifier.notifyError(errorMessage + ": " + confEx.message)
                        UIUtil.invokeLaterIfNeeded {
                            com.gg.plugins.mongo.utils.GuiUtils.showNotification(
                                treePanel,
                                MessageType.ERROR,
                                errorMessage,
                                Balloon.Position.atLeft
                            )
                        }
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    } finally {
                        mongoTree.setPaintBusy(false)
                    }
                }
            }
        })
    }

    fun loadDatabase(mongoDatabase: MongoDatabase) {
        val configuration = mongoDatabase.parentServer.configuration
        val collections = mongoService.loadCollections(mongoDatabase, configuration)
        mongoDatabase.collections = collections as TreeSet<MongoCollection>
        mongoTreeModel.fireTreeStructureChanged(mongoDatabase)
    }

    fun loadSelectedCollectionValues(mongoCollection: MongoCollection) {
        val parentServer = mongoCollection.parentDatabase.parentServer
        val configuration = parentServer.configuration
        val navigation = Navigation()
        val queryOptions = MongoQueryOptions()
        queryOptions.resultLimit = configuration.defaultRowLimit
        navigation.addNewWayPoint(mongoCollection, queryOptions)
        MongoFileSystem.getInstance().openEditor(MongoObjectFile(project, configuration, navigation))
    }

    fun removeNode(node: Any) {
        when (node) {
            is MongoServer -> mongoTreeModel.removeConfiguration(node)
            is MongoDatabase -> {
                mongoService.removeDatabase(node.parentServer.configuration, node)
                openServer(node.parentServer)
            }

            is MongoCollection -> {
                mongoService.removeCollection(node.parentDatabase.parentServer.configuration, node)
                loadDatabase(node.parentDatabase)
            }
        }
        mongoTreeModel.fireTreeStructureChanged(mongoTreeModel.getParent(node))
    }

    fun getServerConfiguration(treeNode: Any?): ServerConfiguration? {
        return mongoTreeModel.getServerConfiguration(treeNode)
    }

    override fun dispose() {}
    private fun expandAll() {
        if (mongoTreeModel.getChildCount(mongoTreeModel.root) > 0) mongoTreeModel.getChildren(mongoTreeModel.root)
            .forEach(Consumer { c: Any? -> mongoTree.expandPath(TreePath(mongoTreeModel.getPathToRoot(c))) })
    }

    fun getTypeOfNode(node: Any?): String {
        return mongoTreeModel.getTypeOfNode(node)
    }

    private fun collapseAll() {
        if (mongoTreeModel.getChildCount(mongoTreeModel.root) > 0) mongoTreeModel.getChildren(mongoTreeModel.root)
            .forEach(Consumer { c: Any? -> mongoTree.collapsePath(TreePath(mongoTreeModel.getPathToRoot(c))) })
    }

    private inner class Actions {
        val refreshServerAction = RefreshServerAction(this@MongoExplorerPanel)
        val addServerAction = AddServerAction(this@MongoExplorerPanel)
        val duplicateServerAction = DuplicateServerAction(this@MongoExplorerPanel)
        val editServerAction = EditServerAction(this@MongoExplorerPanel)
        val deleteAction = DeleteAction(this@MongoExplorerPanel)
        val viewCollectionValuesAction = ViewCollectionValuesAction(this@MongoExplorerPanel)
        val openPluginSettingsAction = OpenPluginSettingsAction()
        val mongoConsoleAction = MongoConsoleAction(this@MongoExplorerPanel)
        val treeExpander: TreeExpander = object : TreeExpander {
            override fun expandAll() {
                this@MongoExplorerPanel.expandAll()
            }

            override fun canExpand(): Boolean {
                return serverConfigurations.isNotEmpty()
            }

            override fun collapseAll() {
                this@MongoExplorerPanel.collapseAll()
            }

            override fun canCollapse(): Boolean {
                return serverConfigurations.isNotEmpty()
            }
        }
        val actionsManager: CommonActionsManager = CommonActionsManager.getInstance()
        val expandAllAction: AnAction = actionsManager.createExpandAllAction(treeExpander, rootPanel)
        val collapseAllAction: AnAction = actionsManager.createCollapseAllAction(treeExpander, rootPanel)
    }

    companion object {
        private val pluginSettingsUrl: URL = GuiUtils::class.java.getResource("/general/add.png")!!
    }
}