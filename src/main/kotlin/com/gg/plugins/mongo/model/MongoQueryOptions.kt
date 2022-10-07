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

import com.mongodb.BasicDBObject
import org.apache.commons.lang.StringUtils
import org.bson.BsonArray
import org.bson.Document
import java.util.LinkedList

class MongoQueryOptions {
    var operations = LinkedList<BasicDBObject>()
        private set
    var filter = EMPTY_DOCUMENT
        private set
    var projection = EMPTY_DOCUMENT
        private set
    var sort = EMPTY_DOCUMENT
        private set
    var resultLimit = DEFAULT_RESULT_LIMIT

    fun isAggregate(): Boolean {
        return !operations.isEmpty()
    }

    fun setOperations(aggregateQuery: String?) {
        operations.clear()
        val parse = BsonArray.parse(aggregateQuery)
        for (operation1 in parse) {
            val operation = BasicDBObject.parse(operation1.toString())
            operations.add(operation)
        }
    }

    fun setFilter(query: String?) {
        if (!StringUtils.isBlank(query)) {
            filter = Document.parse(query)
        }
    }

    fun setFilter(filter: Document): MongoQueryOptions {
        this.filter = filter
        return this
    }

    fun setProjection(query: String?) {
        if (!StringUtils.isBlank(query)) {
            projection = Document.parse(query)
        }
    }

    fun setSort(query: String?) {
        if (!StringUtils.isBlank(query)) {
            sort = Document.parse(query)
        }
    }

    companion object {
        const val NO_LIMIT = 0
        const val DEFAULT_RESULT_LIMIT = 10
        val EMPTY_DOCUMENT = Document()
    }
}