/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.edition;

import com.gg.plugins.mongo.utils.GuiUtils;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class AddKeyDialog extends AbstractAddDialog {

	private JTextField nameTextField;

	private ComboBox typeComboBox;

	private JPanel valuePanel;

	private JPanel mainPanel;

	private AddKeyDialog(MongoEditionPanel mongoEditionPanel) {
		super(mongoEditionPanel);
		mainPanel.setPreferredSize(GuiUtils.enlargeWidth(mainPanel.getPreferredSize(), 1.5d));
		valuePanel.setLayout(new BorderLayout());
		nameTextField.setName("keyName");
		typeComboBox.setName("valueType");
	}

	public static AddKeyDialog createDialog(MongoEditionPanel parentPanel) {
		AddKeyDialog dialog = new AddKeyDialog(parentPanel);
		dialog.init();
		dialog.setTitle("Add A Key");

		return dialog;
	}

	@Nullable
	@Override
	protected ValidationInfo doValidate() {
		String keyName = getKey();
		if (StringUtils.isBlank(keyName)) {
			return new ValidationInfo("Key name is not set");
		}

		if (mongoEditionPanel.containsKey(keyName)) {
			return new ValidationInfo(String.format("Key '%s' is already used", keyName));
		}

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
		return nameTextField;
	}

	@Override
	protected void init() {
		super.init();
		initCombo(typeComboBox, valuePanel);
	}

	public String getKey() {
		return nameTextField.getText();
	}

	@Override
	public Object getValue() {
		return currentEditor.getValue();
	}
}