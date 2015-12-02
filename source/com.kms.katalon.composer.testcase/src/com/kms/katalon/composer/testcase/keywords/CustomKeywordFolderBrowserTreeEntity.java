package com.kms.katalon.composer.testcase.keywords;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.testcase.constants.StringConstants;
import com.kms.katalon.controller.KeywordController;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.core.annotation.Keyword;

public class CustomKeywordFolderBrowserTreeEntity extends KeywordBrowserFolderTreeEntity {
	private static final String KEYWORD_OBJECT_ANNOTATION_METHOD = "keywordObject";
	private static final long serialVersionUID = 1L;
	private static final String simpleName = StringConstants.KEYWORD_BROWSER_CUSTOM_KEYWORD_ROOT_TREE_ITEM_LABEL;

	public CustomKeywordFolderBrowserTreeEntity(IKeywordBrowserTreeEntity parent) {
		super("Custom Keywords", parent);
	}

	@Override
	public boolean hasChildren() {
		try {
			if (ProjectController.getInstance().getCurrentProject() != null
					&& KeywordController.getInstance()
							.getCustomKeywords(ProjectController.getInstance().getCurrentProject()).size() > 0) {
				return true;
			}
		} catch (Exception e) {
			LoggerSingleton.logError(e);
		}
		return false;
	}

	@Override
	public Object[] getChildren() {
		try {
			return getKeywordByKeywordObject().toArray();
		} catch (Exception e) {
			LoggerSingleton.logError(e);
		}
		return null;
	}

	private List<IKeywordBrowserTreeEntity> getKeywordByKeywordObject() throws Exception {
		List<Method> allKeywordMethod = KeywordController.getInstance().getAllCustomKeywordsAsAst(
				ProjectController.getInstance().getCurrentProject());
		Map<String, List<Method>> methodActionMap = new HashMap<String, List<Method>>();
		for (Method method : allKeywordMethod) {
			String keywordObjectParameter = getKeywordObject(method);
			if (keywordObjectParameter != null) {
				List<Method> methodList = methodActionMap.get(keywordObjectParameter);
				if (methodList == null) {
					methodList = new ArrayList<Method>();
					methodActionMap.put(keywordObjectParameter, methodList);
				}
				methodList.add(method);
			}
		}
		List<IKeywordBrowserTreeEntity> childTreeEntityList = new ArrayList<IKeywordBrowserTreeEntity>();
		Iterator<Entry<String, List<Method>>> it = methodActionMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<Method>> pair = (Entry<String, List<Method>>) it.next();
			KeywordBrowserFolderTreeEntity keywordFolder = new KeywordBrowserFolderTreeEntity(pair.getKey(), this);
			for (Method method : pair.getValue()) {
				keywordFolder.children.add(new KeywordBrowserTreeEntity(simpleName, method.getDeclaringClass()
						.getName() + "." + method.getName(), true, keywordFolder));
			}
			
			Collections.sort(keywordFolder.children, new Comparator<IKeywordBrowserTreeEntity>() {

	            @Override
	            public int compare(IKeywordBrowserTreeEntity keywordA, IKeywordBrowserTreeEntity keywordB) {
	                return keywordA.getName().compareToIgnoreCase(keywordB.getName());
	            }
	        });
			
			childTreeEntityList.add(keywordFolder);
		}
		return childTreeEntityList;
	}

	private String getKeywordObject(Method method) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		for (Annotation annotation : method.getDeclaredAnnotations()) {
			if (annotation.annotationType().getName().equals(Keyword.class.getName())) {
				return (String) annotation.annotationType().getMethod(KEYWORD_OBJECT_ANNOTATION_METHOD)
						.invoke(annotation);
			}
		}
		return null;
	}
}
