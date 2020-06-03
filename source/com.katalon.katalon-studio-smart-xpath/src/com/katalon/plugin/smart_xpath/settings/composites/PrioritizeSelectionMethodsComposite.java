package com.katalon.plugin.smart_xpath.settings.composites;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TypedListener;

import com.kms.katalon.core.testobject.SelectorMethod;
import com.katalon.plugin.smart_xpath.constant.SmartXPathMessageConstants;
import com.kms.katalon.composer.components.impl.util.ControlUtils;
import com.kms.katalon.composer.components.util.ColorUtil;
import com.kms.katalon.composer.resources.constants.IImageKeys;
import com.kms.katalon.composer.resources.image.ImageManager;
import com.kms.katalon.execution.webui.setting.WebUiExecutionSettingStore;
import com.kms.katalon.util.collections.Pair;

public class PrioritizeSelectionMethodsComposite extends Composite {

	private static final String GRP_LBL_PRIORITIZE_SELECTION_METHODS_FOR_SELF_HEALING_EXECUTION = SmartXPathMessageConstants.GRP_LBL_PRIORITIZE_SELECTION_METHODS_FOR_SELF_HEALING_EXECUTION;

	private static final String BUTTON_MOVE_UP_PRIORITIZE_SELF_HEALING_EXECUTION_ORDER = SmartXPathMessageConstants.BUTTON_MOVE_UP_PRIORITIZE_SELF_HEALING_EXECUTION_ORDER;

	private static final String BUTTON_MOVE_DOWN_PRIORITIZE_SELF_HEALING_EXECUTION_ORDER = SmartXPathMessageConstants.BUTTON_MOVE_DOWN_PRIORITIZE_SELF_HEALING_EXECUTION_ORDER;

	private static final String COLUMN_SELECTION_METHOD = SmartXPathMessageConstants.COLUMN_SELECTION_METHOD;

	private static final String COLUMN_DETECT_OBJECT_BY = SmartXPathMessageConstants.COLUMN_DETECT_OBJECT_BY;

	private TableViewer tvPrioritizeSelectionMethods;

	private Table tPrioritizeSelectionMethods;

	private TableViewerColumn cvPrioritizeSelectionMethodColumn;

	private TableColumn cPrioritizeSelectionMethodColumn;

	private TableViewerColumn cvMethodsSelected;

	private TableColumn cMethodsSelected;

	private List<Pair<SelectorMethod, Boolean>> methodsPriorityOrder;

	public PrioritizeSelectionMethodsComposite(Composite parent, int style, WebUiExecutionSettingStore preferenceStore) {
		super(parent, style);
		createContents(parent);
	}
	
	public void setInput(List<Pair<SelectorMethod, Boolean>> methodsPriorityOrder) {
		this.methodsPriorityOrder = methodsPriorityOrder;
		tvPrioritizeSelectionMethods.setInput(this.methodsPriorityOrder);
	}

	private void createContents(Composite parent) {
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		GridLayout glSelectionMethodComp = new GridLayout(1, false);
		glSelectionMethodComp.marginHeight = 0;
		glSelectionMethodComp.marginWidth = 0;
		this.setLayout(glSelectionMethodComp);

		createMethodsPriorityOrderComposite();
	}

	private void createMethodsPriorityOrderComposite() {
		Group prioritizeGroup = new Group(this, SWT.NONE);
		prioritizeGroup.setLayout(new GridLayout());
		prioritizeGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		prioritizeGroup.setText(GRP_LBL_PRIORITIZE_SELECTION_METHODS_FOR_SELF_HEALING_EXECUTION);

		createPrioritizeOrderToolbar(prioritizeGroup);
		createPrioritizeTable(prioritizeGroup);
	}

	private void createPrioritizeOrderToolbar(Composite parent) {
		Composite compositeToolbar = new Composite(parent, SWT.NONE);
		compositeToolbar.setLayout(new FillLayout(SWT.HORIZONTAL));
		compositeToolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		ToolBar toolBar = new ToolBar(compositeToolbar, SWT.FLAT | SWT.RIGHT);
		toolBar.setForeground(ColorUtil.getToolBarForegroundColor());

		ToolItem tltmUp = new ToolItem(toolBar, SWT.NONE);
		tltmUp.setText(BUTTON_MOVE_UP_PRIORITIZE_SELF_HEALING_EXECUTION_ORDER);
		tltmUp.setImage(ImageManager.getImage(IImageKeys.MOVE_UP_16));
		tltmUp.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int selectedIndex = tPrioritizeSelectionMethods.getSelectionIndex();
				if (selectedIndex > 0 && selectedIndex < methodsPriorityOrder.size()) {
					Pair<SelectorMethod, Boolean> method = methodsPriorityOrder.get(selectedIndex);
					methodsPriorityOrder.remove(selectedIndex);
					methodsPriorityOrder.add(selectedIndex - 1, method);
					tvPrioritizeSelectionMethods.setSelection(new StructuredSelection(method));
					tvPrioritizeSelectionMethods.refresh();

					handleSelectionChange(null);
				}
			}
		});

		ToolItem tltmDown = new ToolItem(toolBar, SWT.NONE);
		tltmDown.setText(BUTTON_MOVE_DOWN_PRIORITIZE_SELF_HEALING_EXECUTION_ORDER);
		tltmDown.setImage(ImageManager.getImage(IImageKeys.MOVE_DOWN_16));
		tltmDown.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int selectedIndex = tPrioritizeSelectionMethods.getSelectionIndex();
				if (selectedIndex >= 0 && selectedIndex < (methodsPriorityOrder.size() - 1)) {
					Pair<SelectorMethod, Boolean> method = methodsPriorityOrder.get(selectedIndex);
					methodsPriorityOrder.remove(selectedIndex);
					methodsPriorityOrder.add(selectedIndex + 1, method);
					tvPrioritizeSelectionMethods.setSelection(new StructuredSelection(method));
					tvPrioritizeSelectionMethods.refresh();

					handleSelectionChange(null);
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void createPrioritizeTable(Composite parent) {
		Composite tableSelectionPriorityOrderComposite = new Composite(parent, SWT.NONE);
		GridData ldTableComposite = new GridData(SWT.FILL, SWT.FILL, true, true);
		ldTableComposite.minimumHeight = 70;
		tableSelectionPriorityOrderComposite.setLayoutData(ldTableComposite);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableSelectionPriorityOrderComposite.setLayout(tableColumnLayout);
		tvPrioritizeSelectionMethods = new TableViewer(tableSelectionPriorityOrderComposite,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		tvPrioritizeSelectionMethods.setContentProvider(ArrayContentProvider.getInstance());

		tvPrioritizeSelectionMethods.addDragSupport(DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() },
				new DragSourceAdapter() {

					@Override
					public void dragSetData(DragSourceEvent event) {
						StructuredSelection selection = (StructuredSelection) tvPrioritizeSelectionMethods
								.getSelection();
						Pair<SelectorMethod, Boolean> method = ((Pair<SelectorMethod, Boolean>) selection.getFirstElement());
						event.data = String.valueOf(methodsPriorityOrder.indexOf(method));
					}
				});
		tvPrioritizeSelectionMethods.addDropSupport(DND.DROP_MOVE, new Transfer[] { TextTransfer.getInstance() },
				new DropTargetAdapter() {

					@Override
					public void drop(DropTargetEvent event) {
						Pair<SelectorMethod, Boolean> item = (Pair<SelectorMethod, Boolean>) ((TableItem) event.item).getData();
						int newIndex = methodsPriorityOrder.indexOf(item);
						String index = (String) event.data;
						if (index != null && newIndex >= 0) {
							int indexVal = Integer.parseInt(index);
							Pair<SelectorMethod, Boolean> method = methodsPriorityOrder.get(indexVal);
							methodsPriorityOrder.remove(indexVal);
							methodsPriorityOrder.add(newIndex, method);
							tvPrioritizeSelectionMethods.setSelection(new StructuredSelection(method));
							tvPrioritizeSelectionMethods.refresh();
						}
						handleSelectionChange(event);
					}
				});
		tPrioritizeSelectionMethods = tvPrioritizeSelectionMethods.getTable();
		tPrioritizeSelectionMethods.setHeaderVisible(true);
		tPrioritizeSelectionMethods
				.setLinesVisible(ControlUtils.shouldLineVisble(tPrioritizeSelectionMethods.getDisplay()));

		cvPrioritizeSelectionMethodColumn = new TableViewerColumn(tvPrioritizeSelectionMethods, SWT.LEFT);
		cPrioritizeSelectionMethodColumn = cvPrioritizeSelectionMethodColumn.getColumn();
		cPrioritizeSelectionMethodColumn.setText(COLUMN_SELECTION_METHOD);
		cvPrioritizeSelectionMethodColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((Pair<SelectorMethod, Boolean>) element).getLeft().getName();
			}
		});

		cvMethodsSelected = new TableViewerColumn(tvPrioritizeSelectionMethods, SWT.CENTER);
		cMethodsSelected = cvMethodsSelected.getColumn();
		cMethodsSelected.setText(COLUMN_DETECT_OBJECT_BY);

		cvMethodsSelected.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Object property = cell.getElement();
				if (!(property instanceof Pair)) {
					return;
				}
				Boolean isSelected = ((Pair<String, Boolean>) property).getRight();
				((TableItem) cell.getViewerRow().getItem()).setChecked(isSelected);
				tPrioritizeSelectionMethods.redraw();
			}
		});

		cvMethodsSelected.setEditingSupport(new EditingSupport(cvMethodsSelected.getViewer()) {

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor();
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				return ((Pair<String, Boolean>) element).getRight();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((Pair<String, Boolean>) element).setRight((boolean) value);
				tvPrioritizeSelectionMethods.update(element, null);
				
				handleSelectionChange(null);
			}
		});

		tableColumnLayout.setColumnData(cPrioritizeSelectionMethodColumn, new ColumnWeightData(80, 100));
		tableColumnLayout.setColumnData(cMethodsSelected, new ColumnWeightData(20, 100));

		tPrioritizeSelectionMethods.addListener(SWT.PaintItem, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (event.index == 1) {
					Pair<String, Boolean> data = (Pair<String, Boolean>) ((TableItem) event.item).getData();
					Image tmpImage = getCheckboxSymbol(data.getRight());
					int tmpWidth = 0;
					int tmpHeight = 0;
					int tmpX = 0;
					int tmpY = 0;

					tmpWidth = tPrioritizeSelectionMethods.getColumn(event.index).getWidth();
					tmpHeight = ((TableItem) event.item).getBounds().height;

					tmpX = tmpImage.getBounds().width;
					tmpX = (tmpWidth / 2 - tmpX / 2);
					tmpY = tmpImage.getBounds().height;
					tmpY = (tmpHeight / 2 - tmpY / 2);
					if (tmpX <= 0) tmpX = event.x;
					else tmpX += event.x;
					if (tmpY <= 0) tmpY = event.y;
					else tmpY += event.y;
					event.gc.drawImage(tmpImage, tmpX, tmpY);
				}
			}
		});
	}

	protected Image getCheckboxSymbol(boolean isChecked) {
		return isChecked ? ImageManager.getImage(IImageKeys.CHECKBOX_CHECKED_16)
				: ImageManager.getImage(IImageKeys.CHECKBOX_UNCHECKED_16);
	}
	
	public List<Pair<SelectorMethod, Boolean>> getInput() {
		return methodsPriorityOrder;
	}

	public boolean compareInput(List<Pair<SelectorMethod, Boolean>> methodsPriorityOrderBeforeSetting) {
		return methodsPriorityOrder != null && methodsPriorityOrder.equals(methodsPriorityOrderBeforeSetting);
	}

    private void handleSelectionChange(TypedEvent selectionEvent) {
        dispatchSelectionEvent(selectionEvent);
    }

    private void dispatchSelectionEvent(TypedEvent selectionEvent) {
        notifyListeners(SWT.Selection, null);
        notifyListeners(SWT.DefaultSelection, null);
    }

    public void addSelectionListener(SelectionListener listener) {
        checkWidget();
        if (listener == null) {
            return;
        }
        TypedListener typedListener = new TypedListener(listener);
        addListener(SWT.Selection, typedListener);
        addListener(SWT.DefaultSelection, typedListener);
    }
}
