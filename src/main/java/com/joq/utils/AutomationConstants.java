package com.joq.utils;

public class AutomationConstants {
	public static final String PERFROMANCE_TEST_CONFIG = "performance_test_config";
	public static final String EMAIL_CONFIG = "email_config";
	public static final String JMETER_PATH = "jmeterPath";
	public static final String API_FILE_PATH = "apiFilePath";
	public static final String NEED_REMOTE_EXECUTIONS = "needRemoteHostsExecution";
	public static final String REMOTE_HOSTS = "remoteHosts";
	public static final String YOUR_MAIL = "yourMail";
	public static final String YOUR_MAIL_PASSWORD = "yourMailPassword";
	public static final String RECIPIENT_MAIL_LISTS = "recipientMaillists";
	public static final String NEED_EMAIL_REPORT = "needEmailReport";
	public static final String TEST_DATA_PATH = "./src/test/resources/TestData/";
	public static final String JMX_SCRIPTS_PATH = "./src/test/resources/PerformanceTesting/JMX_Scripts/";

	// ===========> Exception Messages
	public static final String CAUSE = "Cause of the Exception : ";
	public static final String EXCEPTION_MESSAGE_EXCEL_FILE_PATH = "Specify test data Excel file path in automation_test_config.properties file";
	public static final String EXCEPTIION_EXCEL_SHEETNAME = "Can't read data from specified sheet, check sheet name";
	public static final String EXCEPTIION_EXCEL_COLUMN_NO = "Specify column index greater than zero";
	public static final String EXCEPTIION_EXCEL_ROW_NO = "Specify row index greater than zero";
	public static final String EXCEPTIION_EXCEL_COLUMN_NAME = "Excel column with given name not found, check the name";
	public static final String EXCEPTIION_EXCEL_PATH = "Give excel file path as argument";
	public static final String EXCEPTIION_EXCEL_FILE = "Please provide excel file with .xlsx or .xls format";
}