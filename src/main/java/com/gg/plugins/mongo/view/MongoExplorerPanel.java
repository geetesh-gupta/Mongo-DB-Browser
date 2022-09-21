/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view;

import com.gg.plugins.mongo.action.explorer.*;
import com.gg.plugins.mongo.config.ConfigurationException;
import com.gg.plugins.mongo.config.MongoConfiguration;
import com.gg.plugins.mongo.config.ServerConfiguration;
import com.gg.plugins.mongo.editor.MongoFileSystem;
import com.gg.plugins.mongo.editor.MongoObjectFile;
import com.gg.plugins.mongo.model.*;
import com.gg.plugins.mongo.model.navigation.Navigation;
import com.gg.plugins.mongo.service.MongoService;
import com.gg.plugins.mongo.service.Notifier;
import com.gg.plugins.mongo.utils.GuiUtils;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Set;

import static com.gg.plugins.mongo.utils.GuiUtils.showNotification;

public class MongoExplorerPanel extends JPanel implements Disposable {

	private static final URL pluginSettingsUrl = GuiUtils.class.getResource("/general/add.png");

	private final Project project;

	private final MongoService mongoService;

	private final Notifier notifier;

	private final JPanel treePanel;

	private final Tree mongoTree;

	private final MongoTreeBuilder mongoTreeBuilder;

	private JPanel rootPanel;

	private JPanel toolBarPanel;

	private JPanel containerPanel;

	public MongoExplorerPanel(Project project) {
		this.project = project;
		this.mongoService = MongoService.getInstance(project);
		this.notifier = Notifier.getInstance(project);

		treePanel = new JPanel(new BorderLayout());
		treePanel.setLayout(new BorderLayout());

		mongoTree = createTree();
		mongoTreeBuilder = new MongoTreeBuilder();
		mongoTree.setModel(mongoTreeBuilder.getTreeModel());

		setLayout(new BorderLayout());
		treePanel.add(new JBScrollPane(mongoTree), BorderLayout.CENTER);

		Splitter splitter = new Splitter(true, 0.6f);
		splitter.setFirstComponent(treePanel);

		containerPanel.add(splitter, BorderLayout.CENTER);

		add(rootPanel, BorderLayout.CENTER);

		toolBarPanel.setLayout(new BorderLayout());

		init();
	}

	private Tree createTree() {

		Tree tree = new Tree() {

			private final JLabel myLabel = new JLabel(String.format(
					"<html><center>No Mongo server available<br><br>You may use <img src=\"%s\"> to add " +
					"configuration</center></html>",
					pluginSettingsUrl));

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (!getServerConfigurations().isEmpty())
					return;

				myLabel.setFont(getFont());
				myLabel.setBackground(getBackground());
				myLabel.setForeground(getForeground());
				Rectangle bounds = getBounds();
				Dimension size = myLabel.getPreferredSize();
				myLabel.setBounds(0, 0, size.width, size.height);

				int x = (bounds.width - size.width) / 2;
				Graphics g2 = g.create(bounds.x + x, bounds.y + 20, bounds.width, bounds.height);
				try {
					myLabel.paint(g2);
				} finally {
					g2.dispose();
				}
			}
		};

		tree.getEmptyText().clear();
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setName("mongoTree");
		tree.setRootVisible(false);

		new TreeSpeedSearch(tree, treePath -> {
			final MongoTreeNode node = (MongoTreeNode) treePath.getLastPathComponent();
			final Object userObject = node.getUserObject();
			if (userObject instanceof MongoDatabase) {
				return ((MongoDatabase) userObject).getName();
			}
			if (userObject instanceof MongoCollection) {
				return ((MongoCollection) userObject).getName();
			}
			return "<empty>";
		});

		return tree;
	}

	public void init() {
		loadAllServerConfigurations();
		installActions();
		doubleClickEventHandler();
	}

	private Set<ServerConfiguration> getServerConfigurations() {
		return MongoConfiguration.getInstance(project).getServerConfigurations();
	}

	private void loadAllServerConfigurations() {
		this.mongoService.cleanUpServers();

		Set<ServerConfiguration> serverConfigurations = getServerConfigurations();
		for (ServerConfiguration serverConfiguration : serverConfigurations) {
			addConfiguration(serverConfiguration);
		}
	}

	private void installActions() {
		Actions actions = new Actions();
		Disposer.register(this, () -> {
			actions.collapseAllAction.unregisterCustomShortcutSet(rootPanel);
			actions.expandAllAction.unregisterCustomShortcutSet(rootPanel);
		});

		DefaultActionGroup actionGroup = new DefaultActionGroup("MongoExplorerGroup", false);

		if (ApplicationManager.getApplication() != null) {
			actionGroup.add(actions.addServerAction);
			actionGroup.add(actions.duplicateServerAction);
			actionGroup.addSeparator();
			actionGroup.add(actions.refreshServerAction);
			actionGroup.add(actions.mongoConsoleAction);
			actionGroup.addSeparator();
			actionGroup.add(actions.expandAllAction);
			actionGroup.add(actions.collapseAllAction);
			actionGroup.addSeparator();
			actionGroup.add(actions.openPluginSettingsAction);
		}
		GuiUtils.installActionGroupInToolBar(actionGroup,
				toolBarPanel,
				ActionManager.getInstance(),
				"MongoExplorerActions",
				true);

		DefaultActionGroup actionPopupGroup = new DefaultActionGroup("MongoExplorerPopupGroup", true);
		if (ApplicationManager.getApplication() != null) {
			actionPopupGroup.add(actions.refreshServerAction);
			actionPopupGroup.add(actions.editServerAction);
			actionPopupGroup.add(actions.duplicateServerAction);
			actionPopupGroup.add(actions.deleteAction);
			actionPopupGroup.addSeparator();
			actionPopupGroup.add(actions.viewCollectionValuesAction);
		}
		PopupHandler.installPopupMenu(mongoTree, actionPopupGroup, "POPUP");
	}

	private void doubleClickEventHandler() {
		new DoubleClickListener() {
			@Override
			protected boolean onDoubleClick(@NotNull MouseEvent event) {
				if (!(event.getSource() instanceof JTree)) {
					return false;
				}

				MongoTreeNode node = getSelectedNode();
				Object userObject = node.getUserObject();

				if (userObject instanceof MongoServer && node.getChildren().isEmpty()) {
					openServer(node);
				} else if (userObject instanceof MongoDatabase && node.getChildren().isEmpty()) {
					loadDatabase(node);
				} else if (userObject instanceof MongoCollection) {
					loadSelectedCollectionValues((MongoCollection) userObject);
				}
				return false;
			}
		}.installOn(mongoTree);
	}

	public void addConfiguration(ServerConfiguration serverConfiguration) {
		MongoServer mongoServer = mongoTreeBuilder.addConfiguration(serverConfiguration);
		mongoService.registerServer(mongoServer);
	}

	public MongoTreeNode getSelectedNode() {
		return (MongoTreeNode) mongoTree.getLastSelectedPathComponent();
	}

	public void openServer(final MongoTreeNode serverNode) {
		assert serverNode.getType() == MongoTreeNodeEnum.MongoServer;
		MongoServer mongoServer = (MongoServer) serverNode.getUserObject();
		ProgressManager.getInstance().run(new Task.Backgroundable(project, "Connecting to " + mongoServer.getLabel()) {

			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				mongoTree.setPaintBusy(true);
				ApplicationManager.getApplication().invokeLater(() -> {
					try {
						Set<MongoDatabase> mongoDatabases =
								mongoService.loadDatabases(mongoServer, mongoServer.getConfiguration());
						if (mongoDatabases.isEmpty()) {
							return;
						}
						MongoServer newMongoServer = new MongoServer(getServerConfiguration(serverNode));
						newMongoServer.setDatabases(mongoDatabases);

						MongoTreeNode newMongoServerNode = new MongoTreeNode(newMongoServer);
						refreshNodeChildren(newMongoServerNode);
						mongoTreeBuilder.replace(serverNode, newMongoServerNode);
					} catch (ConfigurationException confEx) {
						mongoServer.setStatus(MongoServer.Status.ERROR);

						String errorMessage = String.format("Error when connecting to %s", mongoServer.getLabel());
						notifier.notifyError(errorMessage + ": " + confEx.getMessage());
						UIUtil.invokeLaterIfNeeded(() -> showNotification(treePanel,
								MessageType.ERROR,
								errorMessage,
								Balloon.Position.atLeft));
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						mongoTree.setPaintBusy(false);
					}
				});
			}
		});
	}

	public void loadDatabase(MongoTreeNode dbNode) {
		assert dbNode.getType() == MongoTreeNodeEnum.MongoDatabase;
		MongoTreeNode serverNode = dbNode.getParent();
		ServerConfiguration configuration = getServerConfiguration(serverNode);
		MongoDatabase mongoDatabase = (MongoDatabase) dbNode.getUserObject();
		MongoDatabase mongoDatabaseUpdated = mongoService.loadDatabase(mongoDatabase, configuration);
		MongoTreeNode newDbNode = new MongoTreeNode(mongoDatabaseUpdated);
		refreshNodeChildren(newDbNode);
		mongoTreeBuilder.replace(dbNode, newDbNode);
	}

	public void loadSelectedCollectionValues(MongoCollection mongoCollection) {
		MongoServer parentServer = mongoCollection.getParentDatabase().getParentServer();
		ServerConfiguration configuration = parentServer.getConfiguration();

		Navigation navigation = new Navigation();
		MongoQueryOptions queryOptions = new MongoQueryOptions();
		queryOptions.setResultLimit(configuration.getDefaultRowLimit());
		navigation.addNewWayPoint(mongoCollection, queryOptions);

		MongoFileSystem.getInstance().openEditor(new MongoObjectFile(project, configuration, navigation));
	}

	public ServerConfiguration getServerConfiguration(MongoTreeNode treeNode) {
		return mongoTreeBuilder.getServerConfiguration(treeNode);
	}

	private void refreshNodeChildren(MongoTreeNode node) {
		if (node != null) {
			switch (node.getType()) {
				case ROOT: {
				}
				case MongoServer: {
					mongoTreeBuilder.refreshNodeChildren(node, false);
					break;
				}
				case MongoDatabase: {
					mongoTreeBuilder.refreshNodeChildren(node, false);
				}
			}
		}
	}

	public void removeNode(MongoTreeNode mongoTreeNode) {
		ServerConfiguration serverConfiguration = getServerConfiguration(mongoTreeNode);
		Object userObject = mongoTreeNode.getUserObject();
		switch (mongoTreeNode.getType()) {
			case MongoServer: {
				mongoTreeBuilder.removeConfiguration((MongoServer) userObject);
				break;
			}
			case MongoDatabase: {
				mongoService.removeDatabase(serverConfiguration, (MongoDatabase) userObject);
				break;
			}
			case MongoCollection: {
				mongoService.removeCollection(serverConfiguration, (MongoCollection) userObject);
				break;
			}
		}

		mongoTreeBuilder.getTreeModel().removeNodeFromParent(mongoTreeNode);

		refreshNodeChildren(mongoTreeNode.getParent());
		notifier.notifyInfo(mongoTreeNode.getType() + " " + mongoTreeNode + " removed");
	}

	private void expandAll() {
		if (mongoTreeBuilder.getRootNode().getChildCount() > 0)
			mongoTreeBuilder.getRootNode()
			                .getChildren()
			                .forEach(c -> mongoTree.expandPath(new TreePath(mongoTreeBuilder.getTreeModel()
			                                                                                .getPathToRoot(c))));
	}

	private void collapseAll() {
		if (mongoTreeBuilder.getRootNode().getChildCount() > 0)
			mongoTreeBuilder.getRootNode()
			                .getChildren()
			                .forEach(c -> mongoTree.collapsePath(new TreePath(mongoTreeBuilder.getTreeModel()
			                                                                                  .getPathToRoot(c))));
	}

	public JPanel getContent() {
		return rootPanel;
	}

	public MongoService getMongoService() {
		return mongoService;
	}

	@Override
	public void dispose() {

	}

	private class Actions {
		final TreeExpander treeExpander = new TreeExpander() {
			@Override
			public void expandAll() {
				MongoExplorerPanel.this.expandAll();
			}

			@Override
			public boolean canExpand() {
				return !getServerConfigurations().isEmpty();
			}

			@Override
			public void collapseAll() {
				MongoExplorerPanel.this.collapseAll();
			}

			@Override
			public boolean canCollapse() {
				return !getServerConfigurations().isEmpty();
			}
		};

		public RefreshServerAction refreshServerAction = new RefreshServerAction(MongoExplorerPanel.this);

		public AddServerAction addServerAction = new AddServerAction(MongoExplorerPanel.this);

		public DuplicateServerAction duplicateServerAction = new DuplicateServerAction(MongoExplorerPanel.this);

		public EditServerAction editServerAction = new EditServerAction(MongoExplorerPanel.this);

		public DeleteAction deleteAction = new DeleteAction(MongoExplorerPanel.this);

		public ViewCollectionValuesAction viewCollectionValuesAction =
				new ViewCollectionValuesAction(MongoExplorerPanel.this);

		public OpenPluginSettingsAction openPluginSettingsAction = new OpenPluginSettingsAction();

		public MongoConsoleAction mongoConsoleAction = new MongoConsoleAction(MongoExplorerPanel.this);

		CommonActionsManager actionsManager = CommonActionsManager.getInstance();

		public final AnAction expandAllAction = actionsManager.createExpandAllAction(treeExpander, rootPanel);

		public final AnAction collapseAllAction = actionsManager.createCollapseAllAction(treeExpander, rootPanel);
	}

}
