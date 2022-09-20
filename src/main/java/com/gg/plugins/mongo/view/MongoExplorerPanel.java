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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		mongoTreeBuilder = new MongoTreeBuilder(mongoTree, this);
		mongoTree.setModel(mongoTreeBuilder.getTreeModel());

		setLayout(new BorderLayout());
		treePanel.add(new JBScrollPane(mongoTree), BorderLayout.CENTER);

		Splitter splitter = new Splitter(true, 0.6f);
		splitter.setFirstComponent(treePanel);

		containerPanel.add(splitter, BorderLayout.CENTER);

		add(rootPanel, BorderLayout.CENTER);

		toolBarPanel.setLayout(new BorderLayout());

		loadAllServerConfigurations();

		installActions();
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
		tree.setRootVisible(true);
		tree.expandRow(0);

		new TreeSpeedSearch(tree, treePath -> {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
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

	private void loadAllServerConfigurations() {
		this.mongoService.cleanUpServers();

		List<ServerConfiguration> serverConfigurations = getServerConfigurations();
		for (ServerConfiguration serverConfiguration : serverConfigurations) {
			addConfiguration(serverConfiguration);
		}
	}

	private void installActions() {

		final TreeExpander treeExpander = new TreeExpander() {
			@Override
			public void expandAll() {
				//				MongoExplorerPanel.this.expandAll();
			}

			@Override
			public boolean canExpand() {
				return !getServerConfigurations().isEmpty();
			}

			@Override
			public void collapseAll() {
				//				MongoExplorerPanel.this.collapseAll();
			}

			@Override
			public boolean canCollapse() {
				return !getServerConfigurations().isEmpty();
			}
		};

		CommonActionsManager actionsManager = CommonActionsManager.getInstance();

		final AnAction expandAllAction = actionsManager.createExpandAllAction(treeExpander, rootPanel);
		final AnAction collapseAllAction = actionsManager.createCollapseAllAction(treeExpander, rootPanel);

		Disposer.register(this, () -> {
			collapseAllAction.unregisterCustomShortcutSet(rootPanel);
			expandAllAction.unregisterCustomShortcutSet(rootPanel);
		});

		DefaultActionGroup actionGroup = new DefaultActionGroup("MongoExplorerGroup", false);
		RefreshServerAction refreshServerAction = new RefreshServerAction(this);
		AddServerAction addServerAction = new AddServerAction(this);
		DuplicateServerAction duplicateServerAction = new DuplicateServerAction(this);
		if (ApplicationManager.getApplication() != null) {
			actionGroup.add(addServerAction);
			actionGroup.add(duplicateServerAction);
			actionGroup.addSeparator();
			actionGroup.add(refreshServerAction);
			actionGroup.add(new MongoConsoleAction(this));
			actionGroup.add(expandAllAction);
			actionGroup.add(collapseAllAction);
			actionGroup.addSeparator();
			actionGroup.add(new OpenPluginSettingsAction());
		}

		GuiUtils.installActionGroupInToolBar(actionGroup,
				toolBarPanel,
				ActionManager.getInstance(),
				"MongoExplorerActions",
				true);

		DefaultActionGroup actionPopupGroup = new DefaultActionGroup("MongoExplorerPopupGroup", true);
		if (ApplicationManager.getApplication() != null) {
			actionPopupGroup.add(refreshServerAction);
			actionPopupGroup.add(new EditServerAction(this));
			actionPopupGroup.add(duplicateServerAction);
			actionPopupGroup.add(new DeleteAction(this));
			actionPopupGroup.addSeparator();
			actionPopupGroup.add(new ViewCollectionValuesAction(this));
		}

		PopupHandler.installPopupMenu(mongoTree, actionPopupGroup, "POPUP");

		new DoubleClickListener() {
			@Override
			protected boolean onDoubleClick(@NotNull MouseEvent event) {
				if (!(event.getSource() instanceof JTree)) {
					return false;
				}

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) mongoTree.getLastSelectedPathComponent();
				if (node != null) {
					Object object = node.getUserObject();
					if (object instanceof MongoServer) {
						//					MongoServer selectedMongoServer = getSelectedServer();
						//					if (selectedMongoServer != null) {
						openServer((MongoServer) object);
						return true;
						//					}
					} else if (object instanceof MongoDatabase) {
						return true;
					} else if (object instanceof MongoCollection) {
						loadSelectedCollectionValues((MongoCollection) object);
						return true;
					}
				}
				return false;
			}
		}.installOn(mongoTree);

	}

	private List<ServerConfiguration> getServerConfigurations() {
		return MongoConfiguration.getInstance(project).getServerConfigurations();
	}

	public void addConfiguration(ServerConfiguration serverConfiguration) {
		MongoServer mongoServer = mongoTreeBuilder.addConfiguration(serverConfiguration);
		mongoService.registerServer(mongoServer);
	}

	public void openServer(final MongoServer mongoServer) {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, "Connecting to " + mongoServer.getLabel()) {

			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				mongoTree.setPaintBusy(true);
				ApplicationManager.getApplication().invokeLater(() -> {
					try {
						List<MongoDatabase> mongoDatabases =
								mongoService.loadDatabases(mongoServer, mongoServer.getConfiguration());
						if (mongoDatabases.isEmpty()) {
							return;
						}
						mongoServer.setDatabases(mongoDatabases);
						mongoTreeBuilder.openServer(mongoServer);
						//						                                                .makeVisible
						//						                                                (mongoServer,
						//						                                                mongoTree, v -> {}
						//						                .onSuccess(e -> mongoTreeBuilder.getTreeModel()
						//						                                                .makeVisible
						//						                                                (mongoServer,
						//						                                                mongoTree, v -> {}));

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

	public void loadSelectedCollectionValues(MongoCollection mongoCollection) {
		MongoServer parentServer = mongoCollection.getParentDatabase().getParentServer();
		ServerConfiguration configuration = parentServer.getConfiguration();

		Navigation navigation = new Navigation();
		MongoQueryOptions queryOptions = new MongoQueryOptions();
		queryOptions.setResultLimit(configuration.getDefaultRowLimit());
		navigation.addNewWayPoint(mongoCollection, queryOptions);

		MongoFileSystem.getInstance().openEditor(new MongoObjectFile(project, configuration, navigation));
	}

	public MongoServer getSelectedServer() {
		Set<MongoServer> selectedElements =
				Arrays.stream(mongoTree.getSelectedNodes(DefaultMutableTreeNode.class, null))
				      .map(n -> (MongoServer) n.getUserObject())
				      .collect(Collectors.toSet());
		if (selectedElements.isEmpty()) {
			return null;
		}
		return selectedElements.iterator().next();
	}

	public MongoCollection getSelectedCollection() {

		Set<MongoCollection> selectedElements =
				Arrays.stream(mongoTree.getSelectedNodes(DefaultMutableTreeNode.class, null))
				      .map(n -> (MongoCollection) n.getUserObject())
				      .collect(Collectors.toSet());
		if (selectedElements.isEmpty()) {
			return null;
		}
		return selectedElements.iterator().next();
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

}
