package com.kms.katalon.selenium.ide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.kms.katalon.selenium.ide.format.DefaultFormatter;
import com.kms.katalon.selenium.ide.format.EchoFormatter;
import com.kms.katalon.selenium.ide.format.Formatter;
import com.kms.katalon.selenium.ide.format.PauseFormatter;
import com.kms.katalon.selenium.ide.format.StoreFormatter;
import com.kms.katalon.selenium.ide.format.VerifyAndAssertFormatter;
import com.kms.katalon.selenium.ide.format.WaitForFormatter;
import com.kms.katalon.selenium.ide.model.Command;
import com.kms.katalon.selenium.ide.model.TestCase;

public final class SeleniumIdeFormatter {
	
	private static final String DATE_FORMAT = "dd-MMM-yyyy hh:mm:ss a";
	
	private String email;

	private static final SeleniumIdeFormatter INSTANCE = new SeleniumIdeFormatter();
	
	private final Map<String, Formatter> formatters = new LinkedHashMap<>();
	
	{
		formatters.put("assert", new VerifyAndAssertFormatter("assert"));
		formatters.put("verify", new VerifyAndAssertFormatter("verify"));
		formatters.put("store", new StoreFormatter());
		
		formatters.put("sendKeys", new DefaultFormatter());
		formatters.put("chooseCancelOnNextPrompt", new DefaultFormatter());
		
		formatters.put("waitForPageToLoad", new DefaultFormatter());
		formatters.put("waitForCondition", new DefaultFormatter());
		formatters.put("waitForFrameToLoad", new DefaultFormatter());
		formatters.put("waitForPopUp", new DefaultFormatter());
		
		formatters.put("waitFor", new WaitForFormatter());
		
		formatters.put("echo", new EchoFormatter());
		formatters.put("pause", new PauseFormatter());
		formatters.put("default", new DefaultFormatter());
	}
	
	public static SeleniumIdeFormatter getInstance() {
        return INSTANCE;
    }

	public String format(TestCase testCase) {
		StringBuilder builder = new StringBuilder();
		builder.append(getHeader(testCase));
		
		List<String> commands = formatCommands(testCase.getCommands());
		commands.forEach(c -> builder.append(c));
		
		builder.append(getFooter(testCase));
		return builder.toString();
	}
	
	public List<String> formatCommands(List<Command> commands) {
		List<String> formattedCommands = new ArrayList<>();
		commands.forEach(command -> {
			String formatted = formatCommand(command);
			formattedCommands.add(formatted);
		});
		return formattedCommands;				
	}
	
	public String formatCommand(Command command) {
		Formatter formatter = getFormatter(command.getCommand());
		if (formatter == null) {
			return String.format("Method %s is not found", command.getCommand());
		}
		String comment = String.format("\n\"%s | %s | %s\"\n", 
				encodeString(command.getCommand()), 
				encodeString(command.getTarget()), 
				encodeString(command.getValue()));
		String formatted = formatter.format(command);
		if (StringUtils.isBlank(formatted)) {
			return String.format("Method %s is not found\n", command.getCommand());
		}
		return comment + formatted;
	}

	public String getHeader(TestCase testCase) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(  "import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint\n" +
						"import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase\n" +
						"import static com.kms.katalon.core.testdata.TestDataFactory.findTestData\n" +
						"import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject\n" +
						"import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint\n" +
						"import com.kms.katalon.core.checkpoint.CheckpointFactory as CheckpointFactory\n" +
						"import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as MobileBuiltInKeywords\n" +
						"import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile\n" +
						"import com.kms.katalon.core.model.FailureHandling as FailureHandling\n" +
						"import com.kms.katalon.core.testcase.TestCase as TestCase\n" +
						"import com.kms.katalon.core.testcase.TestCaseFactory as TestCaseFactory\n" +
						"import com.kms.katalon.core.testdata.TestData as TestData\n" +
						"import com.kms.katalon.core.testdata.TestDataFactory as TestDataFactory\n" +
						"import com.kms.katalon.core.testobject.ObjectRepository as ObjectRepository\n" +
						"import com.kms.katalon.core.testobject.TestObject as TestObject\n" +
						"import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WSBuiltInKeywords\n" +
						"import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS\n" +
						"import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUiBuiltInKeywords\n" +
						"import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI\n" +
						"import internal.GlobalVariable as GlobalVariable\n");
		
		buffer.append(  "import com.kms.katalon.core.webui.driver.DriverFactory as DriverFactory\n" +
						"import static com.kms.katalon.core.webui.driver.KatalonWebDriverBackedSelenium.WAIT_FOR_PAGE_TO_LOAD_IN_SECONDS\n" + 
						"import com.kms.katalon.core.webui.driver.KatalonWebDriverBackedSelenium\n\n" +
						"import com.thoughtworks.selenium.Selenium\n" +
						"import org.openqa.selenium.firefox.FirefoxDriver\n" +
						"import org.openqa.selenium.WebDriver\n" +						
						"import static org.junit.Assert.*\n" +
						"import java.util.regex.Pattern\n" +
						"import static org.apache.commons.lang3.StringUtils.join\n\n");
		buffer.append("'----------------------------------------------------'\n");
		buffer.append("'This test case script is generated by Katalon Studio'\n");
		buffer.append(String.format("'Generated date: %s'\n", getCurrentDateTime()));
		buffer.append(String.format("'File path: %s'\n", encodeString(testCase.getFilePath())));
		buffer.append(String.format("'Generated by user email: %s'\n", this.email));
		buffer.append("'----------------------------------------------------'\n\n");
		buffer.append("String baseUrl = \""+ testCase.getBaseUrl() +"\"\n\n");
		buffer.append("WebUI.openBrowser(baseUrl)\n\n");
		buffer.append("selenium = new KatalonWebDriverBackedSelenium(baseUrl)\n");
		return buffer.toString();
	}

	public String getFooter(TestCase testCase) {
		return "\nWebUI.closeBrowser()";
	}
	
	private String encodeString(String filePath) {
		return filePath.replace("\\", Matcher.quoteReplacement(File.separator));
	}
	
	private String getCurrentDateTime() {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return formatter.format(Calendar.getInstance().getTime());
	}
	
	private Formatter getFormatter(String command) {
		if (StringUtils.isBlank(command)) {
			return null;
		}
		for (Map.Entry<String, Formatter> entry : formatters.entrySet()) {
		    String key = entry.getKey();
		    Formatter formatter = entry.getValue();
		    if (command.contains(key)) {
		    	return formatter;
		    }
		}
		return formatters.get("default");
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getEmail() {
		return email;
	}
	
	public static void main(String[] args) {
		List<Command> commands = new ArrayList<>();
		
		commands.add(new Command("addLocationStrategy", "aaa", "bbb"));
		commands.add(new Command("addLocationStrategyAndWait", "aaa", "bbb"));
		commands.add(new Command("addScript", "aaa", "bbb"));
		commands.add(new Command("addScriptAndWait", "aaa", "bbb"));
		commands.add(new Command("addSelection", "aaa", "bbb"));
		commands.add(new Command("addSelectionAndWait", "aaa", "bbb"));
		commands.add(new Command("ajaxWait", "aaa", "bbb"));
		commands.add(new Command("ajaxWaitAndWait", "aaa", "bbb"));
		commands.add(new Command("allowNativeXpath", "aaa", "bbb"));
		commands.add(new Command("allowNativeXpathAndWait", "aaa", "bbb"));
		commands.add(new Command("altKeyDown", "aaa", "bbb"));
		commands.add(new Command("altKeyDownAndWait", "aaa", "bbb"));
		commands.add(new Command("altKeyUp", "aaa", "bbb"));
		commands.add(new Command("altKeyUpAndWait", "aaa", "bbb"));
		commands.add(new Command("answerOnNextPrompt", "aaa", "bbb"));
		commands.add(new Command("assertAlert", "aaa", "bbb"));
		commands.add(new Command("assertAlertAndWait", "aaa", "bbb"));
		commands.add(new Command("assertAlertNotPresent", "aaa", "bbb"));
		commands.add(new Command("assertAlertPresent", "aaa", "bbb"));
		commands.add(new Command("assertAllButtons", "aaa", "bbb"));
		commands.add(new Command("assertAllFields", "aaa", "bbb"));
		commands.add(new Command("assertAllLinks", "aaa", "bbb"));
		commands.add(new Command("assertAllWindowIds", "aaa", "bbb"));
		commands.add(new Command("assertAllWindowNames", "aaa", "bbb"));
		commands.add(new Command("assertAllWindowTitles", "aaa", "bbb"));
		commands.add(new Command("assertAttribute", "aaa", "bbb"));
		commands.add(new Command("assertAttributeFromAllWindows", "aaa", "bbb"));
		commands.add(new Command("assertBodyText", "aaa", "bbb"));
		commands.add(new Command("assertChecked", "aaa", "bbb"));
		commands.add(new Command("assertConfirmation", "aaa", "bbb"));
		commands.add(new Command("assertConfirmationAndWait", "aaa", "bbb"));
		commands.add(new Command("assertConfirmationNotPresent", "aaa", "bbb"));
		commands.add(new Command("assertConfirmationPresent", "aaa", "bbb"));
		commands.add(new Command("assertCookie", "aaa", "bbb"));
		commands.add(new Command("assertCookieByName", "aaa", "bbb"));
		commands.add(new Command("assertCookieNotPresent", "aaa", "bbb"));
		commands.add(new Command("assertCookiePresent", "aaa", "bbb"));
		commands.add(new Command("assertCssCount", "aaa", "bbb"));
		commands.add(new Command("assertCursorPosition", "aaa", "bbb"));
		commands.add(new Command("assertEditable", "aaa", "bbb"));
		commands.add(new Command("assertElementHeight", "aaa", "bbb"));
		commands.add(new Command("assertElementIndex", "aaa", "bbb"));
		commands.add(new Command("assertElementNotPresent", "aaa", "bbb"));
		commands.add(new Command("assertElementPositionLeft", "aaa", "bbb"));
		commands.add(new Command("assertElementPositionTop", "aaa", "bbb"));
		commands.add(new Command("assertElementPresent", "aaa", "bbb"));
		commands.add(new Command("assertElementWidth", "aaa", "bbb"));
		commands.add(new Command("assertEval", "aaa", "bbb"));
		commands.add(new Command("assertExpression", "aaa", "bbb"));
		commands.add(new Command("assertHtmlSource", "aaa", "bbb"));
		commands.add(new Command("assertLocation", "aaa", "bbb"));
		commands.add(new Command("assertMouseSpeed", "aaa", "bbb"));
		commands.add(new Command("assertNotAlert", "aaa", "bbb"));
		commands.add(new Command("assertNotAllButtons", "aaa", "bbb"));
		commands.add(new Command("assertNotAllFields", "aaa", "bbb"));
		commands.add(new Command("assertNotAllLinks", "aaa", "bbb"));
		commands.add(new Command("assertNotAllWindowIds", "aaa", "bbb"));
		commands.add(new Command("assertNotAllWindowNames", "aaa", "bbb"));
		commands.add(new Command("assertNotAllWindowTitles", "aaa", "bbb"));
		commands.add(new Command("assertNotAttribute", "aaa", "bbb"));
		commands.add(new Command("assertNotAttributeFromAllWindows", "aaa", "bbb"));
		commands.add(new Command("assertNotBodyText", "aaa", "bbb"));
		commands.add(new Command("assertNotChecked", "aaa", "bbb"));
		commands.add(new Command("assertNotConfirmation", "aaa", "bbb"));
		commands.add(new Command("assertNotCookie", "aaa", "bbb"));
		commands.add(new Command("assertNotCookieByName", "aaa", "bbb"));
		commands.add(new Command("assertNotCssCount", "aaa", "bbb"));
		commands.add(new Command("assertNotCursorPosition", "aaa", "bbb"));
		commands.add(new Command("assertNotEditable", "aaa", "bbb"));
		commands.add(new Command("assertNotElementHeight", "aaa", "bbb"));
		commands.add(new Command("assertNotElementIndex", "aaa", "bbb"));
		commands.add(new Command("assertNotElementPositionLeft", "aaa", "bbb"));
		commands.add(new Command("assertNotElementPositionTop", "aaa", "bbb"));
		commands.add(new Command("assertNotElementWidth", "aaa", "bbb"));
		commands.add(new Command("assertNotEval", "aaa", "bbb"));
		commands.add(new Command("assertNotExpression", "aaa", "bbb"));
		commands.add(new Command("assertNotHtmlSource", "aaa", "bbb"));
		commands.add(new Command("assertNotLocation", "aaa", "bbb"));
		commands.add(new Command("assertNotMouseSpeed", "aaa", "bbb"));
		commands.add(new Command("assertNotOrdered", "aaa", "bbb"));
		commands.add(new Command("assertNotPrompt", "aaa", "bbb"));
		commands.add(new Command("assertNotSelectOptions", "aaa", "bbb"));
		commands.add(new Command("assertNotSelectedId", "aaa", "bbb"));
		commands.add(new Command("assertNotSelectedIds", "aaa", "bbb"));
		commands.add(new Command("assertNotSelectedIndex", "aaa", "bbb"));
		commands.add(new Command("assertNotSelectedIndexes", "aaa", "bbb"));
		commands.add(new Command("assertNotSelectedLabel", "aaa", "bbb"));
		commands.add(new Command("assertNotSelectedLabels", "aaa", "bbb"));
		commands.add(new Command("assertNotSelectedValue", "aaa", "bbb"));
		commands.add(new Command("assertNotSelectedValues", "aaa", "bbb"));
		commands.add(new Command("assertNotSomethingSelected", "aaa", "bbb"));
		commands.add(new Command("assertNotSpeed", "aaa", "bbb"));
		commands.add(new Command("assertNotTable", "aaa", "bbb"));
		commands.add(new Command("assertNotText", "aaa", "bbb"));
		commands.add(new Command("assertNotTitle", "aaa", "bbb"));
		commands.add(new Command("assertNotValue", "aaa", "bbb"));
		commands.add(new Command("assertNotVisible", "aaa", "bbb"));
		commands.add(new Command("assertNotWhetherThisFrameMatchFrameExpression", "aaa", "bbb"));
		commands.add(new Command("assertNotWhetherThisWindowMatchWindowExpression", "aaa", "bbb"));
		commands.add(new Command("assertNotXpathCount", "aaa", "bbb"));
		commands.add(new Command("assertOrdered", "aaa", "bbb"));
		commands.add(new Command("assertPrompt", "aaa", "bbb"));
		commands.add(new Command("assertPromptAndWait", "aaa", "bbb"));
		commands.add(new Command("assertPromptNotPresent", "aaa", "bbb"));
		commands.add(new Command("assertPromptPresent", "aaa", "bbb"));
		commands.add(new Command("assertSelectOptions", "aaa", "bbb"));
		commands.add(new Command("assertSelectedId", "aaa", "bbb"));
		commands.add(new Command("assertSelectedIds", "aaa", "bbb"));
		commands.add(new Command("assertSelectedIndex", "aaa", "bbb"));
		commands.add(new Command("assertSelectedIndexes", "aaa", "bbb"));
		commands.add(new Command("assertSelectedLabel", "aaa", "bbb"));
		commands.add(new Command("assertSelectedLabels", "aaa", "bbb"));
		commands.add(new Command("assertSelectedValue", "aaa", "bbb"));
		commands.add(new Command("assertSelectedValues", "aaa", "bbb"));
		commands.add(new Command("assertSomethingSelected", "aaa", "bbb"));
		commands.add(new Command("assertSpeed", "aaa", "bbb"));
		commands.add(new Command("assertTable", "aaa", "bbb"));
		commands.add(new Command("assertText", "aaa", "bbb"));
		commands.add(new Command("assertTextAndWait", "aaa", "bbb"));
		commands.add(new Command("assertTextNotPresent", "aaa", "bbb"));
		commands.add(new Command("assertTextPresent", "aaa", "bbb"));
		commands.add(new Command("assertTitle", "aaa", "bbb"));
		commands.add(new Command("assertTitleAndWait", "aaa", "bbb"));
		commands.add(new Command("assertValue", "aaa", "bbb"));
		commands.add(new Command("assertValueAndWait", "aaa", "bbb"));
		commands.add(new Command("assertVisible", "aaa", "bbb"));
		commands.add(new Command("assertWhetherThisFrameMatchFrameExpression", "aaa", "bbb"));
		commands.add(new Command("assertWhetherThisWindowMatchWindowExpression", "aaa", "bbb"));
		commands.add(new Command("assertXpathCount", "aaa", "bbb"));
		commands.add(new Command("assignId", "aaa", "bbb"));
		commands.add(new Command("assignIdAndWait", "aaa", "bbb"));
		commands.add(new Command("break", "aaa", "bbb"));
		commands.add(new Command("captureEntirePageScreenshot", "aaa", "bbb"));
		commands.add(new Command("captureEntirePageScreenshotAndWait", "aaa", "bbb"));
		commands.add(new Command("check", "aaa", "bbb"));
		commands.add(new Command("checkAndWait", "aaa", "bbb"));
		commands.add(new Command("chooseCancelOnNextConfirmation", "aaa", "bbb"));
		commands.add(new Command("chooseCancelOnNextPrompt", "aaa", "bbb"));
		commands.add(new Command("chooseCancelOnNextPromptAndWait", "aaa", "bbb"));
		commands.add(new Command("chooseOkOnNextConfirmation", "aaa", "bbb"));
		commands.add(new Command("chooseOkOnNextConfirmationAndWait", "aaa", "bbb"));
		commands.add(new Command("click", "aaa", "bbb"));
		commands.add(new Command("clickAndWait", "aaa", "bbb"));
		commands.add(new Command("clickAt", "aaa", "bbb"));
		commands.add(new Command("clickAtAndWait", "aaa", "bbb"));
		commands.add(new Command("close", "aaa", "bbb"));
		commands.add(new Command("contextMenu", "aaa", "bbb"));
		commands.add(new Command("contextMenuAndWait", "aaa", "bbb"));
		commands.add(new Command("contextMenuAt", "aaa", "bbb"));
		commands.add(new Command("contextMenuAtAndWait", "aaa", "bbb"));
		commands.add(new Command("controlKeyDown", "aaa", "bbb"));
		commands.add(new Command("controlKeyDownAndWait", "aaa", "bbb"));
		commands.add(new Command("controlKeyUp", "aaa", "bbb"));
		commands.add(new Command("controlKeyUpAndWait", "aaa", "bbb"));
		commands.add(new Command("createCookie", "aaa", "bbb"));
		commands.add(new Command("createCookieAndWait", "aaa", "bbb"));
		commands.add(new Command("deleteAllVisibleCookies", "aaa", "bbb"));
		commands.add(new Command("deleteAllVisibleCookiesAndWait", "aaa", "bbb"));
		commands.add(new Command("deleteCookie", "aaa", "bbb"));
		commands.add(new Command("deleteCookieAndWait", "aaa", "bbb"));
		commands.add(new Command("deselectPopUp", "aaa", "bbb"));
		commands.add(new Command("deselectPopUpAndWait", "aaa", "bbb"));
		commands.add(new Command("domWait", "aaa", "bbb"));
		commands.add(new Command("domWaitAndWait", "aaa", "bbb"));
		commands.add(new Command("doubleClick", "aaa", "bbb"));
		commands.add(new Command("doubleClickAndWait", "aaa", "bbb"));
		commands.add(new Command("doubleClickAt", "aaa", "bbb"));
		commands.add(new Command("doubleClickAtAndWait", "aaa", "bbb"));
		commands.add(new Command("dragAndDrop", "aaa", "bbb"));
		commands.add(new Command("dragAndDropAndWait", "aaa", "bbb"));
		commands.add(new Command("dragAndDropToObject", "aaa", "bbb"));
		commands.add(new Command("dragAndDropToObjectAndWait", "aaa", "bbb"));
		commands.add(new Command("echo", "aaa", "bbb"));
		commands.add(new Command("echoAndWait", "aaa", "bbb"));
		commands.add(new Command("editContent", "aaa", "bbb"));
		commands.add(new Command("editContentAndWait", "aaa", "bbb"));
		commands.add(new Command("fireEvent", "aaa", "bbb"));
		commands.add(new Command("fireEventAndWait", "aaa", "bbb"));
		commands.add(new Command("focus", "aaa", "bbb"));
		commands.add(new Command("focusAndWait", "aaa", "bbb"));
		commands.add(new Command("goBack", "aaa", "bbb"));
		commands.add(new Command("goBackAndWait", "aaa", "bbb"));
		commands.add(new Command("highlight", "aaa", "bbb"));
		commands.add(new Command("highlightAndWait", "aaa", "bbb"));
		commands.add(new Command("ignoreAttributesWithoutValue", "aaa", "bbb"));
		commands.add(new Command("ignoreAttributesWithoutValueAndWait", "aaa", "bbb"));
		commands.add(new Command("keyDown", "aaa", "bbb"));
		commands.add(new Command("keyDownAndWait", "aaa", "bbb"));
		commands.add(new Command("keyPress", "aaa", "bbb"));
		commands.add(new Command("keyPressAndWait", "aaa", "bbb"));
		commands.add(new Command("keyUp", "aaa", "bbb"));
		commands.add(new Command("keyUpAndWait", "aaa", "bbb"));
		commands.add(new Command("metaKeyDown", "aaa", "bbb"));
		commands.add(new Command("metaKeyDownAndWait", "aaa", "bbb"));
		commands.add(new Command("metaKeyUp", "aaa", "bbb"));
		commands.add(new Command("metaKeyUpAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseDown", "aaa", "bbb"));
		commands.add(new Command("mouseDownAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseDownAt", "aaa", "bbb"));
		commands.add(new Command("mouseDownAtAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseDownRight", "aaa", "bbb"));
		commands.add(new Command("mouseDownRightAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseDownRightAt", "aaa", "bbb"));
		commands.add(new Command("mouseDownRightAtAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseMove", "aaa", "bbb"));
		commands.add(new Command("mouseMoveAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseMoveAt", "aaa", "bbb"));
		commands.add(new Command("mouseMoveAtAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseOut", "aaa", "bbb"));
		commands.add(new Command("mouseOutAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseOver", "aaa", "bbb"));
		commands.add(new Command("mouseOverAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseUp", "aaa", "bbb"));
		commands.add(new Command("mouseUpAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseUpAt", "aaa", "bbb"));
		commands.add(new Command("mouseUpAtAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseUpRight", "aaa", "bbb"));
		commands.add(new Command("mouseUpRightAndWait", "aaa", "bbb"));
		commands.add(new Command("mouseUpRightAt", "aaa", "bbb"));
		commands.add(new Command("mouseUpRightAtAndWait", "aaa", "bbb"));
		commands.add(new Command("open", "aaa", "bbb"));
		commands.add(new Command("openWindow", "aaa", "bbb"));
		commands.add(new Command("openWindowAndWait", "aaa", "bbb"));
		commands.add(new Command("pageWait", "aaa", "bbb"));
		commands.add(new Command("pageWaitAndWait", "aaa", "bbb"));
		commands.add(new Command("pause", "aaa", "bbb"));
		commands.add(new Command("prePageWait", "aaa", "bbb"));
		commands.add(new Command("prePageWaitAndWait", "aaa", "bbb"));
		commands.add(new Command("refresh", "aaa", "bbb"));
		commands.add(new Command("refreshAndWait", "aaa", "bbb"));
		commands.add(new Command("removeAllSelections", "aaa", "bbb"));
		commands.add(new Command("removeAllSelectionsAndWait", "aaa", "bbb"));
		commands.add(new Command("removeScript", "aaa", "bbb"));
		commands.add(new Command("removeScriptAndWait", "aaa", "bbb"));
		commands.add(new Command("removeSelection", "aaa", "bbb"));
		commands.add(new Command("removeSelectionAndWait", "aaa", "bbb"));
		commands.add(new Command("rollup", "aaa", "bbb"));
		commands.add(new Command("rollupAndWait", "aaa", "bbb"));
		commands.add(new Command("runScript", "aaa", "bbb"));
		commands.add(new Command("runScriptAndWait", "aaa", "bbb"));
		commands.add(new Command("select", "aaa", "bbb"));
		commands.add(new Command("selectAndWait", "aaa", "bbb"));
		commands.add(new Command("selectFrame", "aaa", "bbb"));
		commands.add(new Command("selectPopUp", "aaa", "bbb"));
		commands.add(new Command("selectPopUpAndWait", "aaa", "bbb"));
		commands.add(new Command("selectWindow", "aaa", "bbb"));
		commands.add(new Command("sendKeys", "aaa", "bbb"));
		commands.add(new Command("sendKeysAndWait", "aaa", "bbb"));
		commands.add(new Command("setCursorPosition", "aaa", "bbb"));
		commands.add(new Command("setCursorPositionAndWait", "aaa", "bbb"));
		commands.add(new Command("setMouseSpeed", "aaa", "bbb"));
		commands.add(new Command("setMouseSpeedAndWait", "aaa", "bbb"));
		commands.add(new Command("setSpeed", "aaa", "bbb"));
		commands.add(new Command("setSpeedAndWait", "aaa", "bbb"));
		commands.add(new Command("setTimeout", "aaa", "bbb"));
		commands.add(new Command("shiftKeyDown", "aaa", "bbb"));
		commands.add(new Command("shiftKeyDownAndWait", "aaa", "bbb"));
		commands.add(new Command("shiftKeyUp", "aaa", "bbb"));
		commands.add(new Command("shiftKeyUpAndWait", "aaa", "bbb"));
		commands.add(new Command("showElement", "aaa", "bbb"));
		commands.add(new Command("showElementAndWait", "aaa", "bbb"));
		commands.add(new Command("store", "aaa", "bbb"));
		commands.add(new Command("storeAlert", "aaa", "bbb"));
		commands.add(new Command("storeAlertPresent", "aaa", "bbb"));
		commands.add(new Command("storeAllButtons", "aaa", "bbb"));
		commands.add(new Command("storeAllFields", "aaa", "bbb"));
		commands.add(new Command("storeAllLinks", "aaa", "bbb"));
		commands.add(new Command("storeAllWindowIds", "aaa", "bbb"));
		commands.add(new Command("storeAllWindowNames", "aaa", "bbb"));
		commands.add(new Command("storeAllWindowTitles", "aaa", "bbb"));
		commands.add(new Command("storeAndWait", "aaa", "bbb"));
		commands.add(new Command("storeAttribute", "aaa", "bbb"));
		commands.add(new Command("storeAttributeAndWait", "aaa", "bbb"));
		commands.add(new Command("storeAttributeFromAllWindows", "aaa", "bbb"));
		commands.add(new Command("storeBodyText", "aaa", "bbb"));
		commands.add(new Command("storeChecked", "aaa", "bbb"));
		commands.add(new Command("storeConfirmation", "aaa", "bbb"));
		commands.add(new Command("storeConfirmationPresent", "aaa", "bbb"));
		commands.add(new Command("storeCookie", "aaa", "bbb"));
		commands.add(new Command("storeCookieByName", "aaa", "bbb"));
		commands.add(new Command("storeCookiePresent", "aaa", "bbb"));
		commands.add(new Command("storeCssCount", "aaa", "bbb"));
		commands.add(new Command("storeCursorPosition", "aaa", "bbb"));
		commands.add(new Command("storeEditable", "aaa", "bbb"));
		commands.add(new Command("storeElementHeight", "aaa", "bbb"));
		commands.add(new Command("storeElementIndex", "aaa", "bbb"));
		commands.add(new Command("storeElementPositionLeft", "aaa", "bbb"));
		commands.add(new Command("storeElementPositionTop", "aaa", "bbb"));
		commands.add(new Command("storeElementPresent", "aaa", "bbb"));
		commands.add(new Command("storeElementWidth", "aaa", "bbb"));
		commands.add(new Command("storeEval", "aaa", "bbb"));
		commands.add(new Command("storeEvalAndWait", "aaa", "bbb"));
		commands.add(new Command("storeExpression", "aaa", "bbb"));
		commands.add(new Command("storeHtmlSource", "aaa", "bbb"));
		commands.add(new Command("storeLocation", "aaa", "bbb"));
		commands.add(new Command("storeMouseSpeed", "aaa", "bbb"));
		commands.add(new Command("storeOrdered", "aaa", "bbb"));
		commands.add(new Command("storePrompt", "aaa", "bbb"));
		commands.add(new Command("storePromptPresent", "aaa", "bbb"));
		commands.add(new Command("storeSelectOptions", "aaa", "bbb"));
		commands.add(new Command("storeSelectedId", "aaa", "bbb"));
		commands.add(new Command("storeSelectedIds", "aaa", "bbb"));
		commands.add(new Command("storeSelectedIndex", "aaa", "bbb"));
		commands.add(new Command("storeSelectedIndexes", "aaa", "bbb"));
		commands.add(new Command("storeSelectedLabel", "aaa", "bbb"));
		commands.add(new Command("storeSelectedLabels", "aaa", "bbb"));
		commands.add(new Command("storeSelectedValue", "aaa", "bbb"));
		commands.add(new Command("storeSelectedValues", "aaa", "bbb"));
		commands.add(new Command("storeSomethingSelected", "aaa", "bbb"));
		commands.add(new Command("storeSpeed", "aaa", "bbb"));
		commands.add(new Command("storeTable", "aaa", "bbb"));
		commands.add(new Command("storeText", "aaa", "bbb"));
		commands.add(new Command("storeTextAndWait", "aaa", "bbb"));
		commands.add(new Command("storeTextPresent", "aaa", "bbb"));
		commands.add(new Command("storeTitle", "aaa", "bbb"));
		commands.add(new Command("storeTitleAndWait", "aaa", "bbb"));
		commands.add(new Command("storeValue", "aaa", "bbb"));
		commands.add(new Command("storeValueAndWait", "aaa", "bbb"));
		commands.add(new Command("storeVisible", "aaa", "bbb"));
		commands.add(new Command("storeWhetherThisFrameMatchFrameExpression", "aaa", "bbb"));
		commands.add(new Command("storeWhetherThisWindowMatchWindowExpression", "aaa", "bbb"));
		commands.add(new Command("storeXpathCount", "aaa", "bbb"));
		commands.add(new Command("submit", "aaa", "bbb"));
		commands.add(new Command("submitAndWait", "aaa", "bbb"));
		commands.add(new Command("type", "aaa", "bbb"));
		commands.add(new Command("typeAndWait", "aaa", "bbb"));
		commands.add(new Command("typeKeys", "aaa", "bbb"));
		commands.add(new Command("typeKeysAndWait", "aaa", "bbb"));
		commands.add(new Command("uncheck", "aaa", "bbb"));
		commands.add(new Command("uncheckAndWait", "aaa", "bbb"));
		commands.add(new Command("useXpathLibrary", "aaa", "bbb"));
		commands.add(new Command("useXpathLibraryAndWait", "aaa", "bbb"));
		commands.add(new Command("verifyAlert", "aaa", "bbb"));
		commands.add(new Command("verifyAlertNotPresent", "aaa", "bbb"));
		commands.add(new Command("verifyAlertPresent", "aaa", "bbb"));
		commands.add(new Command("verifyAllButtons", "aaa", "bbb"));
		commands.add(new Command("verifyAllFields", "aaa", "bbb"));
		commands.add(new Command("verifyAllLinks", "aaa", "bbb"));
		commands.add(new Command("verifyAllWindowIds", "aaa", "bbb"));
		commands.add(new Command("verifyAllWindowNames", "aaa", "bbb"));
		commands.add(new Command("verifyAllWindowTitles", "aaa", "bbb"));
		commands.add(new Command("verifyAttribute", "aaa", "bbb"));
		commands.add(new Command("verifyAttributeFromAllWindows", "aaa", "bbb"));
		commands.add(new Command("verifyBodyText", "aaa", "bbb"));
		commands.add(new Command("verifyChecked", "aaa", "bbb"));
		commands.add(new Command("verifyConfirmation", "aaa", "bbb"));
		commands.add(new Command("verifyConfirmationNotPresent", "aaa", "bbb"));
		commands.add(new Command("verifyConfirmationPresent", "aaa", "bbb"));
		commands.add(new Command("verifyCookie", "aaa", "bbb"));
		commands.add(new Command("verifyCookieByName", "aaa", "bbb"));
		commands.add(new Command("verifyCookieNotPresent", "aaa", "bbb"));
		commands.add(new Command("verifyCookiePresent", "aaa", "bbb"));
		commands.add(new Command("verifyCssCount", "aaa", "bbb"));
		commands.add(new Command("verifyCursorPosition", "aaa", "bbb"));
		commands.add(new Command("verifyEditable", "aaa", "bbb"));
		commands.add(new Command("verifyElementHeight", "aaa", "bbb"));
		commands.add(new Command("verifyElementIndex", "aaa", "bbb"));
		commands.add(new Command("verifyElementNotPresent", "aaa", "bbb"));
		commands.add(new Command("verifyElementPositionLeft", "aaa", "bbb"));
		commands.add(new Command("verifyElementPositionTop", "aaa", "bbb"));
		commands.add(new Command("verifyElementPresent", "aaa", "bbb"));
		commands.add(new Command("verifyElementWidth", "aaa", "bbb"));
		commands.add(new Command("verifyEval", "aaa", "bbb"));
		commands.add(new Command("verifyExpression", "aaa", "bbb"));
		commands.add(new Command("verifyHtmlSource", "aaa", "bbb"));
		commands.add(new Command("verifyLocation", "aaa", "bbb"));
		commands.add(new Command("verifyMouseSpeed", "aaa", "bbb"));
		commands.add(new Command("verifyNotAlert", "aaa", "bbb"));
		commands.add(new Command("verifyNotAllButtons", "aaa", "bbb"));
		commands.add(new Command("verifyNotAllFields", "aaa", "bbb"));
		commands.add(new Command("verifyNotAllLinks", "aaa", "bbb"));
		commands.add(new Command("verifyNotAllWindowIds", "aaa", "bbb"));
		commands.add(new Command("verifyNotAllWindowNames", "aaa", "bbb"));
		commands.add(new Command("verifyNotAllWindowTitles", "aaa", "bbb"));
		commands.add(new Command("verifyNotAttribute", "aaa", "bbb"));
		commands.add(new Command("verifyNotAttributeFromAllWindows", "aaa", "bbb"));
		commands.add(new Command("verifyNotBodyText", "aaa", "bbb"));
		commands.add(new Command("verifyNotChecked", "aaa", "bbb"));
		commands.add(new Command("verifyNotConfirmation", "aaa", "bbb"));
		commands.add(new Command("verifyNotCookie", "aaa", "bbb"));
		commands.add(new Command("verifyNotCookieByName", "aaa", "bbb"));
		commands.add(new Command("verifyNotCssCount", "aaa", "bbb"));
		commands.add(new Command("verifyNotCursorPosition", "aaa", "bbb"));
		commands.add(new Command("verifyNotEditable", "aaa", "bbb"));
		commands.add(new Command("verifyNotElementHeight", "aaa", "bbb"));
		commands.add(new Command("verifyNotElementIndex", "aaa", "bbb"));
		commands.add(new Command("verifyNotElementPositionLeft", "aaa", "bbb"));
		commands.add(new Command("verifyNotElementPositionTop", "aaa", "bbb"));
		commands.add(new Command("verifyNotElementWidth", "aaa", "bbb"));
		commands.add(new Command("verifyNotEval", "aaa", "bbb"));
		commands.add(new Command("verifyNotExpression", "aaa", "bbb"));
		commands.add(new Command("verifyNotHtmlSource", "aaa", "bbb"));
		commands.add(new Command("verifyNotLocation", "aaa", "bbb"));
		commands.add(new Command("verifyNotMouseSpeed", "aaa", "bbb"));
		commands.add(new Command("verifyNotOrdered", "aaa", "bbb"));
		commands.add(new Command("verifyNotPrompt", "aaa", "bbb"));
		commands.add(new Command("verifyNotSelectOptions", "aaa", "bbb"));
		commands.add(new Command("verifyNotSelectedId", "aaa", "bbb"));
		commands.add(new Command("verifyNotSelectedIds", "aaa", "bbb"));
		commands.add(new Command("verifyNotSelectedIndex", "aaa", "bbb"));
		commands.add(new Command("verifyNotSelectedIndexes", "aaa", "bbb"));
		commands.add(new Command("verifyNotSelectedLabel", "aaa", "bbb"));
		commands.add(new Command("verifyNotSelectedLabels", "aaa", "bbb"));
		commands.add(new Command("verifyNotSelectedValue", "aaa", "bbb"));
		commands.add(new Command("verifyNotSelectedValues", "aaa", "bbb"));
		commands.add(new Command("verifyNotSomethingSelected", "aaa", "bbb"));
		commands.add(new Command("verifyNotSpeed", "aaa", "bbb"));
		commands.add(new Command("verifyNotTable", "aaa", "bbb"));
		commands.add(new Command("verifyNotText", "aaa", "bbb"));
		commands.add(new Command("verifyNotTitle", "aaa", "bbb"));
		commands.add(new Command("verifyNotValue", "aaa", "bbb"));
		commands.add(new Command("verifyNotVisible", "aaa", "bbb"));
		commands.add(new Command("verifyNotWhetherThisFrameMatchFrameExpression", "aaa", "bbb"));
		commands.add(new Command("verifyNotWhetherThisWindowMatchWindowExpression", "aaa", "bbb"));
		commands.add(new Command("verifyNotXpathCount", "aaa", "bbb"));
		commands.add(new Command("verifyOrdered", "aaa", "bbb"));
		commands.add(new Command("verifyPrompt", "aaa", "bbb"));
		commands.add(new Command("verifyPromptNotPresent", "aaa", "bbb"));
		commands.add(new Command("verifyPromptPresent", "aaa", "bbb"));
		commands.add(new Command("verifySelectOptions", "aaa", "bbb"));
		commands.add(new Command("verifySelectedId", "aaa", "bbb"));
		commands.add(new Command("verifySelectedIds", "aaa", "bbb"));
		commands.add(new Command("verifySelectedIndex", "aaa", "bbb"));
		commands.add(new Command("verifySelectedIndexes", "aaa", "bbb"));
		commands.add(new Command("verifySelectedLabel", "aaa", "bbb"));
		commands.add(new Command("verifySelectedLabels", "aaa", "bbb"));
		commands.add(new Command("verifySelectedValue", "aaa", "bbb"));
		commands.add(new Command("verifySelectedValues", "aaa", "bbb"));
		commands.add(new Command("verifySomethingSelected", "aaa", "bbb"));
		commands.add(new Command("verifySpeed", "aaa", "bbb"));
		commands.add(new Command("verifyTable", "aaa", "bbb"));
		commands.add(new Command("verifyText", "aaa", "bbb"));
		commands.add(new Command("verifyTextAndWait", "aaa", "bbb"));
		commands.add(new Command("verifyTextNotPresent", "aaa", "bbb"));
		commands.add(new Command("verifyTextPresent", "aaa", "bbb"));
		commands.add(new Command("verifyTitle", "aaa", "bbb"));
		commands.add(new Command("verifyTitleAndWait", "aaa", "bbb"));
		commands.add(new Command("verifyValue", "aaa", "bbb"));
		commands.add(new Command("verifyValueAndWait", "aaa", "bbb"));
		commands.add(new Command("verifyVisible", "aaa", "bbb"));
		commands.add(new Command("verifyWhetherThisFrameMatchFrameExpression", "aaa", "bbb"));
		commands.add(new Command("verifyWhetherThisWindowMatchWindowExpression", "aaa", "bbb"));
		commands.add(new Command("verifyXpathCount", "aaa", "bbb"));
		commands.add(new Command("waitForAlert", "aaa", "bbb"));
		commands.add(new Command("waitForAlertNotPresent", "aaa", "bbb"));
		commands.add(new Command("waitForAlertPresent", "aaa", "bbb"));
		commands.add(new Command("waitForAllButtons", "aaa", "bbb"));
		commands.add(new Command("waitForAllFields", "aaa", "bbb"));
		commands.add(new Command("waitForAllLinks", "aaa", "bbb"));
		commands.add(new Command("waitForAllWindowIds", "aaa", "bbb"));
		commands.add(new Command("waitForAllWindowNames", "aaa", "bbb"));
		commands.add(new Command("waitForAllWindowTitles", "aaa", "bbb"));
		commands.add(new Command("waitForAttribute", "aaa", "bbb"));
		commands.add(new Command("waitForAttributeFromAllWindows", "aaa", "bbb"));
		commands.add(new Command("waitForBodyText", "aaa", "bbb"));
		commands.add(new Command("waitForChecked", "aaa", "bbb"));
		commands.add(new Command("waitForCondition", "aaa", "bbb"));
		commands.add(new Command("waitForConfirmation", "aaa", "bbb"));
		commands.add(new Command("waitForConfirmationNotPresent", "aaa", "bbb"));
		commands.add(new Command("waitForConfirmationPresent", "aaa", "bbb"));
		commands.add(new Command("waitForCookie", "aaa", "bbb"));
		commands.add(new Command("waitForCookieByName", "aaa", "bbb"));
		commands.add(new Command("waitForCookieNotPresent", "aaa", "bbb"));
		commands.add(new Command("waitForCookiePresent", "aaa", "bbb"));
		commands.add(new Command("waitForCssCount", "aaa", "bbb"));
		commands.add(new Command("waitForCursorPosition", "aaa", "bbb"));
		commands.add(new Command("waitForEditable", "aaa", "bbb"));
		commands.add(new Command("waitForElementHeight", "aaa", "bbb"));
		commands.add(new Command("waitForElementIndex", "aaa", "bbb"));
		commands.add(new Command("waitForElementNotPresent", "aaa", "bbb"));
		commands.add(new Command("waitForElementPositionLeft", "aaa", "bbb"));
		commands.add(new Command("waitForElementPositionTop", "aaa", "bbb"));
		commands.add(new Command("waitForElementPresent", "aaa", "bbb"));
		commands.add(new Command("waitForElementWidth", "aaa", "bbb"));
		commands.add(new Command("waitForEval", "aaa", "bbb"));
		commands.add(new Command("waitForExpression", "aaa", "bbb"));
		commands.add(new Command("waitForFrameToLoad", "aaa", "bbb"));
		commands.add(new Command("waitForHtmlSource", "aaa", "bbb"));
		commands.add(new Command("waitForLocation", "aaa", "bbb"));
		commands.add(new Command("waitForMouseSpeed", "aaa", "bbb"));
		commands.add(new Command("waitForNotAlert", "aaa", "bbb"));
		commands.add(new Command("waitForNotAllButtons", "aaa", "bbb"));
		commands.add(new Command("waitForNotAllFields", "aaa", "bbb"));
		commands.add(new Command("waitForNotAllLinks", "aaa", "bbb"));
		commands.add(new Command("waitForNotAllWindowIds", "aaa", "bbb"));
		commands.add(new Command("waitForNotAllWindowNames", "aaa", "bbb"));
		commands.add(new Command("waitForNotAllWindowTitles", "aaa", "bbb"));
		commands.add(new Command("waitForNotAttribute", "aaa", "bbb"));
		commands.add(new Command("waitForNotAttributeFromAllWindows", "aaa", "bbb"));
		commands.add(new Command("waitForNotBodyText", "aaa", "bbb"));
		commands.add(new Command("waitForNotChecked", "aaa", "bbb"));
		commands.add(new Command("waitForNotConfirmation", "aaa", "bbb"));
		commands.add(new Command("waitForNotCookie", "aaa", "bbb"));
		commands.add(new Command("waitForNotCookieByName", "aaa", "bbb"));
		commands.add(new Command("waitForNotCssCount", "aaa", "bbb"));
		commands.add(new Command("waitForNotCursorPosition", "aaa", "bbb"));
		commands.add(new Command("waitForNotEditable", "aaa", "bbb"));
		commands.add(new Command("waitForNotElementHeight", "aaa", "bbb"));
		commands.add(new Command("waitForNotElementIndex", "aaa", "bbb"));
		commands.add(new Command("waitForNotElementPositionLeft", "aaa", "bbb"));
		commands.add(new Command("waitForNotElementPositionTop", "aaa", "bbb"));
		commands.add(new Command("waitForNotElementWidth", "aaa", "bbb"));
		commands.add(new Command("waitForNotEval", "aaa", "bbb"));
		commands.add(new Command("waitForNotExpression", "aaa", "bbb"));
		commands.add(new Command("waitForNotHtmlSource", "aaa", "bbb"));
		commands.add(new Command("waitForNotLocation", "aaa", "bbb"));
		commands.add(new Command("waitForNotMouseSpeed", "aaa", "bbb"));
		commands.add(new Command("waitForNotOrdered", "aaa", "bbb"));
		commands.add(new Command("waitForNotPrompt", "aaa", "bbb"));
		commands.add(new Command("waitForNotSelectOptions", "aaa", "bbb"));
		commands.add(new Command("waitForNotSelectedId", "aaa", "bbb"));
		commands.add(new Command("waitForNotSelectedIds", "aaa", "bbb"));
		commands.add(new Command("waitForNotSelectedIndex", "aaa", "bbb"));
		commands.add(new Command("waitForNotSelectedIndexes", "aaa", "bbb"));
		commands.add(new Command("waitForNotSelectedLabel", "aaa", "bbb"));
		commands.add(new Command("waitForNotSelectedLabels", "aaa", "bbb"));
		commands.add(new Command("waitForNotSelectedValue", "aaa", "bbb"));
		commands.add(new Command("waitForNotSelectedValues", "aaa", "bbb"));
		commands.add(new Command("waitForNotSomethingSelected", "aaa", "bbb"));
		commands.add(new Command("waitForNotSpeed", "aaa", "bbb"));
		commands.add(new Command("waitForNotTable", "aaa", "bbb"));
		commands.add(new Command("waitForNotText", "aaa", "bbb"));
		commands.add(new Command("waitForNotTitle", "aaa", "bbb"));
		commands.add(new Command("waitForNotValue", "aaa", "bbb"));
		commands.add(new Command("waitForNotVisible", "aaa", "bbb"));
		commands.add(new Command("waitForNotWhetherThisFrameMatchFrameExpression", "aaa", "bbb"));
		commands.add(new Command("waitForNotWhetherThisWindowMatchWindowExpression", "aaa", "bbb"));
		commands.add(new Command("waitForNotXpathCount", "aaa", "bbb"));
		commands.add(new Command("waitForOrdered", "aaa", "bbb"));
		commands.add(new Command("waitForPageToLoad", "aaa", "bbb"));
		commands.add(new Command("waitForPopUp", "aaa", "bbb"));
		commands.add(new Command("waitForPrompt", "aaa", "bbb"));
		commands.add(new Command("waitForPromptNotPresent", "aaa", "bbb"));
		commands.add(new Command("waitForPromptPresent", "aaa", "bbb"));
		commands.add(new Command("waitForSelectOptions", "aaa", "bbb"));
		commands.add(new Command("waitForSelectedId", "aaa", "bbb"));
		commands.add(new Command("waitForSelectedIds", "aaa", "bbb"));
		commands.add(new Command("waitForSelectedIndex", "aaa", "bbb"));
		commands.add(new Command("waitForSelectedIndexes", "aaa", "bbb"));
		commands.add(new Command("waitForSelectedLabel", "aaa", "bbb"));
		commands.add(new Command("waitForSelectedLabels", "aaa", "bbb"));
		commands.add(new Command("waitForSelectedValue", "aaa", "bbb"));
		commands.add(new Command("waitForSelectedValues", "aaa", "bbb"));
		commands.add(new Command("waitForSomethingSelected", "aaa", "bbb"));
		commands.add(new Command("waitForSpeed", "aaa", "bbb"));
		commands.add(new Command("waitForTable", "aaa", "bbb"));
		commands.add(new Command("waitForText", "aaa", "bbb"));
		commands.add(new Command("waitForTextNotPresent", "aaa", "bbb"));
		commands.add(new Command("waitForTextPresent", "aaa", "bbb"));
		commands.add(new Command("waitForTitle", "aaa", "bbb"));
		commands.add(new Command("waitForValue", "aaa", "bbb"));
		commands.add(new Command("waitForVisible", "aaa", "bbb"));
		commands.add(new Command("waitForWhetherThisFrameMatchFrameExpression", "aaa", "bbb"));
		commands.add(new Command("waitForWhetherThisWindowMatchWindowExpression", "aaa", "bbb"));
		commands.add(new Command("waitForXpathCount", "aaa", "bbb"));
		commands.add(new Command("waitPreparation", "aaa", "bbb"));
		commands.add(new Command("waitPreparationAndWait", "aaa", "bbb"));
		commands.add(new Command("windowFocus", "aaa", "bbb"));
		commands.add(new Command("windowFocusAndWait", "aaa", "bbb"));
		commands.add(new Command("windowMaximize", "aaa", "bbb"));
		commands.add(new Command("windowMaximizeAndWait", "aaa", "bbb"));
		
		for (Command c : commands) {
			System.out.println(SeleniumIdeFormatter.getInstance().formatCommand(c));
		}
	}
}
