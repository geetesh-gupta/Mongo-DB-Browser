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
