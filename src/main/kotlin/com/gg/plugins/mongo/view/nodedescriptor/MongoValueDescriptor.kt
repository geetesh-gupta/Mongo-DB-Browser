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
package com.gg.plugins.mongo.view.nodedescriptor

import com.gg.plugins.mongo.utils.DateUtils
import com.gg.plugins.mongo.utils.MongoUtils
import com.gg.plugins.mongo.utils.StringUtils
import com.gg.plugins.mongo.view.style.StyleAttributesProvider
import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.SimpleTextAttributes
import org.bson.Document
import org.bson.types.Binary
import java.util.Date
import java.util.Locale

open class MongoValueDescriptor private constructor(
    private val index: Int,
    override var value: Any?,
    private val valueTextAttributes: SimpleTextAttributes?
) : MongoNodeDescriptor {
    override fun renderValue(cellRenderer: ColoredTableCellRenderer, isNodeExpanded: Boolean) {
        if (!isNodeExpanded) {
            cellRenderer.append(formattedValue!!, valueTextAttributes!!)
        }
    }

    override fun renderNode(cellRenderer: ColoredTreeCellRenderer) {
        cellRenderer.append(key!!, StyleAttributesProvider.indexAttribute)
    }

    override val key: String?
        get() = String.format("[%s]", index)
    override val formattedValue: String?
        get() = String.format(
            "%s",
            StringUtils.abbreviateInCenter(value.toString(), MongoNodeDescriptor.MAX_LENGTH)
        )

    override fun pretty(): String? {
        return formattedValue
    }

    override fun toString(): String {
        return value.toString()
    }

    private class MongoStringValueDescriptor(index: Int, value: String) :
        MongoValueDescriptor(index, value, StyleAttributesProvider.stringAttribute) {
        override val formattedValue: String
            get() = StringUtils.abbreviateInCenter(value.toString(), MongoNodeDescriptor.MAX_LENGTH)
    }

    private class MongoNullValueDescriptor(index: Int) :
        MongoValueDescriptor(index, null, StyleAttributesProvider.nullAttribute) {
        override val formattedValue: String
            get() = "null"

        override fun toString(): String {
            return "null"
        }
    }

    private class MongoDateValueDescriptor(index: Int, value: Date) :
        MongoValueDescriptor(index, value, StyleAttributesProvider.stringAttribute) {
        override val formattedValue: String
            get() = formattedDate

        override fun toString(): String {
            return String.format("\"%s\"", formattedDate)
        }

        private val formattedDate: String
            get() = DATE_FORMAT.format(
                value
            )

        companion object {
            private val DATE_FORMAT = DateUtils.utcDateTime(Locale.getDefault())
        }
    }

    private class MongoDocumentValueDescriptor(index: Int, value: Document) :
        MongoValueDescriptor(index, value, StyleAttributesProvider.documentAttribute) {
        override val formattedValue: String
            get() {
                return String.format(
                    "%s",
                    StringUtils.abbreviateInCenter(
                        (value as Document).toJson(MongoUtils.DOCUMENT_CODEC),
                        MongoNodeDescriptor.MAX_LENGTH
                    )
                )
            }

        override fun pretty(): String? {
            return (value as Document?)!!.toJson(MongoUtils.WRITER_SETTINGS)
        }

        override fun toString(): String {
            return (value as Document?)!!.toJson(MongoUtils.DOCUMENT_CODEC)
        }
    }

    private class MongoListValueDescriptor(index: Int, value: Any?) :
        MongoValueDescriptor(index, value, StyleAttributesProvider.documentAttribute) {
        override val formattedValue: String
            get() = formattedList

        override fun toString(): String {
            return formattedList
        }

        private val formattedList: String
            get() = MongoUtils.stringifyList(value as List<*>?)
    }

    private class MongoBinaryDescriptor(index: Int, value: Binary) :
        MongoValueDescriptor(index, value, StyleAttributesProvider.nullAttribute) {
        override val formattedValue: String
            get() = "Cannot display value"
    }

    companion object {
        fun createDescriptor(index: Int, value: Any?): MongoValueDescriptor { //TODO refactor this
            if (value == null) {
                return MongoNullValueDescriptor(index)
            }
            return when (value) {
                is String -> {
                    MongoStringValueDescriptor(index, value)
                }

                is Boolean -> {
                    object : MongoValueDescriptor(index, value, StyleAttributesProvider.booleanAttribute) {
                        override var value: Any? = null
                            get() = super.value
                            set(value) {
                                field = java.lang.Boolean.parseBoolean(value as String?)
                            }
                    }
                }

                is Number -> {
                    object : MongoValueDescriptor(index, value, StyleAttributesProvider.numberAttribute) {
                        override var value: Any? = null
                            get() = super.value
                            set(value) {
                                field = value.toString().toInt()
                            }
                    }
                }

                is Date -> {
                    MongoDateValueDescriptor(index, value)
                }

                is Document -> {
                    MongoDocumentValueDescriptor(index, value)
                }

                is List<*> -> {
                    MongoListValueDescriptor(index, value)
                }

                is Binary -> {
                    MongoBinaryDescriptor(index, value)
                }

                else -> {
                    MongoValueDescriptor(index, value, StyleAttributesProvider.stringAttribute)
                }
            }
        }
    }
}