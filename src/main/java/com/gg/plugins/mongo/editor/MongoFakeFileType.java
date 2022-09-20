/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.editor;

import com.gg.plugins.mongo.utils.GuiUtils;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.ex.FakeFileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class MongoFakeFileType extends FakeFileType {

	public static final FileType INSTANCE = new MongoFakeFileType();

	private static final Icon MONGO_ICON = GuiUtils.loadIcon("mongo_logo.png");

	@Override
	public boolean isMyFileType(@NotNull VirtualFile file) {
		return false;
	}

	@NotNull
	@Override
	public String getDefaultExtension() {
		return "json";
	}

	@Override
	public Icon getIcon() {
		return MONGO_ICON;
	}

	@NotNull
	@Override
	public String getName() {
		return "MONGO";
	}

	@NotNull
	@Override
	public String getDescription() {
		return "MONGO";
	}
}
