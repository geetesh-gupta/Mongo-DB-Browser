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

package com.gg.plugins.mongo.view;

import com.gg.plugins.mongo.model.NbPerPage;
import com.gg.plugins.mongo.model.Pagination;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.ClickListener;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.*;

/**
 * Fork of the filter select component in VCS Tool window of Intellij
 *
 * @see com.intellij.vcs.log.ui.filter.VcsLogPopupComponent
 */
class PaginationPopupComponent extends JPanel {
	private static final int GAP_BEFORE_ARROW = 3;

	private static final int BORDER_SIZE = 2;

	private final Pagination pagination;

	private JLabel myValueLabel;

	public PaginationPopupComponent(Pagination pagination) {
		this.pagination = pagination;
	}

	private static String getText(NbPerPage nbPerPage) {
		return NbPerPage.ALL.equals(nbPerPage) ?
		       String.format("%s docs", NbPerPage.ALL.label) :
		       String.format("%s docs / page", nbPerPage.label);
	}

	private static Border createFocusedBorder() {
		return BorderFactory.createCompoundBorder(new RoundedLineBorder(UIUtil.getHeaderActiveColor(), 10,
						BORDER_SIZE),
				JBUI.Borders.empty(2));
	}

	private static Border createUnfocusedBorder() {
		return BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(BORDER_SIZE,
				BORDER_SIZE,
				BORDER_SIZE,
				BORDER_SIZE), JBUI.Borders.empty(2));
	}

	public JComponent initUi() {
		myValueLabel = new JLabel() {
			@Override
			public String getText() {
				return getCurrentText();
			}
		};
		setDefaultForeground();
		setFocusable(true);
		setBorder(createUnfocusedBorder());

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(myValueLabel);
		add(Box.createHorizontalStrut(GAP_BEFORE_ARROW));
		add(new JLabel(AllIcons.Ide.Statusbar_arrows));

		installChangeListener(() -> {
			myValueLabel.revalidate();
			myValueLabel.repaint();
		});
		showPopupMenuOnClick();
		showPopupMenuFromKeyboard();
		indicateHovering();
		indicateFocusing();
		return this;
	}

	private void installChangeListener(Runnable listener) {
		pagination.addSetPageListener(listener);
	}

	private String getCurrentText() {
		NbPerPage nbPerPage = pagination.getNbPerPage();
		return getText(nbPerPage);
	}

	/**
	 * Create popup actions available under this filter.
	 */
	private ActionGroup createActionGroup() {
		DefaultActionGroup actionGroup = new DefaultActionGroup();

		for (NbPerPage nbPerPage : NbPerPage.values()) {
			actionGroup.add(new NbPerPageAction(nbPerPage));
		}

		return actionGroup;
	}

	private void indicateFocusing() {
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(@NotNull FocusEvent e) {
				setBorder(createFocusedBorder());
			}

			@Override
			public void focusLost(@NotNull FocusEvent e) {
				setBorder(createUnfocusedBorder());
			}
		});
	}

	private void showPopupMenuFromKeyboard() {
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(@NotNull KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {
					showPopupMenu();
				}
			}
		});
	}

	private void showPopupMenuOnClick() {
		new ClickListener() {
			@Override
			public boolean onClick(@NotNull MouseEvent event, int clickCount) {
				showPopupMenu();
				return true;
			}
		}.installOn(this);
	}

	private void indicateHovering() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(@NotNull MouseEvent e) {
				setOnHoverForeground();
			}

			@Override
			public void mouseExited(@NotNull MouseEvent e) {
				setDefaultForeground();
			}
		});
	}

	private void setDefaultForeground() {
		myValueLabel.setForeground(UIUtil.isUnderDarcula() ?
		                           UIUtil.getLabelForeground() :
		                           UIUtil.getInactiveTextColor().darker().darker());
	}

	private void setOnHoverForeground() {
		myValueLabel.setForeground(UIUtil.isUnderDarcula() ?
		                           UIUtil.getLabelForeground() :
		                           UIUtil.getTextFieldForeground());
	}

	private void showPopupMenu() {
		ListPopup popup = createPopupMenu();
		popup.showInCenterOf(this);
	}

	@NotNull
	private ListPopup createPopupMenu() {
		return JBPopupFactory.getInstance()
		                     .createActionGroupPopup(null,
				                     createActionGroup(),
				                     DataManager.getInstance().getDataContext(this),
				                     JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
				                     false);
	}

	private class NbPerPageAction extends DumbAwareAction {

		private final NbPerPage nbPerPage;

		NbPerPageAction(NbPerPage nbPerPage) {
			super(getText(nbPerPage));
			this.nbPerPage = nbPerPage;
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			pagination.setNbPerPage(nbPerPage);
		}
	}

}
