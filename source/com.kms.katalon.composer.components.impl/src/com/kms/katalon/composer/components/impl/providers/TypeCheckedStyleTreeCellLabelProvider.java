package com.kms.katalon.composer.components.impl.providers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;

import com.kms.katalon.composer.components.impl.control.CTreeViewer;

public abstract class TypeCheckedStyleTreeCellLabelProvider<T> extends TypeCheckedStyleCellLabelProvider<T> {

    public TypeCheckedStyleTreeCellLabelProvider(int columnIndex) {
        super(columnIndex);
    }

    @Override
    public void initialize(ColumnViewer viewer, ViewerColumn column) {
        super.initialize(viewer, column);
    }

    protected ViewerCell getOwnedViewerCell(Event event) {
        CTreeViewer treeViewer = (CTreeViewer) getViewer();
        return treeViewer.getViewerRowFromItem(event.item).getCell(columnIndex);
    }

    @Override
    protected int getSpace() {
        return 0;
    }

    @Override
    protected int getLeftMargin() {
        return 0;
    }
    
    @Override
    protected int getRightMargin() {
        return 0;
    }
    
    @Override
    protected boolean canNotDrawSafely(Object element) {
        return super.canNotDrawSafely(element) || !(getViewer() instanceof CTreeViewer);
    }

    @Override
    protected Rectangle getTextBounds(Rectangle originalBounds) {
        return new Rectangle(originalBounds.x, originalBounds.y, originalBounds.width + 1, originalBounds.height);
    }
}
