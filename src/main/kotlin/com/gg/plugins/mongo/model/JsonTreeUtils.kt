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
package com.gg.plugins.mongo.model

import com.gg.plugins.mongo.view.nodedescriptor.MongoKeyValueDescriptor
import com.gg.plugins.mongo.view.nodedescriptor.MongoResultDescriptor
import com.gg.plugins.mongo.view.nodedescriptor.MongoValueDescriptor
import com.mongodb.DBRef
import org.bson.Document
import java.util.Enumeration
import javax.swing.tree.TreeNode

object JsonTreeUtils {
    fun buildJsonTree(collectionName: String?, documents: List<Document?>?, startIndex: Int): TreeNode {
        val rootNode = JsonTreeNode(MongoResultDescriptor(collectionName))
        var i = startIndex
        for (document in documents!!) {
            val currentNode = JsonTreeNode(MongoValueDescriptor.createDescriptor(i++, document))
            processDocument(currentNode, document)
            rootNode.add(currentNode)
        }
        return rootNode
    }

    fun buildJsonTree(document: Document?): TreeNode {
        val rootNode = JsonTreeNode(MongoResultDescriptor())
        if (document != null) {
            processDocument(rootNode, document)
        }
        return rootNode
    }

    fun processDocument(parentNode: JsonTreeNode, document: Document?) {
        for (key in document!!.keys) {
            val value = document[key]
            val currentNode = JsonTreeNode(MongoKeyValueDescriptor.createDescriptor(key, value))
            processValue(value, currentNode)
            parentNode.add(currentNode)
        }
    }

    fun processObjectList(parentNode: JsonTreeNode, objectList: List<*>) {
        for (i in objectList.indices) {
            val value = objectList[i]!!
            val currentNode = JsonTreeNode(MongoValueDescriptor.createDescriptor(i, value))
            processValue(value, currentNode)
            parentNode.add(currentNode)
        }
    }

    private fun processValue(value: Any?, currentNode: JsonTreeNode) {
        when (value) {
            is Document -> processDocument(currentNode, value as Document?)
            is DBRef -> processDBRef(currentNode, value)
            is List<*> -> processObjectList(currentNode, value)
        }
    }

    private fun processDBRef(parentNode: JsonTreeNode, dbRef: DBRef) {
        parentNode.add(JsonTreeNode(MongoKeyValueDescriptor.createDescriptor("\$ref", dbRef.collectionName)))
        parentNode.add(JsonTreeNode(MongoKeyValueDescriptor.createDescriptor("\$id", dbRef.id)))
        parentNode.add(JsonTreeNode(MongoKeyValueDescriptor.createDescriptor("\$db", dbRef.databaseName)))
    }

    fun buildDocumentObject(rootNode: JsonTreeNode): Document {
        val document = Document()
        val children: Enumeration<*> = rootNode.children()
        while (children.hasMoreElements()) {
            val node = children.nextElement() as JsonTreeNode
            val descriptor = node.descriptor as MongoKeyValueDescriptor
            when (val value = descriptor.value) {
                is Document -> document[descriptor.key] = buildDocumentObject(node)
                is DBRef -> document[descriptor.key] = buildDBRefObject(node)
                is List<*> -> document[descriptor.key] = buildObjectList(node)
                else -> document[descriptor.key] = value
            }
        }
        return document
    }

    private fun buildDBRefObject(parentNode: JsonTreeNode): DBRef {
        var id: Any? = null
        var collectionName: String? = null
        var databaseName: String? = null
        val children: Enumeration<*> = parentNode.children()
        while (children.hasMoreElements()) {
            val node = children.nextElement() as JsonTreeNode
            val descriptor = node.descriptor
            when (val formattedKey = descriptor.key) {
                "\$id" -> id = descriptor.value
                "\$ref" -> collectionName = descriptor.value as String
                "\$db" -> databaseName = descriptor.value as String
                else -> throw IllegalArgumentException("Unexpected key: $formattedKey")
            }
        }
        require(!(collectionName == null || id == null)) { "When using DBRef, \$ref and \$id should be set." }
        return DBRef(databaseName, collectionName, id)
    }

    private fun buildObjectList(parentNode: JsonTreeNode): List<*> {
        val basicDBList: MutableList<Any?> = ArrayList()
        val children: Enumeration<*> = parentNode.children()
        while (children.hasMoreElements()) {
            val node = children.nextElement() as JsonTreeNode
            val descriptor = node.descriptor as MongoValueDescriptor
            when (val value = descriptor.value) {
                is Document -> basicDBList.add(buildDocumentObject(node))
                is List<*> -> basicDBList.add(buildObjectList(node))
                else -> basicDBList.add(value)
            }
        }
        return basicDBList
    }
}