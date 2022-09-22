/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.action.pagination;

import com.gg.plugins.mongo.model.NbPerPage;
import com.gg.plugins.mongo.model.Pagination;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class PaginationAction extends AnAction implements DumbAware {
	final Pagination pagination;

	PaginationAction(Pagination pagination, String toolTip, String description, Icon icon) {
		super(toolTip, description, icon);
		this.pagination = pagination;
	}

	public static class Next extends PaginationAction {
		public Next(Pagination pagination) {
			super(pagination, "See next results", "Next page", AllIcons.Actions.Forward);
		}

		@Override
		public void update(AnActionEvent event) {
			event.getPresentation()
			     .setEnabled(!NbPerPage.ALL.equals(pagination.getNbPerPage()) &&
			                 pagination.getPageNumber() < pagination.getTotalPageNumber());
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			pagination.next();
		}
	}

	public static class Previous extends PaginationAction {
		public Previous(Pagination pagination) {
			super(pagination, "See previous results", "Previous page", AllIcons.Actions.Back);
		}

		@Override
		public void update(AnActionEvent event) {
			event.getPresentation()
			     .setEnabled(!NbPerPage.ALL.equals(pagination.getNbPerPage()) && pagination.getStartIndex() > 0);
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			pagination.previous();
		}
	}
}
