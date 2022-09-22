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

import com.gg.plugins.mongo.service.MongoService;
import com.gg.plugins.mongo.view.MongoPanel;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class MongoDataEditor extends UserDataHolderBase implements FileEditor {

	private MongoPanel panel;

	private boolean disposed;

	public MongoDataEditor(Project project, MongoService mongoService, MongoObjectFile mongoObjectFile) {
		panel = new MongoPanel(project,
				mongoService,
				mongoObjectFile.getConfiguration(),
				mongoObjectFile.getNavigation());
		ApplicationManager.getApplication().invokeLater(() -> panel.showResults());
	}

	@NotNull
	@Override
	public JComponent getComponent() {
		return isDisposed() ? new JPanel() : panel;
	}

	@Nullable
	@Override
	public JComponent getPreferredFocusedComponent() {
		return panel == null ? null : panel.getResultPanel();
	}

	@NotNull
	@Override
	public String getName() {
		return "Mongo Data";
	}

	@NotNull
	@Override
	public FileEditorState getState(@NotNull FileEditorStateLevel level) {
		return FileEditorState.INSTANCE;
	}

	@Override
	public void setState(@NotNull FileEditorState state) {

	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	//    Unused methods

	@Override
	public void selectNotify() {

	}

	@Override
	public void deselectNotify() {

	}

	@Override
	public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

	}

	@Override
	public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

	}

	@Nullable
	@Override
	public BackgroundEditorHighlighter getBackgroundHighlighter() {
		return null;
	}

	@Nullable
	@Override
	public FileEditorLocation getCurrentLocation() {
		return null;
	}

	@Nullable
	@Override
	public StructureViewBuilder getStructureViewBuilder() {
		return null;
	}

	@Override
	public @Nullable VirtualFile getFile() {
		List<VirtualFile> fileList = Arrays.stream(super.getUserMap().getKeys())
		                                   .filter(key -> super.getUserMap().get(key) instanceof VirtualFile)
		                                   .map(key -> (VirtualFile) super.getUserMap().get(key))
		                                   .collect(Collectors.toList());
		if (!fileList.isEmpty()) {
			return fileList.get(0);
		}
		return null;
	}

	private boolean isDisposed() {
		return disposed;
	}

	@Override
	public void dispose() {
		if (!disposed) {
			panel.dispose();
			panel = null;
			disposed = true;
		}
	}
}
