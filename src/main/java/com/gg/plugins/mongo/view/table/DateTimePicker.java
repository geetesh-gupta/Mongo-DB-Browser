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

/*
  This is licensed under LGPL.  License can be found here:  http://www.gnu.org/licenses/lgpl-3.0.txt
  This is provided as is.  If you have questions please direct them to charlie.hubbard at gmail dot you know what.
 */
package com.gg.plugins.mongo.view.table;

import com.gg.plugins.mongo.utils.DateUtils;
import com.gg.plugins.mongo.view.style.StyleAttributesProvider;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.calendar.SingleDaySelectionModel;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public class DateTimePicker extends JXDatePicker {

	private static final Color backgroundColor = JBColor.background();

	private static final Color foregroundColor = JBColor.foreground();

	private static final Color selectionBackgroundColor = UIUtil.getTableSelectionBackground(true);

	private static final Color selectionForegroundColor = UIUtil.getTableSelectionForeground(true);

	private static final Color monthForegroundColor = StyleAttributesProvider.NUMBER_COLOR;

	private static final Color dayOfTheWeekForegroundColor = StyleAttributesProvider.KEY_COLOR;

	private static final Color todayBackgroundColor = JBColor.WHITE;

	private JSpinner timeSpinner;

	private JPanel timePanel;

	private DateFormat timeFormat;

	private DateTimePicker() {
		super(null, Locale.getDefault());
		getMonthView().setSelectionModel(new SingleDaySelectionModel());
	}

	public static DateTimePicker create() {
		DateTimePicker dateTimePicker = new DateTimePicker();
		dateTimePicker.setFormats(DateUtils.utcDateTime(Locale.getDefault()));
		dateTimePicker.setTimeFormat(DateUtils.utcTime(Locale.getDefault()));
		dateTimePicker.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateTimePicker.applyUIStyle();

		return dateTimePicker;
	}

	private void setTimeFormat(DateFormat timeFormat) {
		this.timeFormat = timeFormat;
		updateTextFieldFormat();
	}

	private void applyUIStyle() {
		JXMonthView monthView = getMonthView();
		monthView.setMonthStringBackground(backgroundColor);
		monthView.setMonthStringForeground(monthForegroundColor);
		monthView.setSelectionBackground(selectionBackgroundColor);
		monthView.setSelectionForeground(selectionForegroundColor);
		monthView.setDaysOfTheWeekForeground(dayOfTheWeekForegroundColor);
		monthView.setBackground(backgroundColor);
		monthView.setForeground(foregroundColor);
		monthView.setTodayBackground(todayBackgroundColor);

		getLinkPanel().setBackground(backgroundColor);
		getLinkPanel().setForeground(foregroundColor);
	}

	private void updateTextFieldFormat() {
		if (timeSpinner == null)
			return;
		JFormattedTextField tf = ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField();
		DefaultFormatterFactory factory = (DefaultFormatterFactory) tf.getFormatterFactory();
		DateFormatter formatter = (DateFormatter) factory.getDefaultFormatter();
		// Change the date format to only show the hours
		formatter.setFormat(timeFormat);
	}

	@Override
	public JPanel getLinkPanel() {
		super.getLinkPanel();
		if (timePanel == null) {
			timePanel = createTimePanel();
		}
		setTimeSpinners();
		return timePanel;
	}

	public void commitEdit() throws ParseException {
		commitTime();
		super.commitEdit();
	}

	public void cancelEdit() {
		super.cancelEdit();
		setTimeSpinners();
	}

	private void commitTime() {
		Date date = getDate();
		if (date != null) {
			Date time = (Date) timeSpinner.getValue();
			GregorianCalendar timeCalendar = new GregorianCalendar();
			timeCalendar.setTime(time);

			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
			calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
			calendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
			calendar.set(Calendar.MILLISECOND, 0);

			Date newDate = calendar.getTime();
			setDate(newDate);
		}
	}

	private JPanel createTimePanel() {
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new FlowLayout());

		SpinnerDateModel dateModel = new SpinnerDateModel();
		timeSpinner = new JSpinner(dateModel);
		if (timeFormat == null)
			timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
		updateTextFieldFormat();
		newPanel.add(timeSpinner);
		return newPanel;
	}

	private void setTimeSpinners() {
		Date date = getDate();
		if (date != null) {
			timeSpinner.setValue(date);
		}
	}
}