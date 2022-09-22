/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import com.gg.plugins.mongo.view.nodedescriptor.MongoNodeDescriptor;

import javax.swing.tree.DefaultMutableTreeNode;

public class JsonTreeNode extends DefaultMutableTreeNode {
	private final MongoNodeDescriptor nodeDescriptor;

	public JsonTreeNode(MongoNodeDescriptor nodeDescriptor) {
		this.nodeDescriptor = nodeDescriptor;
		setUserObject(nodeDescriptor);
	}

	public MongoNodeDescriptor getDescriptor() {
		return nodeDescriptor;
	}
}
