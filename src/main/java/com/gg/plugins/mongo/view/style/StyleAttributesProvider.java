/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view.style;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;

import java.awt.*;

public class StyleAttributesProvider {

	public static final Color KEY_COLOR = new JBColor(new Color(102, 14, 122), new Color(204, 120, 50));

	public static final Color NUMBER_COLOR = JBColor.BLUE;

	private static final Color LIGHT_GREEN = new JBColor(new Color(0, 128, 0), new Color(165, 194, 97));

	private static final Color LIGHT_GRAY = Gray._128;

	private static final SimpleTextAttributes INDEX =
			new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, JBColor.BLACK);

	private static final SimpleTextAttributes KEY_VALUE =
			new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, KEY_COLOR);

	private static final SimpleTextAttributes INTEGER_TEXT_ATTRIBUTE =
			new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, NUMBER_COLOR);

	private static final SimpleTextAttributes BOOLEAN_TEXT_ATTRIBUTE = INTEGER_TEXT_ATTRIBUTE;

	private static final SimpleTextAttributes OBJECT_ID_TEXT_ATTRIBUTE = INTEGER_TEXT_ATTRIBUTE;

	private static final SimpleTextAttributes STRING_TEXT_ATTRIBUTE =
			new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, LIGHT_GREEN);

	private static final SimpleTextAttributes NULL_TEXT_ATTRIBUTE =
			new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, LIGHT_GRAY);

	private static final SimpleTextAttributes DOCUMENT_TEXT_ATTRIBUTE =
			new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, LIGHT_GRAY);

	public static SimpleTextAttributes getIndexAttribute() {
		return INDEX;
	}

	public static SimpleTextAttributes getKeyValueAttribute() {
		return KEY_VALUE;
	}

	public static SimpleTextAttributes getNumberAttribute() {
		return INTEGER_TEXT_ATTRIBUTE;
	}

	public static SimpleTextAttributes getBooleanAttribute() {
		return BOOLEAN_TEXT_ATTRIBUTE;
	}

	public static SimpleTextAttributes getStringAttribute() {
		return STRING_TEXT_ATTRIBUTE;
	}

	public static SimpleTextAttributes getNullAttribute() {
		return NULL_TEXT_ATTRIBUTE;
	}

	public static SimpleTextAttributes getDocumentAttribute() {
		return DOCUMENT_TEXT_ATTRIBUTE;
	}

	public static SimpleTextAttributes getObjectIdAttribute() {
		return OBJECT_ID_TEXT_ATTRIBUTE;
	}
}
