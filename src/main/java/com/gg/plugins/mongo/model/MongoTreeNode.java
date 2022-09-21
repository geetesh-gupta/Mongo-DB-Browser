/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import com.gg.plugins.mongo.config.ServerConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class MongoTreeNode extends DefaultMutableTreeNode {

	private final MongoTreeNodeEnum type;

	public MongoTreeNode(@NotNull Object userObject) {
		super(userObject, !(userObject instanceof MongoCollection));
		this.type = MongoTreeNode.getTypeFromUserObject(userObject);
	}

	public static MongoTreeNodeEnum getTypeFromUserObject(Object element) {
		if (element instanceof MongoServer) {
			return MongoTreeNodeEnum.MongoServer;
		} else if (element instanceof MongoDatabase) {
			return MongoTreeNodeEnum.MongoDatabase;
		} else if (element instanceof MongoCollection) {
			return MongoTreeNodeEnum.MongoCollection;
		}
		return MongoTreeNodeEnum.ROOT;
	}

	public static ServerConfiguration getServerConfiguration(Object element) {
		if (element instanceof MongoServer) {
			return ((MongoServer) element).getConfiguration();
		} else if (element instanceof MongoDatabase) {
			return ((MongoDatabase) element).getParentServer().getConfiguration();
		} else if (element instanceof MongoCollection) {
			return ((MongoCollection) element).getParentDatabase().getParentServer().getConfiguration();
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		assert obj instanceof MongoTreeNode;
		return toString().equals(obj.toString());
	}

	public List<MongoTreeNode> getChildren() {
		List<MongoTreeNode> nodes = new ArrayList<>();
		if (getChildCount() > 0)
			children.forEach(c -> nodes.add((MongoTreeNode) c));
		return nodes;
	}

	public MongoTreeNodeEnum getType() {
		return type;
	}

	@Override
	public MongoTreeNode getParent() {
		return (MongoTreeNode) super.getParent();
	}

	@Override
	public MongoTreeNode getRoot() {
		return (MongoTreeNode) super.getRoot();
	}

	@Override
	public String toString() {
		switch (type) {
			case ROOT:
				return "<root>";
			case MongoServer:
				return ((MongoServer) userObject).getLabel();
			case MongoDatabase:
				return ((MongoDatabase) userObject).getName();
			case MongoCollection:
				return ((MongoCollection) userObject).getName();
			default:
				return userObject != null ? userObject.toString() : "<other>";
		}
	}
}
