/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.editor;

import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.model.navigation.Navigation;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;

public class MongoObjectFile extends VirtualFile {

	private final long myModStamp;

	private final ServerConfiguration configuration;

	private final Navigation navigation;

	private final Project project;

	private final String path;

	public MongoObjectFile(Project project, ServerConfiguration configuration, Navigation navigation) {
		this.project = project;
		this.configuration = configuration;
		this.navigation = navigation;
		this.myModStamp = LocalTimeCounter.currentTime();
		this.path = getName();
	}

	@NotNull
	@Override
	public String getName() {
		return String.format("%s/%s", configuration.getLabel(), navigation.getCurrentWayPoint().getLabel());
	}

	@NotNull
	@Override
	public VirtualFileSystem getFileSystem() {
		return MongoFileSystem.getInstance();
	}

	@NotNull
	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	//    Unused methods
	@Override
	public VirtualFile getParent() {
		return null;
	}

	@Override
	public VirtualFile[] getChildren() {
		return new VirtualFile[0];
	}

	@NotNull
	public FileType getFileType() {
		return MongoFakeFileType.INSTANCE;
	}

	@NotNull
	@Override
	public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) {
		throw new UnsupportedOperationException("MongoResultFile is read-only");
	}

	@NotNull
	@Override
	public byte[] contentsToByteArray() {
		return new byte[0];
	}

	@Override
	public long getModificationStamp() {
		return myModStamp;
	}

	@Override
	public long getTimeStamp() {
		return 0;
	}

	@Override
	public long getLength() {
		return 0;
	}

	@Override
	public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {

	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	public ServerConfiguration getConfiguration() {
		return configuration;
	}

	public Navigation getNavigation() {
		return navigation;
	}

	public Project getProject() {
		return project;
	}
}
