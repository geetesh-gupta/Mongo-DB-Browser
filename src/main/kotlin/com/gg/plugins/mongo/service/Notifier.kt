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

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

class Notifier private constructor(private val project: Project) {
    private fun notify(message: String?, notificationType: NotificationType?) {
        if (NotificationGroupManager.getInstance() != null) {
            NotificationGroupManager.getInstance().getNotificationGroup("MongoDBBrowserNotificationGroup")
                .createNotification("[MongoDBBrowser] $message", notificationType!!).notify(project)
        }
    }

    fun notifyInfo(message: String?) {
        notify(message, NotificationType.INFORMATION)
    }

    fun notifyError(content: String?) {
        notify(content, NotificationType.ERROR)
    }

    companion object {
        fun getInstance(project: Project): Notifier {
            return project.getService(Notifier::class.java)
        }
    }
}