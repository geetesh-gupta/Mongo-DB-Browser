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
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jdom.Element

class MongoDataEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file is MongoObjectFile
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val mongoObjectFile = file as MongoObjectFile
        return MongoDataEditor(project, MongoService.getInstance(project), mongoObjectFile)
    }

    override fun disposeEditor(editor: FileEditor) {
        editor.dispose()
    }

    override fun readState(
        sourceElement: Element,
        project: Project,
        file: VirtualFile
    ): FileEditorState {
        return FileEditorState.INSTANCE
    }

    override fun writeState(state: FileEditorState, project: Project, targetElement: Element) {}
    override fun getEditorTypeId(): String {
        return "MongoData"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR
    }
}