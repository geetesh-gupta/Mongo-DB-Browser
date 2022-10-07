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
package com.gg.plugins.mongo.view.table

import com.gg.plugins.mongo.utils.DateUtils
import com.gg.plugins.mongo.view.style.StyleAttributesProvider
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import org.jdesktop.swingx.JXDatePicker
import org.jdesktop.swingx.calendar.SingleDaySelectionModel
import java.awt.*
import java.text.DateFormat
import java.text.ParseException
import java.util.*
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerDateModel
import javax.swing.text.DateFormatter
import javax.swing.text.DefaultFormatterFactory

class DateTimePicker private constructor() : JXDatePicker(null, Locale.getDefault()) {
    private var timeSpinner: JSpinner? = null
    private var timePanel: JPanel? = null
    private var timeFormat: DateFormat? = null

    init {
        monthView.selectionModel = SingleDaySelectionModel()
    }

    private fun setTimeFormat(timeFormat: DateFormat?) {
        this.timeFormat = timeFormat
        updateTextFieldFormat()
    }

    private fun applyUIStyle() {
        val monthView = monthView
        monthView.monthStringBackground = backgroundColor
        monthView.monthStringForeground = monthForegroundColor
        monthView.selectionBackground = selectionBackgroundColor
        monthView.selectionForeground = selectionForegroundColor
        monthView.daysOfTheWeekForeground = dayOfTheWeekForegroundColor
        monthView.background = backgroundColor
        monthView.foreground = foregroundColor
        monthView.todayBackground = todayBackgroundColor
        linkPanel.background = backgroundColor
        linkPanel.foreground = foregroundColor
    }

    private fun updateTextFieldFormat() {
        if (timeSpinner == null) return
        val tf = (timeSpinner!!.editor as JSpinner.DefaultEditor).textField
        val factory = tf.formatterFactory as DefaultFormatterFactory
        val formatter = factory.defaultFormatter as DateFormatter
        // Change the date format to only show the hours
        formatter.setFormat(timeFormat)
    }

    override fun getLinkPanel(): JPanel {
        super.getLinkPanel()
        if (timePanel == null) {
            timePanel = createTimePanel()
        }
        setTimeSpinners()
        return timePanel!!
    }

    @Throws(ParseException::class)
    override fun commitEdit() {
        commitTime()
        super.commitEdit()
    }

    override fun cancelEdit() {
        super.cancelEdit()
        setTimeSpinners()
    }

    private fun commitTime() {
        val date = date
        if (date != null) {
            val time = timeSpinner!!.value as Date
            val timeCalendar = GregorianCalendar()
            timeCalendar.time = time
            val calendar = GregorianCalendar()
            calendar.time = date
            calendar[Calendar.HOUR_OF_DAY] = timeCalendar[Calendar.HOUR_OF_DAY]
            calendar[Calendar.MINUTE] = timeCalendar[Calendar.MINUTE]
            calendar[Calendar.SECOND] = timeCalendar[Calendar.SECOND]
            calendar[Calendar.MILLISECOND] = 0
            val newDate = calendar.time
            setDate(newDate)
        }
    }

    private fun createTimePanel(): JPanel {
        val newPanel = JPanel()
        newPanel.layout = FlowLayout()
        val dateModel = SpinnerDateModel()
        timeSpinner = JSpinner(dateModel)
        if (timeFormat == null) timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
        updateTextFieldFormat()
        newPanel.add(timeSpinner)
        return newPanel
    }

    private fun setTimeSpinners() {
        val date = date
        if (date != null) {
            timeSpinner!!.value = date
        }
    }

    companion object {
        private val backgroundColor = JBColor.background()
        private val foregroundColor = JBColor.foreground()
        private val selectionBackgroundColor = UIUtil.getTableSelectionBackground(true)
        private val selectionForegroundColor = UIUtil.getTableSelectionForeground(true)
        private val monthForegroundColor = StyleAttributesProvider.NUMBER_COLOR
        private val dayOfTheWeekForegroundColor = StyleAttributesProvider.KEY_COLOR
        private val todayBackgroundColor: Color = JBColor.WHITE
        fun create(): DateTimePicker {
            val dateTimePicker = DateTimePicker()
            dateTimePicker.setFormats(DateUtils.utcDateTime(Locale.getDefault()))
            dateTimePicker.setTimeFormat(DateUtils.utcTime(Locale.getDefault()))
            dateTimePicker.timeZone = TimeZone.getTimeZone("UTC")
            dateTimePicker.applyUIStyle()
            return dateTimePicker
        }
    }
}