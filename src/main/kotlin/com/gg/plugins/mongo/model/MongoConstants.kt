/*
 * Copyright (c) 2022 Geetesh Gupta.
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

import com.gg.plugins.mongo.utils.GuiUtils

object MongoConstants {
    val MONGO_SERVER = GuiUtils.loadIcon("mongo_logo.png")
    val MONGO_DATABASE = GuiUtils.loadIcon("database.png")
    val MONGO_COLLECTION = GuiUtils.loadIcon("folder.png")
    val MONGO_SERVER_ERROR = GuiUtils.loadIcon("mongo_warning.png")
}