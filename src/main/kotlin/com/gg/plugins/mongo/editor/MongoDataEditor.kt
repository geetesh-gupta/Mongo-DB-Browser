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
package com.gg.plugins.mongo.editor

import com.gg.plugins.mongo.service.MongoService
import com.gg.plugins.mongo.view.MongoPanel
import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.FileEditorStateLevel
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import java.beans.PropertyChangeListener
import java.util.Arrays
import java.util.stream.Collectors
import javax.swing.JComponent
import javax.swing.JPanel

internal class MongoDataEditor(project: Project, mongoService: MongoService, mongoObjectFile: MongoObjectFile) :
    UserDataHolderBase(), FileEditor {
    private var panel: MongoPanel? = null
    private var isDisposed = false

    init {
        panel = MongoPanel(
            project,
            mongoService,
            mongoObjectFile.configuration,
            mongoObjectFile.navigation
        )
        ApplicationManager.getApplication().invokeLater { panel?.showResults() }
    }

    override fun getComponent(): JPanel {
        return if (isDisposed) JPanel() else panel!!
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return panel?.resultPanel
    }

    override fun getName(): String {
        return "Mongo Data"
    }

    override fun getState(level: FileEditorStateLevel): FileEditorState {
        return FileEditorState.INSTANCE
    }

    override fun setState(state: FileEditorState) {}
    override fun isModified(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    //    Unused methods
    override fun selectNotify() {}
    override fun deselectNotify() {}
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return null
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        return null
    }

    override fun getStructureViewBuilder(): StructureViewBuilder? {
        return null
    }

    override fun getFile(): VirtualFile? {
        val fileList = Arrays.stream(super.getUserMap().keys)
            .filter { key -> super.getUserMap().get(key) is VirtualFile }
            .map { key -> super.getUserMap().get(key) as VirtualFile }
            .collect(Collectors.toList())
        return if (fileList.isNotEmpty()) {
            fileList[0]
        } else null
    }

    override fun dispose() {
        if (!isDisposed) {
            panel?.dispose()
            panel = null
            isDisposed = true
        }
    }
}