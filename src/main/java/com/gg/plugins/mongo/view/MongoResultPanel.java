/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view;

import com.gg.plugins.mongo.model.MongoCollectionResult;
import com.gg.plugins.mongo.model.NbPerPage;
import com.gg.plugins.mongo.model.Pagination;
import com.gg.plugins.mongo.service.Notifier;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBCardLayout;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;
import com.intellij.util.ui.tree.TreeUtil;
import com.mongodb.DBRef;
import org.bson.Document;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MongoResultPanel extends JPanel implements Disposable {

	private final Project project;

	private final MongoPanel.MongoDocumentOperations mongoDocumentOperations;

	private final Notifier notifier;

	private final JPanel resultTreePanel;

	private final ActionCallback actionCallback;

	JsonTreeTableView resultTreeTableView;

	private JPanel mainPanel;

	private JPanel containerPanel;

	private ViewMode currentViewMode = ViewMode.TREE;

	public MongoResultPanel(Project project,
			MongoPanel.MongoDocumentOperations mongoDocumentOperations,
			Notifier notifier) {
		this.project = project;
		this.mongoDocumentOperations = mongoDocumentOperations;
		this.notifier = notifier;
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);

		resultTreePanel = new JPanel(new BorderLayout());

		containerPanel.setLayout(new JBCardLayout());
		containerPanel.add(resultTreePanel);

		actionCallback = new ActionCallback() {
			public void onOperationSuccess(String shortMessage, String detailedMessage) {
				notifier.notifyInfo(detailedMessage);
			}

			@Override
			public void onOperationFailure(Exception exception) {
				notifier.notifyError(exception.getMessage());
			}
		};

		Disposer.register(project, this);
	}

	private static List<Document> extractDocuments(Pagination pagination, List<Document> documents) {
		if (NbPerPage.ALL.equals(pagination.getNbPerPage())) {
			return documents;
		}
		if (pagination.getNbDocumentsPerPage() >= documents.size()) {
			return documents;
		}

		int startIndex = pagination.getStartIndex();
		int endIndex = startIndex + pagination.getNbDocumentsPerPage();

		return IntStream.range(startIndex, endIndex)
		                .mapToObj(documents::get)
		                .collect(Collectors.toCollection(LinkedList::new));
	}

	void updateResultView(MongoCollectionResult mongoCollectionResult, Pagination pagination) {
		if (ViewMode.TREE.equals(currentViewMode)) {
			updateResultTreeTable(mongoCollectionResult, pagination);
		} else {
			updateResultTable(mongoCollectionResult);
		}
	}

	private void updateResultTreeTable(MongoCollectionResult mongoCollectionResult, Pagination pagination) {
		//		resultTreeTableView =
		//				new JsonTreeTableView(JsonTreeUtils.buildJsonTree(mongoCollectionResult.getCollectionName(),
		//						extractDocuments(pagination, mongoCollectionResult.getDocuments()),
		//						pagination.getStartIndex()), JsonTreeTableView.COLUMNS_FOR_READING);
		//
		//		resultTreeTableView.setName("resultTreeTable");
		//
		//		displayResult(resultTreeTableView);
		//
		//		UIUtil.invokeAndWaitIfNeeded((Runnable) () -> TreeUtil.expand(resultTreeTableView.getTree(), 2));
	}

	private void updateResultTable(MongoCollectionResult mongoCollectionResult) {
		//		displayResult(new JsonTableView(JsonTableUtils.buildJsonTable(mongoCollectionResult)));
	}

	private void displayResult(JComponent tableView) {
		resultTreePanel.invalidate();
		resultTreePanel.removeAll();
		resultTreePanel.add(new JBScrollPane(tableView));
		resultTreePanel.validate();
	}

	public void editSelectedMongoDocument() {
		Document mongoDocument = getSelectedMongoDocument();
		if (mongoDocument == null) {
		}

		//		MongoEditionDialog.create(project, mongoDocumentOperations, actionCallback).initDocument
		//		(mongoDocument).show();
	}

	private Document getSelectedMongoDocument() {
		TreeTableTree tree = resultTreeTableView.getTree();
		//		JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();
		//		if (treeNode == null) {
		//			return null;
		//		}
		//
		//		MongoNodeDescriptor descriptor = treeNode.getDescriptor();
		//		if (descriptor instanceof MongoKeyValueDescriptor) {
		//			MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
		//			if (StringUtils.equals(keyValueDescriptor.getKey(), "_id")) {
		//				return mongoDocumentOperations.getMongoDocument(keyValueDescriptor.getValue());
		//			}
		//		}

		return null;
	}

	public void addMongoDocument() {
		//		MongoEditionDialog.create(project, mongoDocumentOperations, actionCallback).initDocument(null).show();
	}

	//	public boolean isSelectedNodeId() {
	//		return getObjectIdDescriptorFromSelectedDocument() != null;
	//	}

	//	private MongoKeyValueDescriptor getObjectIdDescriptorFromSelectedDocument() {
	//		if (resultTreeTableView == null) {
	//			return null;
	//		}
	//		TreeTableTree tree = resultTreeTableView.getTree();
	//		JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();
	//		if (treeNode == null) {
	//			return null;
	//		}
	//
	//		MongoNodeDescriptor descriptor = treeNode.getDescriptor();
	//		if (!(descriptor instanceof MongoKeyValueDescriptor)) {
	//			return null;
	//		}
	//		MongoKeyValueDescriptor keyValueDescriptor = (MongoKeyValueDescriptor) descriptor;
	//		if (!"_id".equals(keyValueDescriptor.getKey()) && !(keyValueDescriptor.getValue() instanceof ObjectId)) {
	//			return null;
	//		}
	//
	//		return keyValueDescriptor;
	//	}
	//
	//	public boolean isSelectedDBRef() {
	//		if (resultTreeTableView == null) {
	//			return false;
	//		}
	//
	//		TreeTableTree tree = resultTreeTableView.getTree();
	//		JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();
	//		if (treeNode == null) {
	//			return false;
	//		}
	//
	//		MongoNodeDescriptor descriptor = treeNode.getDescriptor();
	//		if (descriptor instanceof MongoKeyValueDescriptor) {
	//			if (descriptor.getValue() instanceof DBRef) {
	//				return true;
	//			} else {
	//				JsonTreeNode parentNode = (JsonTreeNode) treeNode.getParent();
	//				return parentNode.getDescriptor().getValue() instanceof DBRef;
	//			}
	//		}
	//
	//		return false;
	//	}

	void expandAll() {
		TreeUtil.expandAll(resultTreeTableView.getTree());
	}

	void collapseAll() {
		TreeTableTree tree = resultTreeTableView.getTree();
		TreeUtil.collapseAll(tree, 1);
	}

	//	public String getStringifiedResult() {
	//		JsonTreeNode rootNode = (JsonTreeNode) resultTreeTableView.getTree().getModel().getRoot();
	//		return stringifyResult(rootNode);
	//	}

	//	private String stringifyResult(DefaultMutableTreeNode selectedResultNode) {
	//		return IntStream.range(0, selectedResultNode.getChildCount())
	//		                .mapToObj(i -> getDescriptor(i, selectedResultNode).pretty())
	//		                .collect(Collectors.joining(",", "[", "]"));
	//	}

	//	private static MongoNodeDescriptor getDescriptor(int i, DefaultMutableTreeNode parentNode) {
	//		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parentNode.getChildAt(i);
	//		return (MongoNodeDescriptor) childNode.getUserObject();
	//	}
	//
	//	public String getSelectedNodeStringifiedValue() {
	//		JsonTreeNode lastSelectedResultNode = getSelectedNode();
	//		if (lastSelectedResultNode == null) {
	//			return null;
	//		}
	//		MongoNodeDescriptor userObject = lastSelectedResultNode.getDescriptor();
	//		return userObject.pretty();
	//	}
	//
	//	public JsonTreeNode getSelectedNode() {
	//		return (JsonTreeNode) resultTreeTableView.getTree().getLastSelectedPathComponent();
	//	}
	//
	//	public DBRef getSelectedDBRef() {
	//		TreeTableTree tree = resultTreeTableView.getTree();
	//		JsonTreeNode treeNode = (JsonTreeNode) tree.getLastSelectedPathComponent();
	//
	//		MongoNodeDescriptor descriptor = treeNode.getDescriptor();
	//		DBRef selectedDBRef = null;
	//		if (descriptor instanceof MongoKeyValueDescriptor) {
	//			if (descriptor.getValue() instanceof DBRef) {
	//				selectedDBRef = (DBRef) descriptor.getValue();
	//			} else {
	//				JsonTreeNode parentNode = (JsonTreeNode) treeNode.getParent();
	//				MongoNodeDescriptor parentDescriptor = parentNode.getDescriptor();
	//				if (parentDescriptor.getValue() instanceof DBRef) {
	//					selectedDBRef = (DBRef) parentDescriptor.getValue();
	//				}
	//			}
	//		}
	//
	//		return selectedDBRef;
	//	}

	@Override
	public void dispose() {
		resultTreeTableView = null;
	}

	ViewMode getCurrentViewMode() {
		return currentViewMode;
	}

	void setCurrentViewMode(ViewMode viewMode) {
		this.currentViewMode = viewMode;
	}

	public Document getReferencedDocument(DBRef selectedDBRef) {
		return mongoDocumentOperations.getReferenceDocument(selectedDBRef.getCollectionName(),
				selectedDBRef.getId(),
				selectedDBRef.getDatabaseName());
	}
	//
	//	public void deleteSelectedMongoDocument() {
	//		MongoKeyValueDescriptor descriptor = getObjectIdDescriptorFromSelectedDocument();
	//		if (descriptor == null) {
	//			return;
	//		}
	//
	//		ObjectId objectId = ((ObjectId) descriptor.getValue());
	//		mongoDocumentOperations.deleteMongoDocument(objectId);
	//		notifier.notifyInfo("Document with _id=" + objectId.toString() + " deleted.");
	//	}

	public enum ViewMode {
		TREE, TABLE
	}

	public interface ActionCallback {

		void onOperationSuccess(String label, String message);

		void onOperationFailure(Exception exception);
	}

}
