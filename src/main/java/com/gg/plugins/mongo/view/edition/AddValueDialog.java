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

package com.gg.plugins.mongo.view.edition;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AddValueDialog extends AbstractAddDialog {

	private ComboBox typeComboBox;

	private JPanel valuePanel;

	private JPanel mainPanel;

	private AddValueDialog(MongoEditionPanel mongoEditionPanel) {
		super(mongoEditionPanel);
		valuePanel.setLayout(new BorderLayout());
		typeComboBox.setName("valueType");
		typeComboBox.requestFocus();
	}

	public static AddValueDialog createDialog(MongoEditionPanel parentPanel) {
		AddValueDialog dialog = new AddValueDialog(parentPanel);
		dialog.init();
		dialog.setTitle("Add A Value");
		return dialog;
	}

	@Nullable
	@Override
	protected ValidationInfo doValidate() {
		try {
			currentEditor.validate();
		} catch (Exception ex) {
			return new ValidationInfo(ex.getMessage());
		}

		return null;
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return mainPanel;
	}

	@Nullable
	@Override
	public JComponent getPreferredFocusedComponent() {
		return typeComboBox;
	}

	@Override
	protected void init() {
		super.init();

		initCombo(typeComboBox, valuePanel);
	}

	@Override
	public Object getValue() {
		return currentEditor.getValue();
	}
}