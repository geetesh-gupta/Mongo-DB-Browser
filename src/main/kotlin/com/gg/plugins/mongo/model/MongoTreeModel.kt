/*
 * Copyright (c) 2022 Geetesh Gupta.
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
package com.gg.plugins.mongo.model

import com.gg.plugins.mongo.config.ServerConfiguration
import java.util.TreeMap
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

class MongoTreeModel : TreeModel {
    private val listenerList: MutableList<TreeModelListener> = ArrayList()
    private val mongoServerMap: MutableMap<MongoServer, ServerConfiguration> = TreeMap()
    private val rootNode = "<root>"

    override fun getRoot(): Any {
        return rootNode
    }

    override fun getChild(parent: Any, index: Int): Any? {
        val children = getChildren(parent)
        return if (children.isEmpty()) null else children[index]
    }

    override fun getChildCount(parent: Any): Int {
        return getChildren(parent).size
    }

    override fun isLeaf(node: Any): Boolean {
        return node is MongoCollection
    }

    override fun valueForPathChanged(path: TreePath, newValue: Any) {}

    override fun getIndexOfChild(parent: Any, child: Any): Int {
        return getChildren(parent).indexOf(child)
    }

    override fun addTreeModelListener(l: TreeModelListener) {
        listenerList.add(l)
    }

    override fun removeTreeModelListener(l: TreeModelListener) {
        listenerList.remove(l)
    }

    fun getChildren(parent: Any?): List<Any> {
        return when (parent) {
            rootNode -> ArrayList(mongoServerMap.keys)
            is MongoServer -> ArrayList(parent.databases)
            is MongoDatabase -> ArrayList(parent.collections)
            else -> ArrayList()
        }
    }

    fun removeConfiguration(mongoServer: MongoServer) {
        mongoServerMap.remove(mongoServer)
    }

    fun addConfiguration(serverConfiguration: ServerConfiguration): MongoServer {
        val mongoServer = MongoServer(serverConfiguration)
        mongoServerMap[mongoServer] = serverConfiguration
        fireTreeStructureChanged(rootNode)
        return mongoServer
    }

    fun fireTreeStructureChanged(node: Any?) {
        val e = TreeModelEvent(this, getPathToRoot(node))
        listenerList.forEach { listener ->
            listener.treeStructureChanged(e)
        }
    }

    fun getPathToRoot(aNode: Any?): Array<Any?>? {
        return getPathToRoot(aNode, 0)
    }

    fun getParent(node: Any?): Any? {
        return when (node) {
            is MongoServer -> rootNode
            is MongoDatabase -> node.parentServer
            is MongoCollection -> node.parentDatabase
            else -> null
        }
    }

    private fun getPathToRoot(obj: Any?, depthParam: Int): Array<Any?>? {
        var depth = depthParam
        val retNodes: Array<Any?>?
        when (obj) {
            null -> {
                retNodes = when (depth) {
                    0 -> return null
                    else -> arrayOfNulls(depth)
                }
            }

            else -> {
                depth++
                retNodes = when (obj) {
                    rootNode -> arrayOfNulls(depth)
                    else -> getPathToRoot(getParent(obj), depth)
                }
                retNodes!![retNodes.size - depth] = obj
            }
        }
        return retNodes
    }

    fun getTypeOfNode(node: Any?): String {
        return when (node) {
            rootNode -> "<root>"
            is MongoServer -> "Server"
            is MongoDatabase -> "Database"
            is MongoCollection -> "Collection"
            else -> "<undefined>"
        }
    }

    fun getServerConfiguration(node: Any?): ServerConfiguration? {
        return when (node) {
            is MongoServer -> node.configuration
            is MongoDatabase -> getServerConfiguration(node.parentServer)
            is MongoCollection -> getServerConfiguration(node.parentDatabase)
            else -> null
        }
    }
}