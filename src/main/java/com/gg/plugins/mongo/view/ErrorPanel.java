/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.HoverHyperlinkLabel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

class ErrorPanel extends JPanel {

	public ErrorPanel(final Exception ex) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBackground(JBColor.RED);
		add(new JBLabel("Error during query execution"));
		final HoverHyperlinkLabel hoverHyperlinkLabel = new HoverHyperlinkLabel("More detail...");
		hoverHyperlinkLabel.addHyperlinkListener(hyperlinkEvent -> {
			if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				Messages.showErrorDialog(ex.toString(), "Error During Query Execution");
			}
		});
		add(Box.createRigidArea(new Dimension(10, 10)));
		add(hoverHyperlinkLabel);

	}
}
