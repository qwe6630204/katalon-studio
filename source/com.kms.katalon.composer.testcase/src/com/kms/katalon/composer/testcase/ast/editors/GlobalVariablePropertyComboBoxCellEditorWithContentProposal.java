package com.kms.katalon.composer.testcase.ast.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.kms.katalon.composer.components.event.EventBrokerSingleton;
import com.kms.katalon.composer.components.impl.event.EventServiceAdapter;
import com.kms.katalon.composer.testcase.editors.ComboBoxCellEditorWithContentProposal;
import com.kms.katalon.composer.testcase.groovy.ast.expressions.PropertyExpressionWrapper;
import com.kms.katalon.composer.testcase.groovy.ast.statements.StatementWrapper;
import com.kms.katalon.composer.testcase.util.AstValueUtil;
import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.controller.GlobalVariableController;
import com.kms.katalon.entity.global.ExecutionProfileEntity;
import com.kms.katalon.entity.global.GlobalVariableEntity;
import com.kms.katalon.execution.util.ExecutionProfileStore;

public class GlobalVariablePropertyComboBoxCellEditorWithContentProposal extends ComboBoxCellEditorWithContentProposal implements EventHandler {

    private Object[] items;
    
    private PropertyExpressionWrapper parentWrapper;
    
    private static String GLOBAL_VARIABLE_CLASS_ALIAS_Name = "GlobalVariable";
    
    public GlobalVariablePropertyComboBoxCellEditorWithContentProposal(Composite parent, PropertyExpressionWrapper parentWrapper, Object[] items, Object[] displayedItems, String[] toolTips) {
        super(parent, displayedItems, toolTips);
        this.items = items;
        this.parentWrapper = parentWrapper;
        registerEvent();
    }
    

    private void loadData() {
        List<GlobalVariableEntity> variables = ExecutionProfileStore.getInstance()
                .getSelectedProfile().getGlobalVariableEntities();
        this.items = variables.toArray(new GlobalVariableEntity[variables.size()]);
    }


    private void registerEvent() {
        EventBrokerSingleton.getInstance().getEventBroker().subscribe(EventConstants.PROFILE_SELECTED_PROIFE_CHANGED, this);
        
    }

    @Override
    protected Object doGetValue() {
        int selectedIndex = (int) super.doGetValue();
        Object selectedItem = items[selectedIndex];
        String variableName = ((GlobalVariableEntity)selectedItem).getName();
        return new PropertyExpressionWrapper(GLOBAL_VARIABLE_CLASS_ALIAS_Name, variableName, parentWrapper);
    }
    
    @Override
    protected void doSetValue(Object value) {
        if(!(value instanceof PropertyExpressionWrapper)) {
            super.doSetValue(value);
            return;
        }
        PropertyExpressionWrapper variable = (PropertyExpressionWrapper) value;
        String variableName = variable.getPropertyAsString();
        for(int index =0; index < items.length; index++) {
            if(getVariableName(items[index]).equals(variableName)) {
                super.doSetValue(index);
                return;
            }
        }
    }
    
    public String getVariableName(Object selectedItem) {
        if (selectedItem instanceof String) {
            return (String) selectedItem;
        }
        if(selectedItem instanceof GlobalVariableEntity) {
            return ((GlobalVariableEntity) selectedItem).getName();
        }
        return null;
    }

    @Override
    public void handleEvent(Event event) {
        if (EventConstants.PROFILE_SELECTED_PROIFE_CHANGED.equals(event.getTopic())) {
            loadData();
        }
    }

}
