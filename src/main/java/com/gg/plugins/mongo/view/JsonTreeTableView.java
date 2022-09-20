/*
 * Copyright (c) 2022. Geetesh Gupta
 */

package com.gg.plugins.mongo.view;

import com.intellij.ui.treeStructure.treetable.TreeTable;

public class JsonTreeTableView extends TreeTable {
	public JsonTreeTableView() {super(null);}
	//	private static final ColumnInfo KEY = new ColumnInfo("Key") {
	//
	//		public Object valueOf(Object obj) {
	//			JsonTreeNode node = (JsonTreeNode) obj;
	//			return node.getDescriptor();
	//		}
	//
	//		@Override
	//		public Class getColumnClass() {
	//			return TreeTableModel.class;
	//		}
	//
	//		@Override
	//		public boolean isCellEditable(Object o) {
	//			return false;
	//		}
	//	};
	//
	//	private static final ColumnInfo READONLY_VALUE = new ReadOnlyValueColumnInfo();
	//
	//	public static final ColumnInfo[] COLUMNS_FOR_READING = new ColumnInfo[]{KEY, READONLY_VALUE};
	//
	//	private static final ColumnInfo WRITABLE_VALUE = new WritableColumnInfo();
	//
	//	public static final ColumnInfo[] COLUMNS_FOR_WRITING = new ColumnInfo[]{KEY, WRITABLE_VALUE};
	//
	//	private final ColumnInfo[] columns;
	//
	//	public JsonTreeTableView(TreeNode rootNode, ColumnInfo[] columnInfos) {
	//		super(new ListTreeTableModelOnColumns(rootNode, columnInfos));
	//		this.columns = columnInfos;
	//
	//		final TreeTableTree tree = getTree();
	//
	//		tree.setShowsRootHandles(true);
	//		tree.setRootVisible(false);
	//		UIUtil.setLineStyleAngled(tree);
	//		setTreeCellRenderer(new MongoKeyCellRenderer());
	//
	//		new TreeTableSpeedSearch(this, path -> {
	//			final JsonTreeNode node = (JsonTreeNode) path.getLastPathComponent();
	//			MongoNodeDescriptor descriptor = node.getDescriptor();
	//			return descriptor.getKey();
	//		});
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	@Override
	//	public TableCellRenderer getCellRenderer(int row, int column) {
	//		TreePath treePath = getTree().getPathForRow(row);
	//		if (treePath == null)
	//			return super.getCellRenderer(row, column);
	//
	//		JsonTreeNode node = (JsonTreeNode) treePath.getLastPathComponent();
	//
	//		TableCellRenderer renderer = this.columns[column].getRenderer(node);
	//		return renderer == null ? super.getCellRenderer(row, column) : renderer;
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	@Override
	//	public TableCellEditor getCellEditor(int row, int column) {
	//		TreePath treePath = getTree().getPathForRow(row);
	//		if (treePath == null)
	//			return super.getCellEditor(row, column);
	//
	//		JsonTreeNode node = (JsonTreeNode) treePath.getLastPathComponent();
	//		TableCellEditor editor = columns[column].getEditor(node);
	//		return editor == null ? super.getCellEditor(row, column) : editor;
	//	}
	//
	//	private static class ReadOnlyValueColumnInfo extends ColumnInfo<JsonTreeNode, MongoNodeDescriptor> {
	//		private final TableCellRenderer myRenderer = new MongoValueCellRenderer();
	//
	//		ReadOnlyValueColumnInfo() {
	//			super("Value");
	//		}
	//
	//		public MongoNodeDescriptor valueOf(JsonTreeNode treeNode) {
	//			return treeNode.getDescriptor();
	//		}
	//
	//		@Override
	//		public boolean isCellEditable(JsonTreeNode o) {
	//			return false;
	//		}
	//
	//		@Override
	//		public TableCellRenderer getRenderer(JsonTreeNode o) {
	//			return myRenderer;
	//		}
	//	}
	//
	//	private static class WritableColumnInfo extends ColumnInfo<JsonTreeNode, Object> {
	//
	//		private final TableCellRenderer myRenderer = new MongoValueCellRenderer();
	//
	//		private final TableCellEditor defaultEditor = new MongoValueCellEditor();
	//
	//		WritableColumnInfo() {
	//			super("Value");
	//		}
	//
	//		public Object valueOf(JsonTreeNode treeNode) {
	//			return treeNode.getDescriptor();
	//
	//		}
	//
	//		@Override
	//		public boolean isCellEditable(JsonTreeNode treeNode) {
	//			Object value = treeNode.getDescriptor().getValue();
	//			if (value instanceof Document || value instanceof List) {
	//				return false;
	//			}
	//
	//			return !(value instanceof ObjectId);
	//		}
	//
	//		@Override
	//		public void setValue(JsonTreeNode treeNode, Object value) {
	//			treeNode.getDescriptor().setValue(value);
	//		}
	//
	//		@Override
	//		public TableCellRenderer getRenderer(JsonTreeNode o) {
	//			return myRenderer;
	//		}
	//
	//		@Nullable
	//		@Override
	//		public TableCellEditor getEditor(final JsonTreeNode treeNode) {
	//			Object value = treeNode.getDescriptor().getValue();
	//			if (value instanceof Date) {
	//				return buildDateCellEditor(treeNode);
	//			}
	//			return defaultEditor;
	//		}
	//
	//		private static MongoDatePickerCellEditor buildDateCellEditor(final JsonTreeNode treeNode) {
	//			final MongoDatePickerCellEditor dateEditor = new MongoDatePickerCellEditor();
	//
	//			//  Note from dev: Quite ugly because when clicking on the button to open popup calendar,
	//			stopCellEdition
	//			//  is invoked.
	//			//                 From that point, impossible to set the selected data in the node description
	//			dateEditor.addActionListener(actionEvent -> treeNode.getDescriptor()
	//			                                                    .setValue(dateEditor.getCellEditorValue()));
	//			return dateEditor;
	//		}
	//	}
}