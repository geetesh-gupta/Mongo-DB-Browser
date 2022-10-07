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
package com.gg.plugins.mongo.utils

import org.apache.commons.lang.StringUtils

object StringUtils : StringUtils() {
    private const val ELLIPSIS = "..."
    fun abbreviateInCenter(stringToAbbreviate: String?, length: Int): String {
        if (stringToAbbreviate!!.length <= length) {
            return stringToAbbreviate
        }
        val halfLength = length / 2
        val firstPartLastIndex = halfLength - ELLIPSIS.length
        val stringLength = stringToAbbreviate.length
        return String.format(
            "%s%s%s",
            stringToAbbreviate.substring(0, firstPartLastIndex),
            ELLIPSIS,
            stringToAbbreviate.substring(stringLength - halfLength, stringLength)
        )
    }

    fun parseNumber(number: String): Number {
        try {
            return number.toInt()
        } catch (ex: NumberFormatException) {
            //UGLY :(
        }
        try {
            return number.toLong()
        } catch (ex: NumberFormatException) {
            //UGLY :(
        }
        return number.toDouble()
    }
}