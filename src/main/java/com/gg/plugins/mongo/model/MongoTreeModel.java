/*
 * Copyright (c) 2022 Geetesh Gupta.
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

package com.gg.plugins.mongo.model;

import com.gg.plugins.mongo.config.ServerConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MongoTreeModel implements TreeModel {
	protected final List<TreeModelListener> listenerList = new ArrayList<>();

	private final Map<MongoServer, ServerConfiguration> mongoServerMap = new TreeMap<>();

	private final String rootNode = "<root>";

	public MongoTreeModel() {
	}

	@Override
	public Object getRoot() {
		return rootNode;
	}

	@Override
	public Object getChild(Object parent, int index) {
		List<Object> children = getChildren(parent);
		if (children.isEmpty())
			return null;
		return children.get(index);
	}

	@Override
	public int getChildCount(Object parent) {
		return getChildren(parent).size();
	}

	@Override
	public boolean isLeaf(Object node) {
		return node instanceof MongoCollection;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return getChildren(parent).indexOf(child);
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(l);
	}

	public @NotNull List<Object> getChildren(Object parent) {
		if (parent.equals(rootNode)) {
			return new ArrayList<>(mongoServerMap.keySet());
		} else if (parent instanceof MongoServer) {
			return new ArrayList<>(((MongoServer) parent).getDatabases());
		} else if (parent instanceof MongoDatabase) {
			return new ArrayList<>(((MongoDatabase) parent).getCollections());
		} else {
			return new ArrayList<>();
		}
	}

	public void removeConfiguration(MongoServer mongoServer) {
		mongoServerMap.remove(mongoServer);
	}

	public MongoServer addConfiguration(ServerConfiguration serverConfiguration) {
		MongoServer mongoServer = new MongoServer(serverConfiguration);
		mongoServerMap.put(mongoServer, serverConfiguration);
		fireTreeStructureChanged(rootNode);
		return mongoServer;
	}

	public void fireTreeStructureChanged(Object node) {
		TreeModelEvent e = new TreeModelEvent(this, getPathToRoot(node));
		for (TreeModelListener listener : listenerList) {
			listener.treeStructureChanged(e);
		}
	}

	public Object[] getPathToRoot(Object aNode) {
		return getPathToRoot(aNode, 0);
	}

	public Object getParent(Object node) {
		if (node instanceof MongoServer)
			return rootNode;
		else if (node instanceof MongoDatabase)
			return ((MongoDatabase) node).getParentServer();
		else if (node instanceof MongoCollection)
			return ((MongoCollection) node).getParentDatabase();
		return null;
	}

	protected Object[] getPathToRoot(Object obj, int depth) {
		Object[] retNodes;
		if (obj == null) {
			if (depth == 0)
				return null;
			else
				retNodes = new Object[depth];
		} else {
			depth++;
			if (obj.equals(rootNode))
				retNodes = new Object[depth];
			else
				retNodes = getPathToRoot(getParent(obj), depth);
			retNodes[retNodes.length - depth] = obj;
		}
		return retNodes;
	}

	public String getTypeOfNode(Object node) {
		if (node.equals(rootNode))
			return "<root>";
		else if (node instanceof MongoServer)
			return "Server";
		else if (node instanceof MongoDatabase)
			return "Database";
		else if (node instanceof MongoCollection)
			return "Collection";
		return "<undefined>";
	}

	public ServerConfiguration getServerConfiguration(Object node) {
		if (node instanceof MongoServer)
			return ((MongoServer) node).getConfiguration();
		else if (node instanceof MongoDatabase)
			return getServerConfiguration(((MongoDatabase) node).getParentServer());
		else if (node instanceof MongoCollection)
			return getServerConfiguration(((MongoCollection) node).getParentDatabase());
		return null;
	}

}
