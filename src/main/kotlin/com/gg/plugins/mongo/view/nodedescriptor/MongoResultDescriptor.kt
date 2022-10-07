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

import com.intellij.ui.ColoredTableCellRenderer
import com.intellij.ui.ColoredTreeCellRenderer

class MongoResultDescriptor @JvmOverloads constructor(collectionName: String? = "") : MongoNodeDescriptor {
    private val formattedText: String

    init {
        formattedText = String.format("results of '%s'", collectionName)
    }

    override fun renderValue(cellRenderer: ColoredTableCellRenderer, isNodeExpanded: Boolean) {}
    override fun renderNode(cellRenderer: ColoredTreeCellRenderer) {}
    override val key: String
        get() = formattedText
    override val formattedValue: String
        get() = ""
    override var value: Any?
        get() = null
        set(_) {}

    override fun pretty(): String {
        return formattedText
    }
}