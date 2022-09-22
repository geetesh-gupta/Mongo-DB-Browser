/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.table;

import com.gg.plugins.mongo.utils.DateUtils;
import com.gg.plugins.mongo.view.nodedescriptor.MongoNodeDescriptor;
import org.jdesktop.swingx.table.DatePickerCellEditor;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Locale;

public class MongoDatePickerCellEditor extends DatePickerCellEditor {

	public MongoDatePickerCellEditor() {
		this.dateFormat = DateUtils.utcDateTime(Locale.getDefault());
		datePicker = DateTimePicker.create();
		datePicker.getEditor().setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));
		datePicker.getEditor().setEditable(false);
	}

	@Override
	protected Date getValueAsDate(Object value) {
		MongoNodeDescriptor descriptor = (MongoNodeDescriptor) value;

		return super.getValueAsDate(descriptor.getValue());
	}

	public void addActionListener(ActionListener actionListener) {
		datePicker.addActionListener(actionListener);
	}
}
