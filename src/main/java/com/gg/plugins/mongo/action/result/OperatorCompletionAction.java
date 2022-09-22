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

package com.gg.plugins.mongo.action.result;

import com.gg.plugins.mongo.model.MongoAggregateOperator;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.components.JBList;
import com.mongodb.QueryOperators;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class OperatorCompletionAction extends AnAction implements Disposable, DumbAware {

	private static final String MONGO_OPERATOR_COMPLETION = "MONGO_OPERATOR_COMPLETION";

	private static final JBList<String> QUERY_OPERATOR_LIST;

	static {
		List<String> operator = new LinkedList<>();
		for (MongoAggregateOperator aggregateOperator : MongoAggregateOperator.values()) {
			operator.add(aggregateOperator.getLabel());
		}

		for (Field field : QueryOperators.class.getFields()) {
			try {
				operator.add((String) QueryOperators.class.getDeclaredField(field.getName()).get(String.class));
			} catch (IllegalAccessException | NoSuchFieldException ex) {
				throw new IllegalStateException(ex);
			}
		}

		QUERY_OPERATOR_LIST = new JBList<>(operator);
	}

	private final Project project;

	private final Editor editor;

	public OperatorCompletionAction(Project project, Editor editor) {
		this.project = project;
		this.editor = editor;
		registerCustomShortcutSet(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK, editor.getContentComponent());
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
		final Document document = editor.getDocument();
		CaretModel caretModel = editor.getCaretModel();
		final int offset = caretModel.getOffset();
		new PopupChooserBuilder(QUERY_OPERATOR_LIST).setMovable(false)
		                                            .setCancelKeyEnabled(true)
		                                            .setItemChoosenCallback(() -> {
			                                            final String selectedQueryOperator =
					                                            QUERY_OPERATOR_LIST.getSelectedValue();
			                                            if (selectedQueryOperator == null)
				                                            return;

			                                            WriteCommandAction.writeCommandAction(project)
			                                                              .withName(MONGO_OPERATOR_COMPLETION)
			                                                              .run(() -> document.insertString(offset,
					                                                              selectedQueryOperator));
		                                            })
		                                            .createPopup()
		                                            .showInBestPositionFor(editor);
	}

	@Override
	public void dispose() {
		unregisterCustomShortcutSet(editor.getContentComponent());
	}
}
