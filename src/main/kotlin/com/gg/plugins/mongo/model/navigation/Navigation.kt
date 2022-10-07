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
package com.gg.plugins.mongo.model.navigation

import com.gg.plugins.mongo.model.MongoCollection
import com.gg.plugins.mongo.model.MongoQueryOptions

class Navigation {
    val wayPoints: MutableList<WayPoint> = ArrayList()
    var currentWayPoint: WayPoint? = null
        private set

    fun addNewWayPoint(collection: MongoCollection, mongoQueryOptions: MongoQueryOptions?) {
        currentWayPoint = WayPoint(collection, mongoQueryOptions)
        wayPoints.add(currentWayPoint!!)
    }

    fun moveBackward() {
        if (currentWayPoint != null) {
            val currentWayPointIndex = wayPoints.indexOf(currentWayPoint)
            if (currentWayPointIndex > 0) {
                currentWayPoint = wayPoints[currentWayPointIndex - 1]
                wayPoints.removeAt(currentWayPointIndex)
            }
        }
    }

    class WayPoint internal constructor(val collection: MongoCollection, var queryOptions: MongoQueryOptions?) {
        val label: String
            get() = collection.parentDatabase.name + "/" + collection.name
    }
}