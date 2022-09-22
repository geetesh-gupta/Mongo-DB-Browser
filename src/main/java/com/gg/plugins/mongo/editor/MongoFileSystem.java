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

package com.gg.plugins.mongo.editor;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.newvfs.impl.NullVirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MongoFileSystem extends VirtualFileSystem implements NonPhysicalFileSystem {

	private static final String PROTOCOL = "mongo";

	public static MongoFileSystem getInstance() {
		return (MongoFileSystem) VirtualFileManager.getInstance().getFileSystem(PROTOCOL);
	}

	@NotNull
	@Override
	public String getProtocol() {
		return PROTOCOL;
	}

	@Nullable
	@Override
	public VirtualFile findFileByPath(@NotNull @NonNls String path) {
		return null;
	}

	//    Unused methods

	@Override
	public void refresh(boolean asynchronous) {

	}

	@Nullable
	@Override
	public VirtualFile refreshAndFindFileByPath(@NotNull String path) {
		return null;
	}

	@Override
	public void addVirtualFileListener(@NotNull VirtualFileListener listener) {

	}

	@Override
	public void removeVirtualFileListener(@NotNull VirtualFileListener listener) {

	}

	@Override
	protected void deleteFile(Object requestor, @NotNull VirtualFile vFile) {

	}

	@Override
	protected void moveFile(Object requestor, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent) {

	}

	@Override
	protected void renameFile(Object requestor, @NotNull VirtualFile vFile, @NotNull String newName) {

	}

	@NotNull
	@Override
	protected VirtualFile createChildFile(Object requestor, @NotNull VirtualFile vDir, @NotNull String fileName) {
		return NullVirtualFile.INSTANCE;
	}

	@NotNull
	@Override
	protected VirtualFile createChildDirectory(Object requestor, @NotNull VirtualFile vDir, @NotNull String dirName) {
		return NullVirtualFile.INSTANCE;
	}

	@NotNull
	@Override
	protected VirtualFile copyFile(Object requestor,
			@NotNull VirtualFile virtualFile,
			@NotNull VirtualFile newParent,
			@NotNull String copyName) {
		return NullVirtualFile.INSTANCE;
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	public void openEditor(final MongoObjectFile mongoObjectFile) {
		FileEditorManager fileEditorManager = FileEditorManager.getInstance(mongoObjectFile.getProject());
		fileEditorManager.openFile(mongoObjectFile, true);
		fileEditorManager.setSelectedEditor(mongoObjectFile, "MongoData");
	}
}
