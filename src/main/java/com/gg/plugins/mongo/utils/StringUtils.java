/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.utils;

public class StringUtils extends org.apache.commons.lang.StringUtils {

	private static final String ELLIPSIS = "...";

	public static String abbreviateInCenter(String stringToAbbreviate, int length) {
		if (stringToAbbreviate.length() <= length) {
			return stringToAbbreviate;
		}
		int halfLength = length / 2;
		int firstPartLastIndex = halfLength - ELLIPSIS.length();
		int stringLength = stringToAbbreviate.length();
		return String.format("%s%s%s",
				stringToAbbreviate.substring(0, firstPartLastIndex),
				ELLIPSIS,
				stringToAbbreviate.substring(stringLength - halfLength, stringLength));
	}

	public static Number parseNumber(String number) {
		try {
			return Integer.parseInt(number);

		} catch (NumberFormatException ex) {
			//UGLY :(
		}
		try {
			return Long.parseLong(number);

		} catch (NumberFormatException ex) {
			//UGLY :(
		}
		return Double.parseDouble(number);
	}
}
