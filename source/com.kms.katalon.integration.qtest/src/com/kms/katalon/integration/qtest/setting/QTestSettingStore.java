package com.kms.katalon.integration.qtest.setting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.kms.katalon.core.setting.PropertySettingStoreUtil;
import com.kms.katalon.core.setting.ReportFormatType;
import com.kms.katalon.integration.qtest.credential.IQTestCredential;
import com.kms.katalon.integration.qtest.credential.IQTestToken;

public class QTestSettingStore {
    private static final String FILE_NAME = "com.kms.katalon.integration.qtest";
    public static final String TOKEN_PROPERTY = "token";
    public static final String USERNAME_PROPERTY = "username";
    public static final String PASSWORD_PROPERTY = "password";
    public static final String SERVER_URL_PROPERTY = "serverUrl";
    public static final String AUTO_SUBMIT_RESULT_PROPERTY = "autoSubmitResult";
    public static final String SUBMIT_RESULT_TO_LATEST_VERSION = "submitResultToLatestVersion";
    public static final String ENABLE_INTEGRATION_PROPERTY = "enableIntegration";
    public static final String SEND_ATTACHMENTS_PROPERTY = "sendAttachments";
    public static final String FIRST_TIME_USING = "firstTimeUsing";
    public static final String REPORT_FORMAT = "reportFormat";
    public static final String QTEST_VERSION_PROPERTY = "version";

    public static File getPropertyFile(String projectDir) throws IOException {
        File configFile = new File(projectDir + File.separator
                + PropertySettingStoreUtil.INTERNAL_SETTING_ROOT_FOLDER_NAME + File.separator + FILE_NAME
                + PropertySettingStoreUtil.PROPERTY_FILE_EXENSION);
        if (!configFile.exists()) {
            configFile.createNewFile();
        }
        return configFile;
    }

    /* package */static String getRawToken(String projectDir) {
        try {
            return PropertySettingStoreUtil.getPropertyValue(TOKEN_PROPERTY, getPropertyFile(projectDir));
        } catch (IOException e) {
            return "";
        }
    }

    public static void saveToken(IQTestToken token, String projectDir) throws IOException {
        String rawToken = token != null ? token.getRawToken() : "";
        PropertySettingStoreUtil.addNewProperty(TOKEN_PROPERTY, rawToken, getPropertyFile(projectDir));
    }

    public static String getUsername(String projectDir) {
        try {
            return PropertySettingStoreUtil.getPropertyValue(USERNAME_PROPERTY, getPropertyFile(projectDir));
        } catch (IOException e) {
            return "";
        }
    }

    public static String getPassword(String projectDir) {
        try {
            return PropertySettingStoreUtil.getPropertyValue(PASSWORD_PROPERTY, getPropertyFile(projectDir));
        } catch (IOException e) {
            return "";
        }
    }

    public static String getServerUrl(String projectDir) {
        try {
            return PropertySettingStoreUtil.getPropertyValue(SERVER_URL_PROPERTY, getPropertyFile(projectDir));
        } catch (IOException e) {
            return "";
        }
    }

    public static QTestVersion getQTestVersion(String projectDir) {
        try {
            String versionName = PropertySettingStoreUtil.getPropertyValue(QTEST_VERSION_PROPERTY,
                    getPropertyFile(projectDir));
            if (StringUtils.isBlank(versionName)) {
                if (!StringUtils.isBlank(getRawToken(projectDir))) {
                    return QTestVersion.V6;
                } else {
                    return QTestVersion.getLastest();
                }
            } else {
                return QTestVersion.valueOf(versionName);
            }
        } catch (IOException e) {
            return QTestVersion.getLastest();
        }
    }

    public static void saveUserProfile(IQTestCredential credential, String projectDir) throws IOException {
        saveToken(credential.getToken(), projectDir);
        
        PropertySettingStoreUtil.addNewProperty(USERNAME_PROPERTY, credential.getUsername(),
                getPropertyFile(projectDir));
        PropertySettingStoreUtil.addNewProperty(PASSWORD_PROPERTY, credential.getPassword(),
                getPropertyFile(projectDir));
        PropertySettingStoreUtil.addNewProperty(QTEST_VERSION_PROPERTY, credential.getVersion().name(),
                getPropertyFile(projectDir));

        String savedServerUrl = credential.getServerUrl();
        while (!savedServerUrl.isEmpty() && savedServerUrl.endsWith("/")) {
            savedServerUrl = savedServerUrl.substring(0, savedServerUrl.length() - 1);
        }

        PropertySettingStoreUtil.addNewProperty(SERVER_URL_PROPERTY, savedServerUrl, getPropertyFile(projectDir));
    }

    public static boolean isAutoSubmitResultActive(String projectDir) {
        try {
            return Boolean.parseBoolean(PropertySettingStoreUtil.getPropertyValue(AUTO_SUBMIT_RESULT_PROPERTY,
                    getPropertyFile(projectDir)));
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isSubmitResultToLatestVersionActive(String projectDir) {
        try {
            return Boolean.parseBoolean(PropertySettingStoreUtil.getPropertyValue(SUBMIT_RESULT_TO_LATEST_VERSION,
                    getPropertyFile(projectDir)));
        } catch (IOException e) {
            return false;
        }
    }
    
    public static boolean isIntegrationActive(String projectDir) {
        try {
            return Boolean.parseBoolean(PropertySettingStoreUtil.getPropertyValue(ENABLE_INTEGRATION_PROPERTY,
                    getPropertyFile(projectDir)));
        } catch (IOException e) {
            return false;
        }
    }

    public static void saveAutoSubmit(boolean autoSubmit, String projectDir) throws IOException {
        PropertySettingStoreUtil.addNewProperty(AUTO_SUBMIT_RESULT_PROPERTY, Boolean.toString(autoSubmit),
                getPropertyFile(projectDir));
    }

    public static void saveSubmitToLatestVersion(boolean submitToLatest, String projectDir) throws IOException {
        PropertySettingStoreUtil.addNewProperty(SUBMIT_RESULT_TO_LATEST_VERSION, Boolean.toString(submitToLatest),
                getPropertyFile(projectDir));
    }
    
    public static void saveEnableIntegration(boolean isIntegration, String projectDir) throws IOException {
        PropertySettingStoreUtil.addNewProperty(ENABLE_INTEGRATION_PROPERTY, Boolean.toString(isIntegration),
                getPropertyFile(projectDir));
    }

    public static List<QTestAttachmentSendingType> getAttachmentSendingTypes(String projectDir) {
        try {
            List<QTestAttachmentSendingType> attachmentSendingTypes = new ArrayList<QTestAttachmentSendingType>();
            String sendingTypePropertyString = PropertySettingStoreUtil.getPropertyValue(SEND_ATTACHMENTS_PROPERTY,
                    getPropertyFile(projectDir));

            if (sendingTypePropertyString == null) {
                return Arrays.asList(QTestAttachmentSendingType.values());
            }

            if (sendingTypePropertyString.isEmpty()) {
                return attachmentSendingTypes;
            }

            for (String sendingTypeName : sendingTypePropertyString.trim().split(",")) {
                attachmentSendingTypes.add(QTestAttachmentSendingType.valueOf(sendingTypeName.trim()));
            }

            return attachmentSendingTypes;
        } catch (IOException | IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    public static void saveAttachmentSendingType(List<QTestAttachmentSendingType> types, String projectDir) {
        try {
            StringBuilder attachmentStringBuilder = new StringBuilder();
            for (int index = 0; index < types.size(); index++) {
                attachmentStringBuilder.append(types.get(index).name());
                if (index != types.size() - 1) {
                    attachmentStringBuilder.append(", ");
                }
            }
            PropertySettingStoreUtil.addNewProperty(SEND_ATTACHMENTS_PROPERTY, attachmentStringBuilder.toString(),
                    getPropertyFile(projectDir));
        } catch (IOException | IllegalArgumentException e) {
            // Do nothing
        }
    }

    /**
     * @param projectDir
     *            folder location of the current project
     * @return true if it is the first time users use this feature.
     */
    public static boolean isTheFirstTime(String projectDir) {
        try {
            String value = PropertySettingStoreUtil.getPropertyValue(FIRST_TIME_USING, getPropertyFile(projectDir));
            if (value == null) {
                return true;
            }
            return Boolean.parseBoolean(value);
        } catch (IOException e) {
            return true;
        }
    }

    public static void usedSetupWizard(String projectDir) {
        try {
            PropertySettingStoreUtil.addNewProperty(FIRST_TIME_USING, Boolean.toString(false),
                    getPropertyFile(projectDir));
        } catch (IOException | IllegalArgumentException e) {
        }
    }

    public static List<ReportFormatType> getFormatReportTypes(String projectDir) {
        try {
            List<ReportFormatType> reportFormatTypes = new ArrayList<ReportFormatType>();
            String formatPropertyString = PropertySettingStoreUtil.getPropertyValue(REPORT_FORMAT,
                    getPropertyFile(projectDir));

            // By default, select them all.
            if (formatPropertyString == null) {
                return Arrays.asList(ReportFormatType.HTML, ReportFormatType.LOG);
            }

            if (formatPropertyString.isEmpty()) {
                return reportFormatTypes;
            }

            for (String sendingTypeName : formatPropertyString.trim().split(",")) {
                reportFormatTypes.add(ReportFormatType.valueOf(sendingTypeName.trim()));
            }

            return reportFormatTypes;
        } catch (IOException | IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    public static void saveFormatReportTypes(List<ReportFormatType> types, String projectDir) {
        try {
            StringBuilder attachmentStringBuilder = new StringBuilder();
            for (int index = 0; index < types.size(); index++) {
                attachmentStringBuilder.append(types.get(index).name());
                if (index != types.size() - 1) {
                    attachmentStringBuilder.append(", ");
                }
            }
            PropertySettingStoreUtil.addNewProperty(REPORT_FORMAT, attachmentStringBuilder.toString(),
                    getPropertyFile(projectDir));
        } catch (IOException | IllegalArgumentException e) {
            // Do nothing
        }
    }
}
