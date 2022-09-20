/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import com.gg.plugins.mongo.config.ServerConfiguration;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoTreeBuilder implements Disposable {

	private final Map<MongoServer, ServerConfiguration> serverConfigurations = new HashMap<>();

	private final DefaultTreeModel treeModel;

	private final MongoTreeNode rootNode;

	private final TreeModelListener treeModelListener;

	public MongoTreeBuilder() {
		rootNode = new MongoTreeNode(serverConfigurations);
		treeModel = new DefaultTreeModel(rootNode, true);
		treeModelListener = new MyTreeModelListener();
		treeModel.addTreeModelListener(treeModelListener);
	}

	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}

	public MongoServer addConfiguration(@NotNull ServerConfiguration serverConfiguration) {
		MongoServer mongoServer = new MongoServer(serverConfiguration);
		serverConfigurations.put(mongoServer, serverConfiguration);
		add(new MongoTreeNode(mongoServer), rootNode);
		return mongoServer;
	}

	public void add(MongoTreeNode childNode, MongoTreeNode parentNode) {
		if (treeModel.getIndexOfChild(parentNode, childNode) == -1)
			treeModel.insertNodeInto(childNode, parentNode, treeModel.getChildCount(childNode));
	}

	public void removeAllChildren(MongoTreeNode parentNode) {
		if (parentNode.getChildCount() > 0) {
			parentNode.getChildren().forEach(treeModel::removeNodeFromParent);
		}
	}

	public void refreshNodeChildren(@NotNull MongoTreeNode parentNode, boolean recursive) {
		removeAllChildren(parentNode);
		Object userObject = parentNode.getUserObject();
		switch (parentNode.getType()) {
			case ROOT: {
				serverConfigurations.keySet().forEach(obj -> add(new MongoTreeNode(obj), rootNode));
				break;
			}
			case MongoServer: {
				((MongoServer) userObject).getDatabases().forEach(obj -> add(new MongoTreeNode(obj), parentNode));
				break;
			}
			case MongoDatabase: {
				((MongoDatabase) userObject).getCollections().forEach(obj -> add(new MongoTreeNode(obj), parentNode));
				break;
			}
			case MongoCollection:
			default:
				break;
		}
		if (recursive && parentNode.getChildCount() > 0) {
			parentNode.getChildren().forEach(childNode -> refreshNodeChildren(childNode, true));
		}
	}

	public MongoTreeNode getNodeFromUserObject(Object userObject) throws Exception {
		MongoTreeNode parentNode = null;
		if (userObject instanceof MongoServer) {
			parentNode = rootNode;
		} else if (userObject instanceof MongoDatabase) {
			parentNode = getNodeFromUserObject(((MongoDatabase) userObject).getParentServer());
		} else if (userObject instanceof MongoCollection) {
			parentNode = getNodeFromUserObject(((MongoCollection) userObject).getParentDatabase());
		} else {
			return null;
		}

		if (parentNode.getChildren() == null) {
			throw new Exception("Children don't exist for node: " + parentNode);
		}
		List<MongoTreeNode> childNodes = parentNode.getChildren()
		                                           .stream()
		                                           .filter(childNode -> childNode.getUserObject().equals(userObject))
		                                           .collect(Collectors.toList());
		if (childNodes.size() == 1)
			return childNodes.get(0);
		throw new Exception("UserObject: " + userObject + " doesn't match with children of node: " + parentNode);
	}

	public MongoTreeNode getRootNode() {
		return rootNode;
	}

	public void removeConfiguration(MongoServer mongoServer) {
		serverConfigurations.remove(mongoServer);
	}

	@Override
	public void dispose() {
		treeModel.removeTreeModelListener(treeModelListener);
	}

	public TreePath getTreePath(MongoTreeNode node) {
		return new TreePath(treeModel.getPathToRoot(node));
	}

	static class MyTreeModelListener implements TreeModelListener {
		public void treeNodesChanged(TreeModelEvent e) {
			//			DefaultMutableTreeNode node;
			//			node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());
			//
			//			/*
			//			 * If the event lists children, then the changed
			//			 * node is the child of the node we have already
			//			 * gotten.  Otherwise, the changed node and the
			//			 * specified node are the same.
			//			 */
			//			try {
			//				int index = e.getChildIndices()[0];
			//				node = (DefaultMutableTreeNode) (node.getChildAt(index));
			//			} catch (NullPointerException exc) {}
			//
			//			System.out.println("The user has finished editing the node.");
			//			System.out.println("New value: " + node.getUserObject());
		}

		public void treeNodesInserted(TreeModelEvent e) {
		}

		public void treeNodesRemoved(TreeModelEvent e) {
		}

		public void treeStructureChanged(TreeModelEvent e) {
		}
	}
}
