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

import com.gg.plugins.mongo.view.renderer.MongoTableCellRenderer
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import org.bson.Document
import javax.swing.table.TableCellRenderer

object JsonTableUtils {
    fun buildJsonTable(mongoCollectionResult: MongoCollectionResult): ListTableModel<Document>? {
        val resultObjects = mongoCollectionResult.documents
        if (resultObjects.isEmpty()) {
            return null
        }
        val columnInfos = extractColumnNames(
            resultObjects[0]
        )
        return ListTableModel(columnInfos, resultObjects)
    }

    private fun extractColumnNames(document: Document): Array<ColumnInfo<Document, *>> {
        val keys: Set<String> = document.keys
        return keys.stream().map { TableColumnInfo(it) }.toArray { arrayOfNulls(it) }
    }

    private class TableColumnInfo(private val key: String) : ColumnInfo<Any?, Any?>(key) {
        override fun valueOf(o: Any?): Any? {
            val document = o as Document?
            return document!![key]
        }

        override fun getRenderer(o: Any?): TableCellRenderer {
            return MONGO_TABLE_CELL_RENDERER
        }

        companion object {
            private val MONGO_TABLE_CELL_RENDERER: TableCellRenderer = MongoTableCellRenderer()
        }
    }
}

