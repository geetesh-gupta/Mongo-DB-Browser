/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.utils;

import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

	public static DateFormat utcDateTime(Locale locale) {
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, locale);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format;
	}

	public static DateFormat utcTime(Locale locale) {
		DateFormat format = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format;
	}
}
