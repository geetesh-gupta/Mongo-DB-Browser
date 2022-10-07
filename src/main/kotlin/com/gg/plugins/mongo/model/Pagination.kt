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

import java.math.BigDecimal
import java.math.RoundingMode

class Pagination {
    private val mySetFilterListeners: MutableCollection<Runnable> = ArrayList()
    var resultsPerPage: ResultsPerPage = ResultsPerPage.ALL
        set(value) {
            field = value
            pageNumber = 1
            for (listener in mySetFilterListeners) {
                listener.run()
            }
        }
    var pageNumber = 1
        set(value) {
            field = value
            for (listener in mySetFilterListeners) {
                listener.run()
            }
        }
    private var totalDocuments: Int = 0
    val startIndex: Int
        get() = if (ResultsPerPage.ALL == resultsPerPage) {
            0
        } else {
            countPerPage * (pageNumber - 1)
        }
    val countPerPage: Int
        get() = resultsPerPage.countPerPage
    val totalPageNumber: Int
        get() = if (countPerPage == 0) {
            1
        } else BigDecimal(totalDocuments)
            .divide(BigDecimal(countPerPage), RoundingMode.CEILING)
            .toBigInteger()
            .toInt()

    fun next() {
        pageNumber += 1
    }

    fun previous() {
        pageNumber -= 1
    }

    fun setTotalDocuments(totalDocuments: Int) {
        this.totalDocuments = totalDocuments
    }

    fun addSetPageListener(runnable: Runnable) {
        mySetFilterListeners.add(runnable)
    }
}