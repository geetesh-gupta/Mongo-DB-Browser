/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.editor;

import com.gg.plugins.mongo.service.MongoService;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class MongoDataEditorProvider implements FileEditorProvider, DumbAware {

	@Override
	public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
		return file instanceof MongoObjectFile;
	}

	@NotNull
	@Override
	public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
		MongoObjectFile mongoObjectFile = (MongoObjectFile) file;
		return new MongoDataEditor(project, MongoService.getInstance(project), mongoObjectFile);
	}

	@Override
	public void disposeEditor(@NotNull FileEditor editor) {
		editor.dispose();
	}

	@NotNull
	@Override
	public FileEditorState readState(@NotNull Element sourceElement,
			@NotNull Project project,
			@NotNull VirtualFile file) {
		return FileEditorState.INSTANCE;
	}

	@Override
	public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {

	}

	@NotNull
	@Override
	public String getEditorTypeId() {
		return "MongoData";
	}

	@NotNull
	@Override
	public FileEditorPolicy getPolicy() {
		return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
	}
}
