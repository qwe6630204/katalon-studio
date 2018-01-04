package com.kms.katalon.composer.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.kms.katalon.composer.components.impl.tree.FolderTreeEntity;
import com.kms.katalon.composer.components.impl.tree.TestCaseTreeEntity;
import com.kms.katalon.composer.components.impl.tree.TestSuiteTreeEntity;
import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.components.tree.ITreeEntity;
import com.kms.katalon.composer.project.constants.StringConstants;
import com.kms.katalon.composer.util.groovy.GroovyGuiUtil;
import com.kms.katalon.console.constants.ConsoleStringConstants;
import com.kms.katalon.console.utils.ApplicationInfo;
import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.controller.FolderController;
import com.kms.katalon.controller.ObjectRepositoryController;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.controller.TestCaseController;
import com.kms.katalon.controller.TestSuiteController;
import com.kms.katalon.entity.file.FileEntity;
import com.kms.katalon.entity.folder.FolderEntity;
import com.kms.katalon.entity.folder.FolderEntity.FolderType;
import com.kms.katalon.entity.link.TestSuiteTestCaseLink;
import com.kms.katalon.entity.repository.WebElementEntity;
import com.kms.katalon.entity.repository.WebElementSelectorMethod;
import com.kms.katalon.entity.testcase.TestCaseEntity;
import com.kms.katalon.entity.testsuite.TestSuiteEntity;
import com.kms.katalon.execution.launcher.manager.LauncherManager;
import com.kms.katalon.selenium.ide.SeleniumIdeFormatter;
import com.kms.katalon.selenium.ide.SeleniumIdeParser;
import com.kms.katalon.selenium.ide.model.Command;
import com.kms.katalon.selenium.ide.model.TestCase;
import com.kms.katalon.selenium.ide.model.TestSuite;

public class ImportSeleniumIdeHandler {
	
	private static final String COMMON_FOLDER_NAME = "Common";

	private static final String IMPORTED_FOLDER_NAME = "Imported from Selenium IDE Scripts";

	@Inject
    private IEventBroker eventBroker;

	private FolderTreeEntity testSuiteTreeRoot;
	
	private FolderTreeEntity testCaseTreeRoot;
	
	private FolderTreeEntity objectRepositoryTreeRoot;
	
	@CanExecute
	public boolean canExecute() {
	    return (ProjectController.getInstance().getCurrentProject() != null)
                && !LauncherManager.getInstance().isAnyLauncherRunning();
	}
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		try {
			FileDialog fileDialog = new FileDialog(shell, SWT.SYSTEM_MODAL);
			fileDialog.setText(StringConstants.HAND_IMPORT_SELENIUM_IDE);
			fileDialog.setFilterPath(Platform.getLocation().toString());
			String selectedFilePath = fileDialog.open();
			if (selectedFilePath != null && selectedFilePath.length() > 0) {
				File selectedFile = new File(selectedFilePath);
				if (selectedFile.exists()) {
					if (SeleniumIdeParser.getInstance().isTestSuiteFile(selectedFile)) {
						TestSuite testSuite = SeleniumIdeParser.getInstance().parseTestSuite(selectedFile);
						createTestSuite(testSuite);
					} else if (SeleniumIdeParser.getInstance().isTestCaseFile(selectedFile)) {
						TestCase testCase = SeleniumIdeParser.getInstance().parseTestCase(selectedFile);
						if (testCase != null) {
							createTestObjects(null, Arrays.asList(testCase));
							FolderEntity importTestCaseFolder = getOrCreateFolder(IMPORTED_FOLDER_NAME, testCaseTreeRoot.getObject());
							createTestCaseEntity(testCase, importTestCaseFolder);
						}
					}
				}
			}
		} catch (Exception e) {
			LoggerSingleton.logError(e);
		}
	}
	
	private void createTestSuite(TestSuite testSuite) throws Exception {
		if (testSuite == null) {
			return;
		}
        TestSuiteController tsController = TestSuiteController.getInstance();
        FolderEntity testSuiteParentFolderEntity = getOrCreateFolder(IMPORTED_FOLDER_NAME, testSuiteTreeRoot.getObject());
        
		TestSuiteEntity testSuiteEntity = tsController.newTestSuiteWithoutSave(testSuiteParentFolderEntity, testSuite.getName());
		testSuiteEntity = tsController.saveNewTestSuite(testSuiteEntity);
        
        createTestCases(testSuiteEntity, testSuite.getTestCases());
        
        eventBroker.send(EventConstants.EXPLORER_REFRESH_TREE_ENTITY, testSuiteTreeRoot);
        eventBroker.post(EventConstants.EXPLORER_SET_SELECTED_ITEM, new TestSuiteTreeEntity(testSuiteEntity, testSuiteTreeRoot));
        eventBroker.post(EventConstants.TEST_SUITE_OPEN, testSuiteEntity);
	}

	private void createTestCases(TestSuiteEntity testSuiteEntity, List<TestCase> testCases) throws Exception {
		FolderEntity importTestCaseFolder = createImportTestCaseFolderEntity(testSuiteEntity.getName());
        if (!testCases.isEmpty()) {
        	
        	createTestObjects(testSuiteEntity.getName(), testCases);
        	
        	List<TestSuiteTestCaseLink> testSuiteTestCaseLinks = new ArrayList<>();
        	for (TestCase testCase : testCases) {
        		
        		TestCaseEntity testCaseEntity = createTestCaseEntity(testCase, importTestCaseFolder);
        		
        		TestSuiteTestCaseLink testSuiteTestCaseLink = new TestSuiteTestCaseLink();
        		testSuiteTestCaseLink.setTestCaseId(testCaseEntity.getIdForDisplay());
        		testSuiteTestCaseLinks.add(testSuiteTestCaseLink);
        	}
        	
        	TestSuiteController tsController = TestSuiteController.getInstance();
        	testSuiteEntity.setTestSuiteTestCaseLinks(testSuiteTestCaseLinks);
        	tsController.updateTestSuite(testSuiteEntity);
        	eventBroker.send(EventConstants.TEST_SUITE_OPEN, testSuiteEntity);
        }
	}
	
	private void createTestObjects(String parentFolderName, List<TestCase> testCases) throws Exception {
		Map<String, Set<String>> testObjectNameMap = parseTestObjectNameMap(testCases);
		Set<String> retains = retainAllTestObjects(testObjectNameMap);
		
		// test object common folder
		if (retains.size() > 0) {
			String commonFolderName = COMMON_FOLDER_NAME;
			FolderEntity commonFolderEntity = createImportTestObjectFolderEntity(parentFolderName, commonFolderName);
			for (String t : retains) {
				createTestObject(commonFolderEntity, t);
			}
		}
		
		Iterator<Entry<String, Set<String>>> iterator = testObjectNameMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Set<String>> entry = iterator.next();
			String key = entry.getKey();
			Set<String> value = entry.getValue();
			
			if (value.size() > 0) {
				FolderEntity folderEntity = createImportTestObjectFolderEntity(parentFolderName, key);
				for (String l : value) {
					boolean has = false;
					for (String r : retains) {
						if (l.equalsIgnoreCase(r)) {
							has = true;
							break;
						}
					}
					
					if (!has) {
						createTestObject(folderEntity, l);
					}
				}
			}
		}
	}
	
	private Set<String> retainAllTestObjects(Map<String, Set<String>> map) {
		Set<String> ret = new HashSet<>();
		if (map.size() > 1) {
			Iterator<Entry<String, Set<String>>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Set<String>> entry = iterator.next();
				Set<String> retains = retainTestObjects(entry, map);
				ret.addAll(retains);
			}
		} 
		return ret;
	}
	
	private Set<String> retainTestObjects(Entry<String, Set<String>> item, Map<String, Set<String>> map) {
		Set<String> ret = new HashSet<>();
		Iterator<Entry<String, Set<String>>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Set<String>> entry = iterator.next();
			if (entry.getKey().equalsIgnoreCase(item.getKey())) {
				continue;
			}
			Set<String> value = entry.getValue();
			
			if (value != null) {
				Set<String> origin = new HashSet<>(item.getValue());
				origin.retainAll(value);
				
				ret.addAll(origin);
			}
		}
		return ret;
	}
	
	private FolderEntity createImportTestCaseFolderEntity(String testCaseFolderName) throws Exception {
        FolderEntity parentFolderEntity = getOrCreateFolder(IMPORTED_FOLDER_NAME, testCaseTreeRoot.getObject());
        return FolderController.getInstance().addNewFolder(parentFolderEntity, testCaseFolderName);        
	}
	
	private FolderEntity createImportTestObjectFolderEntity(String testSuiteFolderName, String testObjectFolderName) throws Exception {
        FolderEntity parentFolderEntity = getOrCreateFolder(IMPORTED_FOLDER_NAME, objectRepositoryTreeRoot.getObject());
        FolderEntity testSuiteFolderEntity = null;
        if (StringUtils.isNotBlank(testSuiteFolderName)) {
        	testSuiteFolderEntity = getOrCreateFolder(testSuiteFolderName, parentFolderEntity);
        } else {
        	testSuiteFolderEntity = parentFolderEntity;
        }
        return FolderController.getInstance().addNewFolder(testSuiteFolderEntity, testObjectFolderName);        
	}
	
	private TestCaseEntity createTestCaseEntity(TestCase testCase, FolderEntity importTestCaseFolder) throws Exception {
		SeleniumIdeFormatter.getInstance().setEmail(ApplicationInfo.getAppProperty(ConsoleStringConstants.ARG_EMAIL));
		TestCaseController tcController = TestCaseController.getInstance();
		TestCaseEntity testCaseEntity = tcController.newTestCaseWithoutSave(importTestCaseFolder, testCase.getName());
		testCaseEntity = tcController.saveNewTestCase(testCaseEntity);
		
		String scriptContent = SeleniumIdeFormatter.getInstance().format(testCase);
		GroovyGuiUtil.addContentToTestCase(testCaseEntity, scriptContent);
		
        eventBroker.send(EventConstants.EXPLORER_REFRESH_TREE_ENTITY, testCaseTreeRoot);
        eventBroker.send(EventConstants.EXPLORER_REFRESH_SELECTED_ITEM, testCaseTreeRoot);
        eventBroker.send(EventConstants.EXPLORER_SET_SELECTED_ITEM, new TestCaseTreeEntity(testCaseEntity, testCaseTreeRoot));
		eventBroker.send(EventConstants.TESTCASE_OPEN, testCaseEntity);
		
		return testCaseEntity;
	}
	
	private Map<String, Set<String>> parseTestObjectNameMap(List<TestCase> testCases) {
		Map<String, Set<String>> map = new HashMap<>();
		for (TestCase testCase : testCases) {
			Set<String> testObjects = new HashSet<>();
			for (Command command: testCase.getCommands()) {
				testObjects.add(command.getTarget());
			}
			map.put(testCase.getName(), testObjects);
		}
		return map;
	}
	
	private WebElementEntity createTestObject(FolderEntity parentFolderEntity, String target) throws Exception {
        ObjectRepositoryController toController = ObjectRepositoryController.getInstance();
		String testObjectName = SeleniumIdeParser.getInstance().parseTestObjectName(target);
		String locator = SeleniumIdeParser.getInstance().parseLocator(target);
		WebElementEntity webElement = null;
		if (StringUtils.isNotBlank(locator)) {
			webElement = getWebElementByName(testObjectName, parentFolderEntity);
			if (webElement == null) {
				webElement = toController.newTestObjectWithoutSave(parentFolderEntity, testObjectName);
				webElement.setSelectorMethod(WebElementSelectorMethod.XPATH);
				webElement.setSelectorValue(WebElementSelectorMethod.XPATH, locator);
				webElement = toController.saveNewTestObject(webElement);
			} 
		}
		return webElement;
	}
	
	private WebElementEntity getWebElementByName(String name, FolderEntity folderEntity) throws Exception {
		List<FileEntity> files = FolderController.getInstance().getChildren(folderEntity);
		for (FileEntity entity : files) {
            if (entity instanceof WebElementEntity) {
            	WebElementEntity webElement = (WebElementEntity) entity;
            	if (webElement.getName().equalsIgnoreCase(name)) {
            		return webElement;
            	}
            }
        }
		return null;
	}
	
	@Inject
    @Optional
    private void catchTestSuiteFolderTreeEntitiesRoot(
            @UIEventTopic(EventConstants.EXPLORER_RELOAD_INPUT) List<Object> treeEntities) {
        try {
            testSuiteTreeRoot = getFolderTreeEntity(FolderType.TESTSUITE, treeEntities);            
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
    }
	
	@Inject
    @Optional
    private void catchTestCaseTreeEntitiesRoot(
            @UIEventTopic(EventConstants.EXPLORER_RELOAD_INPUT) List<Object> treeEntities) {
        try {
            testCaseTreeRoot = getFolderTreeEntity(FolderType.TESTCASE, treeEntities);
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
    }
	
	@Inject
    @Optional
    private void execute(@UIEventTopic(EventConstants.TEST_SUITE_NEW) Object eventData) {
        if (!canExecute()) {
            return;
        }
        execute(Display.getCurrent().getActiveShell());
    }
	
	@Inject
	@Optional
	private void catchObjectTreeEntitiesRoot(
			@UIEventTopic(EventConstants.EXPLORER_RELOAD_INPUT) List<Object> treeEntities) {
		try {
			objectRepositoryTreeRoot = getFolderTreeEntity(FolderType.WEBELEMENT, treeEntities);
		} catch (Exception e) {
			LoggerSingleton.logError(e);
		}
	}
	
	private FolderTreeEntity getFolderTreeEntity(FolderType folderType, List<Object> treeEntities) throws Exception {
        for (Object o : treeEntities) {
            Object entityObject = ((ITreeEntity) o).getObject();
            if (!(entityObject instanceof FolderEntity)) {
                return null;
            }
            FolderEntity folder = (FolderEntity) entityObject;
            if (folder.getFolderType() == folderType) {
                return (FolderTreeEntity) o;
            }
        }
        return null;
    }
	
	private FolderEntity getOrCreateFolder(String folderName, FolderEntity parentFolder) throws Exception {
		List<FolderEntity> folders = FolderController.getInstance().getChildFolders(parentFolder);
		for (FolderEntity folder: folders) {
			if (folder.getName().equalsIgnoreCase(folderName)) {
				return folder;
			}
		}
		return FolderController.getInstance().addNewFolder(parentFolder, folderName);
	}
}
