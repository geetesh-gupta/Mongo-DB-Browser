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

import com.gg.plugins.mongo.model.JsonDataType;
import com.gg.plugins.mongo.utils.StringUtils;
import com.gg.plugins.mongo.view.table.DateTimePicker;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.components.JBCheckBox;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
abstract class AbstractAddDialog extends DialogWrapper {
	private static final Map<JsonDataType, TextFieldWrapper> UI_COMPONENT_BY_JSON_DATATYPE = new HashMap<>();

	static {

		UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.STRING, new StringFieldWrapper());
		UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.BOOLEAN, new BooleanFieldWrapper());
		UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.NUMBER, new NumberFieldWrapper());
		UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.NULL, new NullFieldWrapper());
		UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.DATE, new DateTimeFieldWrapper());
		UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.OBJECT, new JsonFieldWrapper());
		UI_COMPONENT_BY_JSON_DATATYPE.put(JsonDataType.ARRAY, new ArrayFieldWrapper());
	}

	final MongoEditionPanel mongoEditionPanel;

	TextFieldWrapper currentEditor = null;

	AbstractAddDialog(MongoEditionPanel mongoEditionPanel) {
		super(mongoEditionPanel, true);
		this.mongoEditionPanel = mongoEditionPanel;
	}

	void initCombo(final ComboBox combobox, final JPanel parentPanel) {
		combobox.setModel(new DefaultComboBoxModel<>(JsonDataType.values()));
		combobox.setRenderer(new ColoredListCellRenderer() {

			@Override
			protected void customizeCellRenderer(@NotNull JList jList, Object o, int i, boolean b, boolean b2) {
				append(((JsonDataType) o).type);
			}
		});

		combobox.setSelectedItem(null);
		combobox.addItemListener(itemEvent -> {
			JsonDataType selectedType = (JsonDataType) combobox.getSelectedItem();
			currentEditor = UI_COMPONENT_BY_JSON_DATATYPE.get(selectedType);
			currentEditor.reset();

			parentPanel.invalidate();
			parentPanel.removeAll();
			parentPanel.add(currentEditor.getComponent(), BorderLayout.CENTER);
			parentPanel.validate();
		});

		combobox.setSelectedItem(JsonDataType.STRING);
	}

	public abstract Object getValue();

	static abstract class TextFieldWrapper<T extends JComponent, V> {

		final T component;

		private TextFieldWrapper(T component) {
			this.component = component;
		}

		protected abstract V getValue();

		protected abstract void reset();

		T getComponent() {
			return component;
		}

		void validate() {
			if (!isValueSet()) {
				throw new IllegalArgumentException("Value is not set");
			}
		}

		boolean isValueSet() {
			return true;
		}
	}

	private static class StringFieldWrapper extends TextFieldWrapper<JTextField, String> {

		private StringFieldWrapper() {
			super(new JTextField());
		}

		@Override
		public String getValue() {
			return component.getText();
		}

		@Override
		public void reset() {
			component.setText("");
		}

		@Override
		public boolean isValueSet() {
			return StringUtils.isNotBlank(component.getText());
		}
	}

	private static class JsonFieldWrapper extends TextFieldWrapper<JTextField, Object> {

		private JsonFieldWrapper() {
			super(new JTextField());
		}

		@Override
		public Object getValue() {
			return Document.parse(component.getText());
		}

		@Override
		public void reset() {
			component.setText("");
		}

		@Override
		public boolean isValueSet() {
			return StringUtils.isNotBlank(component.getText());
		}
	}

	private static class ArrayFieldWrapper extends JsonFieldWrapper {
		@Override
		public Object getValue() {
			//ugly hack to use DocumentParser instead of BsonArray
			String arrayInDoc = String.format("{\"array\": %s}", component.getText());
			return Document.parse(arrayInDoc).get("array");
		}
	}

	private static class NumberFieldWrapper extends TextFieldWrapper<JTextField, Number> {

		private NumberFieldWrapper() {
			super(new JTextField());
		}

		@Override
		public Number getValue() {
			return StringUtils.parseNumber(component.getText());
		}

		@Override
		public void reset() {
			component.setText("");
		}

		@Override
		public boolean isValueSet() {
			return StringUtils.isNotBlank(component.getText());
		}

		@Override
		public void validate() {
			super.validate();
			getValue();
		}
	}

	private static class BooleanFieldWrapper extends TextFieldWrapper<JBCheckBox, Boolean> {

		private BooleanFieldWrapper() {
			super(new JBCheckBox());
		}

		@Override
		public Boolean getValue() {
			return component.isSelected();
		}

		@Override
		public void reset() {
			component.setSelected(false);
		}
	}

	private static class NullFieldWrapper extends TextFieldWrapper<JLabel, Object> {

		private NullFieldWrapper() {
			super(new JLabel("null"));
		}

		@Override
		public Object getValue() {
			return null;
		}

		@Override
		public void reset() {

		}
	}

	private static class DateTimeFieldWrapper extends TextFieldWrapper<DateTimePicker, Date> {

		private DateTimeFieldWrapper() {
			super(DateTimePicker.create());
			component.getEditor().setEditable(false);
		}

		@Override
		public Date getValue() {
			return component.getDate();
		}

		@Override
		public void reset() {
			component.setDate(GregorianCalendar.getInstance().getTime());
		}

		@Override
		public boolean isValueSet() {
			return component.getDate() != null;
		}
	}
}
