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

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.NonPhysicalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.newvfs.impl.NullVirtualFile
import org.jetbrains.annotations.NonNls

class MongoFileSystem : VirtualFileSystem(), NonPhysicalFileSystem {
    override fun getProtocol(): String {
        return PROTOCOL
    }

    override fun findFileByPath(path: @NonNls String): VirtualFile? {
        return null
    }

    //    Unused methods
    override fun refresh(asynchronous: Boolean) {}
    override fun refreshAndFindFileByPath(path: String): VirtualFile? {
        return null
    }

    override fun addVirtualFileListener(listener: VirtualFileListener) {}
    override fun removeVirtualFileListener(listener: VirtualFileListener) {}
    override fun deleteFile(requestor: Any, vFile: VirtualFile) {}
    override fun moveFile(requestor: Any, vFile: VirtualFile, newParent: VirtualFile) {}
    override fun renameFile(requestor: Any, vFile: VirtualFile, newName: String) {}
    override fun createChildFile(requestor: Any, vDir: VirtualFile, fileName: String): VirtualFile {
        return NullVirtualFile.INSTANCE
    }

    override fun createChildDirectory(requestor: Any, vDir: VirtualFile, dirName: String): VirtualFile {
        return NullVirtualFile.INSTANCE
    }

    override fun copyFile(
        requestor: Any,
        virtualFile: VirtualFile,
        newParent: VirtualFile,
        copyName: String
    ): VirtualFile {
        return NullVirtualFile.INSTANCE
    }

    override fun isReadOnly(): Boolean {
        return true
    }

    fun openEditor(mongoObjectFile: MongoObjectFile) {
        val fileEditorManager = FileEditorManager.getInstance(mongoObjectFile.project)
        fileEditorManager.openFile(mongoObjectFile, true)
        fileEditorManager.setSelectedEditor(mongoObjectFile, "MongoData")
    }

    companion object {
        private const val PROTOCOL = "mongo"
        fun getInstance(): MongoFileSystem {
            return VirtualFileManager.getInstance().getFileSystem(PROTOCOL) as MongoFileSystem
        }
    }
}