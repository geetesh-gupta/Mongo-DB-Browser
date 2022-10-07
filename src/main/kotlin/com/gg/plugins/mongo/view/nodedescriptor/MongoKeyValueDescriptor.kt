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
import com.mongodb.DBRef
import org.bson.Document
import org.bson.types.Binary
import org.bson.types.ObjectId
import java.util.Date
import java.util.Locale

open class MongoKeyValueDescriptor private constructor(
    val formattedKey: String?,
    override var value: Any?,
    private val valueTextAttributes: SimpleTextAttributes?
) : MongoNodeDescriptor {
    override fun renderValue(cellRenderer: ColoredTableCellRenderer, isNodeExpanded: Boolean) {
        if (!isNodeExpanded) {
            cellRenderer.append(formattedValue!!, valueTextAttributes!!)
        }
    }

    override fun renderNode(cellRenderer: ColoredTreeCellRenderer) {
        cellRenderer.append(formattedKey!!, StyleAttributesProvider.keyValueAttribute)
    }

    override val key: String?
        get() = formattedKey
    override val formattedValue: String?
        get() = StringUtils.abbreviateInCenter(value.toString(), MongoNodeDescriptor.MAX_LENGTH)

    override fun pretty(): String? {
        return formattedValue
    }

    override fun toString(): String {
        return value.toString()
    }

    private class MongoKeyNullValueDescriptor(key: String?) :
        MongoKeyValueDescriptor(key, null, StyleAttributesProvider.nullAttribute) {
        override val formattedValue: String
            get() = "null"

        override fun toString(): String {
            return formattedValue
        }
    }

    private class MongoKeyStringValueDescriptor(key: String?, value: String) :
        MongoKeyValueDescriptor(key, value, StyleAttributesProvider.stringAttribute) {
        override val formattedValue: String
            get() = value.toString()

        override fun toString(): String {
            return formattedValue
        }
    }

    private class MongoKeyDateValueDescriptor(key: String?, value: Date) :
        MongoKeyValueDescriptor(key, value, StyleAttributesProvider.stringAttribute) {
        override val formattedValue: String
            get() = formattedDate

        override fun toString(): String {
            return formattedDate
        }

        private val formattedDate: String
            get() = DATE_FORMAT.format(
                value
            )

        companion object {
            private val DATE_FORMAT = DateUtils.utcDateTime(Locale.getDefault())
        }
    }

    private class MongoKeyDocumentValueDescriptor(key: String?, value: Document?) :
        MongoKeyValueDescriptor(key, value, StyleAttributesProvider.documentAttribute) {
        override val formattedValue: String
            get() {
                val document = value as Document
                return StringUtils.abbreviateInCenter(
                    document.toJson(MongoUtils.DOCUMENT_CODEC),
                    MongoNodeDescriptor.MAX_LENGTH
                )
            }

        override fun pretty(): String? {
            return (value as Document?)!!.toJson(MongoUtils.WRITER_SETTINGS)
        }

        override fun toString(): String {
            return formattedDocument
        }

        private val formattedDocument: String
            get() = (value as Document?)!!.toJson(MongoUtils.DOCUMENT_CODEC)
    }

    private class MongoKeyBinaryValueDescriptor(key: String?, value: Binary) :
        MongoKeyValueDescriptor(key, value, StyleAttributesProvider.nullAttribute) {
        override val formattedValue: String
            get() = "Cannot display value"
    }

    private class MongoKeyRefValueDescriptor(key: String?, value: Any?) :
        MongoKeyValueDescriptor(key, value, StyleAttributesProvider.documentAttribute) {
        override val formattedValue: String
            get() {
                val dbRef = value as DBRef
                return StringUtils.abbreviateInCenter(dbRef.toString(), MongoNodeDescriptor.MAX_LENGTH)
            }

        override fun toString(): String {
            return value.toString()
        }
    }

    private class MongoKeyListValueDescriptor(key: String?, value: Any?) :
        MongoKeyValueDescriptor(key, value, StyleAttributesProvider.documentAttribute) {
        override val formattedValue: String
            get() = StringUtils.abbreviateInCenter(formattedList, MongoNodeDescriptor.MAX_LENGTH)

        override fun toString(): String {
            return String.format(TO_STRING_TEMPLATE, formattedKey, formattedList)
        }

        private val formattedList: String
            get() = MongoUtils.stringifyList(value as List<*>?)

        companion object {
            private const val TO_STRING_TEMPLATE = "\"%s\" : %s"
        }
    }

    companion object {
        fun createDescriptor(key: String?, value: Any?): MongoKeyValueDescriptor { //TODO refactor this
            return when (value) {
                (value == null) -> {
                    MongoKeyNullValueDescriptor(key)
                }

                is Boolean -> {
                    object : MongoKeyValueDescriptor(key, value, StyleAttributesProvider.booleanAttribute) {
                        override var value: Any? = null
                            get() = super.value
                            set(value) {
                                field = java.lang.Boolean.valueOf(value as String?)
                            }
                    }
                }

                is Int -> {
                    object : MongoKeyValueDescriptor(key, value, StyleAttributesProvider.numberAttribute) {
                        override var value: Any? = null
                            get() = super.value
                            set(value) {
                                field = Integer.valueOf(value as String?)
                            }
                    }
                }

                is Double -> {
                    object : MongoKeyValueDescriptor(key, value, StyleAttributesProvider.numberAttribute) {
                        override var value: Any? = null
                            get() = super.value
                            set(value) {
                                field = java.lang.Double.valueOf(value as String?)
                            }
                    }
                }

                is Long -> {
                    object : MongoKeyValueDescriptor(key, value, StyleAttributesProvider.numberAttribute) {
                        override var value: Any? = null
                            get() = super.value
                            set(value) {
                                field = java.lang.Long.valueOf(value as String?)
                            }
                    }
                }

                is String -> {
                    MongoKeyStringValueDescriptor(key, value)
                }

                is Date -> {
                    MongoKeyDateValueDescriptor(key, value)
                }

                is ObjectId -> {
                    MongoKeyValueDescriptor(key, value, StyleAttributesProvider.objectIdAttribute)
                }

                is Document -> {
                    MongoKeyDocumentValueDescriptor(key, value as Document?)
                }

                is Binary -> {
                    MongoKeyBinaryValueDescriptor(key, value)
                }

                is DBRef -> {
                    MongoKeyRefValueDescriptor(key, value)
                }

                is List<*> -> {
                    MongoKeyListValueDescriptor(key, value)
                }

                else -> {
                    MongoKeyValueDescriptor(key, value, StyleAttributesProvider.stringAttribute)
                }
            }
        }
    }
}