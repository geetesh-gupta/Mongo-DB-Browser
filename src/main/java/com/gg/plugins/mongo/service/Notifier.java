/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.service;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class Notifier {

	private static final String MONGO_NOTIFICATION_GROUP = "Mongo";

	private final Project project;

	private final NotificationGroup notificationGroup;

	private Notifier(Project project) {
		this.project = project;
		this.notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(MONGO_NOTIFICATION_GROUP);
	}

	public static Notifier getInstance(Project project) {
		return project.getService(Notifier.class);
	}

	public void notify(String message, NotificationType notificationType) {
		this.notificationGroup.createNotification("[MongoPlugin] " + message, notificationType).notify(project);
	}

	public void notifyInfo(String message) {
		this.notificationGroup.createNotification(message, NotificationType.INFORMATION).notify(project);
	}

	public void notifyError(String content) {
		this.notificationGroup.createNotification(content, NotificationType.ERROR).notify(project);
	}

}
