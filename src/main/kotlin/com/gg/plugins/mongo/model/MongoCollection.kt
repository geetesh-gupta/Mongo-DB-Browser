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

import java.util.Objects

class MongoCollection(val name: String, val parentDatabase: MongoDatabase) : Comparable<MongoCollection> {
    override fun compareTo(other: MongoCollection): Int {
        return name.compareTo(other.name)
    }

    override fun hashCode(): Int {
        return Objects.hash(name, parentDatabase)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MongoCollection) return false
        return name == other.name && parentDatabase == other.parentDatabase
    }

    override fun toString(): String {
        return name
        //		return "MongoCollection{" + "name='" + name + '\'' + '}';
    }
}