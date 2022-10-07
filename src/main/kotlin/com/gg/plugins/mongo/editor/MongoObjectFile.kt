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

import com.gg.plugins.mongo.config.ServerConfiguration
import com.gg.plugins.mongo.model.navigation.Navigation
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.util.LocalTimeCounter
import java.io.InputStream
import java.io.OutputStream

class MongoObjectFile(val project: Project, val configuration: ServerConfiguration, val navigation: Navigation) :
    VirtualFile() {
    private val myModStamp: Long = LocalTimeCounter.currentTime()
    private val path: String = name

    override fun getName(): String {
        return String.format("%s/%s", configuration.label, navigation.currentWayPoint?.label)
    }

    override fun getFileSystem(): VirtualFileSystem {
        return MongoFileSystem.getInstance()
    }

    override fun getPath(): String {
        return path
    }

    override fun isWritable(): Boolean {
        return false
    }

    override fun isDirectory(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    //    Unused methods
    override fun getParent(): VirtualFile? {
        return null
    }

    override fun getChildren(): Array<VirtualFile?> {
        return arrayOfNulls(0)
    }

    override fun getFileType(): FileType {
        return MongoFakeFileType.INSTANCE
    }

    override fun getOutputStream(requestor: Any, newModificationStamp: Long, newTimeStamp: Long): OutputStream {
        throw UnsupportedOperationException("MongoResultFile is read-only")
    }

    override fun contentsToByteArray(): ByteArray {
        return ByteArray(0)
    }

    override fun getModificationStamp(): Long {
        return myModStamp
    }

    override fun getTimeStamp(): Long {
        return 0
    }

    override fun getLength(): Long {
        return 0
    }

    override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) {}

    override fun getInputStream(): InputStream {
        return InputStream.nullInputStream()
    }
}