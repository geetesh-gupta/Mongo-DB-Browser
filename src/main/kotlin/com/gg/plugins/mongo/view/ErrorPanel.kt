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
package com.gg.plugins.mongo.view

import com.intellij.openapi.ui.Messages
import com.intellij.ui.HoverHyperlinkLabel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import java.awt.Dimension
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.event.HyperlinkEvent

internal class ErrorPanel(ex: Exception) : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        background = JBColor.RED
        add(JBLabel("Error during query execution"))
        val hoverHyperlinkLabel = HoverHyperlinkLabel("More detail...")
        hoverHyperlinkLabel.addHyperlinkListener { hyperlinkEvent: HyperlinkEvent ->
            if (hyperlinkEvent.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                Messages.showErrorDialog(ex.toString(), "Error During Query Execution")
            }
        }
        add(Box.createRigidArea(Dimension(10, 10)))
        add(hoverHyperlinkLabel)
    }
}