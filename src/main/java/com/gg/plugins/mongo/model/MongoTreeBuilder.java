/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.model;

import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.view.MongoExplorerPanel;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.Map;

import static com.gg.plugins.mongo.model.MongoConstants.*;

public class MongoTreeBuilder implements Disposable {
	private static final RootDescriptor ROOT_DESCRIPTOR = new RootDescriptor();

	private final Map<MongoServer, ServerConfiguration> serverConfigurations = new HashMap<>();

	private final DefaultTreeModel treeModel;

	private final DefaultMutableTreeNode rootNode;

	private final TreeModelListener treeModelListener;

	public MongoTreeBuilder(Tree tree, MongoExplorerPanel mongoExplorerPanel) {
		rootNode = new DefaultMutableTreeNode(ROOT_DESCRIPTOR);
		treeModel = new DefaultTreeModel(rootNode);
		treeModelListener = new MyTreeModelListener();
		treeModel.addTreeModelListener(treeModelListener);
	}

	public DefaultTreeModel getTreeModel() {
		return treeModel;
	}

	public MongoServer addConfiguration(@NotNull ServerConfiguration serverConfiguration) {
		MongoServer mongoServer = new MongoServer(serverConfiguration);
		serverConfigurations.put(mongoServer, serverConfiguration);
		treeModel.insertNodeInto(new DefaultMutableTreeNode(mongoServer), rootNode, treeModel.getChildCount(rootNode));

		//		treeModel.invalidate(new TreePath(RootDescriptor.ROOT), true);
		return mongoServer;
	}

	public void openServer(@NotNull MongoServer mongoServer) throws Exception {
		DefaultMutableTreeNode serverNode = null;

		for (int i = 0; i < rootNode.getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode.getChildAt(i);
			if (((MongoServer) node.getUserObject()).getLabel().equals(mongoServer.getLabel())) {
				serverNode = node;
				break;
			}
		}
		if (serverNode == null) {
			throw new Exception(String.format("Server %s does not exists", mongoServer));
		}
		DefaultMutableTreeNode finalServerNode = serverNode;
		mongoServer.getDatabases().forEach(db -> {
			DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode(db);
			treeModel.insertNodeInto(dbNode, finalServerNode, treeModel.getChildCount(finalServerNode));
			db.getCollections().forEach(c -> {
				treeModel.insertNodeInto(new DefaultMutableTreeNode(c), dbNode, treeModel.getChildCount(dbNode));
			});
		});
	}

	//	public void addNode(@NotNull Object element) {
	//		if (element instanceof MongoServer) {
	//			treeModel.insertNodeInto(new DefaultMutableTreeNode(element), rootNode, treeModel.getChildCount
	//			(rootNode));
	//		} else if (element instanceof MongoDatabase) {
	//			MongoServer parentNode = ((MongoDatabase) element).getParentServer();
	//			while (rootNode.children().hasMoreElements()) {
	//				DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode.children().nextElement();
	//				if (((MongoServer) node.getUserObject()).getLabel().equals(parentNode.getLabel())) {
	//					treeModel.insertNodeInto(new DefaultMutableTreeNode(element),
	//							node,
	//							treeModel.getChildCount(rootNode));
	//					break;
	//				}
	//			}
	//		} else if (element instanceof MongoCollection) {
	//			MongoServer parentNode = ((MongoDatabase) element).getParentServer();
	//			while (rootNode.children().hasMoreElements()) {
	//				DefaultMutableTreeNode node = (DefaultMutableTreeNode) rootNode.children().nextElement();
	//				if (((MongoServer) node.getUserObject()).getLabel().equals(parentNode.getLabel())) {
	//					treeModel.insertNodeInto(new DefaultMutableTreeNode(element),
	//							node,
	//							treeModel.getChildCount(rootNode));
	//					break;
	//				}
	//			}
	//			return ArrayUtil.toObjectArray(((MongoDatabase) element).getCollections());
	//		}
	//		return ArrayUtil.EMPTY_OBJECT_ARRAY;
	//
	//	}

	public void removeConfiguration(MongoServer mongoServer) {
		serverConfigurations.remove(mongoServer);
		//		treeModel.invalidate(RootDescriptor.ROOT, true);
	}

	@Override
	public void dispose() {
		treeModel.removeTreeModelListener(treeModelListener);
	}

	private static abstract class MyNodeDescriptor<T> extends PresentableNodeDescriptor<T> {
		private final T myObject;

		MyNodeDescriptor(@Nullable NodeDescriptor parentDescriptor, @NotNull T object) {
			super(null, parentDescriptor);
			myObject = object;
		}

		@Override
		public T getElement() {
			return myObject;
		}
	}

	private static class RootDescriptor extends MyNodeDescriptor<Object> {
		static final Object ROOT = new Object();

		private RootDescriptor() {
			super(null, ROOT);
		}

		@Override
		protected void update(PresentationData presentation) {
			presentation.addText("<root>", SimpleTextAttributes.REGULAR_ATTRIBUTES);
		}
	}

	static class ServerDescriptor extends MyNodeDescriptor<MongoServer> {
		ServerDescriptor(NodeDescriptor parentDescriptor, MongoServer server) {
			super(parentDescriptor, server);
		}

		@Override
		protected void update(@NotNull PresentationData presentation) {
			MongoServer mongoServer = getElement();
			if (MongoServer.Status.ERROR.equals(mongoServer.getStatus())) {
				presentation.setIcon(MONGO_SERVER_ERROR);
			} else {
				presentation.setIcon(MONGO_SERVER);
			}

			String label = mongoServer.getLabel();
			if (MongoServer.Status.LOADING.equals(mongoServer.getStatus())) {
				label += " (loading)";
			}
			presentation.addText(label, SimpleTextAttributes.REGULAR_ATTRIBUTES);
			presentation.setTooltip(mongoServer.getConfiguration().getUrlsInSingleString());
		}
	}

	static class DatabaseDescriptor extends MyNodeDescriptor<MongoDatabase> {
		DatabaseDescriptor(NodeDescriptor parentDescriptor, MongoDatabase database) {
			super(parentDescriptor, database);
		}

		@Override
		protected void update(PresentationData presentation) {
			presentation.setIcon(MONGO_DATABASE);
			presentation.addText(getElement().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
		}
	}

	static class CollectionDescriptor extends MyNodeDescriptor<MongoCollection> {
		CollectionDescriptor(NodeDescriptor parentDescriptor, MongoCollection collection) {
			super(parentDescriptor, collection);
		}

		@Override
		protected void update(PresentationData presentation) {
			presentation.setIcon(MONGO_COLLECTION);
			presentation.addText(getElement().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
		}
	}

	static class MyTreeModelListener implements TreeModelListener {
		public void treeNodesChanged(TreeModelEvent e) {
			DefaultMutableTreeNode node;
			node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());

			/*
			 * If the event lists children, then the changed
			 * node is the child of the node we have already
			 * gotten.  Otherwise, the changed node and the
			 * specified node are the same.
			 */
			try {
				int index = e.getChildIndices()[0];
				node = (DefaultMutableTreeNode) (node.getChildAt(index));
			} catch (NullPointerException exc) {}

			System.out.println("The user has finished editing the node.");
			System.out.println("New value: " + node.getUserObject());
		}

		public void treeNodesInserted(TreeModelEvent e) {
		}

		public void treeNodesRemoved(TreeModelEvent e) {
		}

		public void treeStructureChanged(TreeModelEvent e) {
		}
	}

	private class MongoTreeStructure extends AbstractTreeStructure {
		@Override
		public @NotNull Object getRootElement() {
			return RootDescriptor.ROOT;
		}

		@Override
		public Object @NotNull [] getChildElements(@NotNull Object element) {
			if (element == RootDescriptor.ROOT) {
				return ArrayUtil.toObjectArray(serverConfigurations.keySet());
			} else if (element instanceof MongoServer) {
				return ArrayUtil.toObjectArray(((MongoServer) element).getDatabases());
			} else if (element instanceof MongoDatabase) {
				return ArrayUtil.toObjectArray(((MongoDatabase) element).getCollections());
			}
			return ArrayUtil.EMPTY_OBJECT_ARRAY;
		}

		@Nullable
		@Override
		public Object getParentElement(@NotNull Object element) {
			if (element == RootDescriptor.ROOT) {
				return null;
			} else if (element instanceof MongoServer) {
				return RootDescriptor.ROOT;
			} else if (element instanceof MongoDatabase) {
				return ((MongoDatabase) element).getParentServer();
			} else if (element instanceof MongoCollection) {
				return ((MongoCollection) element).getParentDatabase();
			} else {
				return null;
			}
		}

		@NotNull
		@Override
		public NodeDescriptor createDescriptor(@NotNull Object element, NodeDescriptor parentDescriptor) {
			if (element == RootDescriptor.ROOT) {
				return ROOT_DESCRIPTOR;
			} else if (element instanceof MongoServer) {
				return new ServerDescriptor(parentDescriptor, (MongoServer) element);
			} else if (element instanceof MongoDatabase) {
				return new DatabaseDescriptor(parentDescriptor, (MongoDatabase) element);
			} else if (element instanceof MongoCollection) {
				return new CollectionDescriptor(parentDescriptor, (MongoCollection) element);
			}

			throw new IllegalStateException("Element not supported : " + element.getClass().getName());
		}

		@Override
		public void commit() {
			// do nothing
		}

		@Override
		public boolean hasSomethingToCommit() {
			return false;
		}
	}
}
