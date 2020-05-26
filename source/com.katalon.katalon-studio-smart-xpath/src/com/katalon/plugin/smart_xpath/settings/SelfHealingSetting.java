package com.katalon.plugin.smart_xpath.settings;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.reflect.TypeToken;
import com.katalon.platform.api.exception.InvalidDataTypeFormatException;
import com.katalon.platform.api.exception.ResourceException;
import com.katalon.platform.api.model.Entity;
import com.katalon.platform.api.preference.PluginPreference;
import com.katalon.platform.api.service.ApplicationManager;
import com.katalon.plugin.smart_xpath.constant.SmartXPathMessageConstants;
import com.kms.katalon.constants.IdConstants;
import com.kms.katalon.core.util.internal.JsonUtil;
import com.kms.katalon.util.collections.Pair;

public class SelfHealingSetting {

	public final String SELF_HEALING_ENABLE = SmartXPathMessageConstants.SELF_HEALING_ENABLED_VARIABLE;

	public final String EXCLUDE_KEYWORDS = SmartXPathMessageConstants.EXCLUDE_KEYWORDS_VARIABLE;
	
	public final String METHODS_PRIORITY_ORDER = SmartXPathMessageConstants.METHODS_PRIORITY_ORDER_VARIABLE;

	private Entity project;

	public static SelfHealingSetting getStore(Entity project) {
		return new SelfHealingSetting(project);
	}

	private SelfHealingSetting(Entity project) {
		this.project = project;
	}

	private PluginPreference getInnerStore(Entity currentProject) throws ResourceException {
		return ApplicationManager.getInstance().getPreferenceManager().getPluginPreference(currentProject.getId(),
				IdConstants.KATALON_SMART_XPATH_BUNDLE_ID);
	}

	public boolean isEnableSelfHHealing() throws InvalidDataTypeFormatException, ResourceException  {
		return getInnerStore(project).getBoolean(SELF_HEALING_ENABLE, false);
	}

	public void setEnableSelfHealing(boolean isEnable) throws ResourceException  {
		getInnerStore(project).setBoolean(SELF_HEALING_ENABLE, isEnable);
		getInnerStore(project).save();
	}

	public void setExcludeKeywordList(List<String> excludeKeywords) throws ResourceException {
		String jsonExcludeKeywords = JsonUtil.toJson(excludeKeywords, false);
		getInnerStore(project).setString(EXCLUDE_KEYWORDS, jsonExcludeKeywords);
	}

	public List<String> getExcludeKeywordList() throws ResourceException {
		String jsonExcludeKeywords = getInnerStore(project).getString(EXCLUDE_KEYWORDS, null);

		if (jsonExcludeKeywords == null || StringUtils.isBlank(jsonExcludeKeywords)) {
            return new ArrayList<String>();
        }

        Type excludeKeywordsMapType = new TypeToken<List<String>>() {}.getType();
        return JsonUtil.fromJson(jsonExcludeKeywords, excludeKeywordsMapType);
	}
	
	public void setMethodsPritorityOrder(List<Pair<String, Boolean>> methodsPritorityOrder) throws ResourceException {
		String jsonMethodsPriorityOrder = JsonUtil.toJson(methodsPritorityOrder, false);
		getInnerStore(project).setString(METHODS_PRIORITY_ORDER, jsonMethodsPriorityOrder);
	}
	
	public List<Pair<String, Boolean>> getMethodsPriorityOrder() throws InvalidDataTypeFormatException, ResourceException {
		String jsonMethodsPriorityOrder = getInnerStore(project).getString(METHODS_PRIORITY_ORDER, null);

		if (jsonMethodsPriorityOrder == null || StringUtils.isBlank(jsonMethodsPriorityOrder)) {
			List<Pair<String, Boolean>> methodsPriorityOrder = new ArrayList<Pair<String, Boolean>>();
			methodsPriorityOrder.add(new Pair<String, Boolean>(SmartXPathMessageConstants.XPATH_METHOD, true));
			methodsPriorityOrder.add(new Pair<String, Boolean>(SmartXPathMessageConstants.ATTRIBUTE_METHOD, true));
			methodsPriorityOrder.add(new Pair<String, Boolean>(SmartXPathMessageConstants.CSS_METHOD, true));
			methodsPriorityOrder.add(new Pair<String, Boolean>(SmartXPathMessageConstants.IMAGE_METHOD, true));
            return methodsPriorityOrder;
        }

        Type methodsPriorityOrderMapType = new TypeToken<List<Pair<String, Boolean>>>() {}.getType();
        return JsonUtil.fromJson(jsonMethodsPriorityOrder, methodsPriorityOrderMapType);
	}
}