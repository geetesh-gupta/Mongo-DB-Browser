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
package com.gg.plugins.mongo.utils

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.UIManager

object GuiUtils {
    private const val ICON_FOLDER = "/icons/"
    fun loadIcon(iconFilename: String): Icon? {
        return IconLoader.findIcon(ICON_FOLDER + iconFilename, GuiUtils::class.java)
    }

    fun loadIcon(iconFilename: String?, darkIconFilename: String?): Icon? {
        var iconPath: String? = ICON_FOLDER
        iconPath += if (isUnderDarcula) {
            darkIconFilename
        } else {
            iconFilename
        }
        return IconLoader.findIcon(iconPath!!, GuiUtils::class.java)
    }

    private val isUnderDarcula: Boolean
        get() = UIManager.getLookAndFeel().name.contains("Darcula")

    fun installActionGroupInToolBar(
        actionGroup: DefaultActionGroup?,
        toolBarPanel: JPanel?,
        actionManager: ActionManager?,
        toolbarName: String?,
        horizontal: Boolean
    ) {
        if (actionManager == null) {
            return
        }
        val a = ActionManager.getInstance().createActionToolbar(toolbarName!!, actionGroup!!, horizontal)
        a.targetComponent = a.component
        val actionToolbar = a.component
        toolBarPanel!!.add(actionToolbar, BorderLayout.CENTER)
    }

    fun enlargeWidth(preferredSize: Dimension, factor: Double): Dimension {
        val enlargedWidth = java.lang.Double.valueOf(preferredSize.width * factor).toInt()
        return Dimension(enlargedWidth, preferredSize.height)
    }

    fun showNotification(
        component: JComponent?,
        info: MessageType,
        message: String?,
        position: Balloon.Position?
    ) {
        UIUtil.invokeLaterIfNeeded {
            JBPopupFactory.getInstance()
                .createBalloonBuilder(JLabel(message))
                .setFillColor(info.popupBackground)
                .createBalloon()
                .show(RelativePoint(component!!, Point(0, 0)), position)
        }
    }
}