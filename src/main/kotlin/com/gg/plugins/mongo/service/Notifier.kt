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
package com.gg.plugins.mongo.service

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

class Notifier private constructor(private val project: Project) {
    private val notificationGroup: NotificationGroup =
        NotificationGroupManager.getInstance().getNotificationGroup(MONGO_NOTIFICATION_GROUP)

    fun notify(message: String, notificationType: NotificationType?) {
        notificationGroup.createNotification("[MongoPlugin] $message", notificationType!!).notify(project)
    }

    fun notifyInfo(message: String?) {
        notificationGroup.createNotification(message!!, NotificationType.INFORMATION).notify(
            project
        )
    }

    fun notifyError(content: String?) {
        notificationGroup.createNotification(content!!, NotificationType.ERROR).notify(
            project
        )
    }

    companion object {
        private const val MONGO_NOTIFICATION_GROUP = "Mongo"
        fun getInstance(project: Project): Notifier {
            return project.getService(Notifier::class.java)
        }
    }
}