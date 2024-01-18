package com.joq.keywords;

import static org.apache.jmeter.JMeter.JMETER_REPORT_OUTPUT_DIR_PROPERTY;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.jmeter.assertions.DurationAssertion;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.assertions.gui.DurationAssertionGui;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.joq.utils.AutomationConstants;
import com.joq.utils.Utilities;

public class PerformanceActions {
	static HashTree testPlanTree = new HashTree();
	static HashTree insideRequest;
	static HTTPSamplerProxy HTTPSample = null;

	static int TestCaseID_Index = 0;
	static int TestCaseComment_Index = 1;
	static int ServerIP_Index = 2;
	static int ResourcePath_Index = 3;
	static int RequestType_Index = 4;
	static int PortNo_Index = 5;
	static int Header_Index = 6;
	static int RequestBody_Index = 7;
	static int CSVPath_Index = 8;
	static int CSVParams_Index = 9;
	static int ResourceParamsName_Index = 10;
	static int ResourceParamsValue_Index = 11;
	static int ExpectedResponseCode_Index = 12;
	static int ExpectedResponseMessage_Index = 13;
	static int ExpectedResponseBody_Index = 14;
	static int ExpectedResponseTime_Index = 15;
	static int WebServiceType_Index = 16;
	static int GlobalProp_Index = 17;
	static int UseMultipart_Index = 18;
	static int UseBrowserCompHeaders_Index = 19;
	static int SendFiles_Index = 20;
	static int AttachmentFilePath_Index = 21;
	static int AttachmentParameterName_Index = 22;
	static int AttachmentMMEType_Index = 23;
	static String TestCaseID;
	static String ReqType;
	static String ServerIP;
	static String PortNo;
	static String ResourcePath;
	static String Header;
	static String RequestBody;
	static String ExpectedResponseCode;
	static String ExpectedResponseMessage;
	static String ExpectedResponseBody;
	static String ExpectedResponseTime;
	static String UseMultipart;
	static String UseBrowserCompHeaders;
	static String SendFiles;
	static String AttachmentFilePath;
	static String AttachmentParameterName;
	static String AttachmentMMEType;
	static String CSVParams;
	static String CSVPath;
	static String ResourceParamsName;
	static String ResourceParamsValue;
	static String WebServiceType;
	static String GlobalProp;
	static String TestCaseComment;

	/**
	 * Method to create Test Plan in JMeter
	 * 
	 * @author sanoj.swaminathan
	 * @since 07-Jan-2021
	 * @param testPlanName
	 * @return
	 */
	public static TestPlan createTestPlan(String testPlanName) {
		TestPlan testPlan = null;
		try {
			testPlan = new TestPlan(testPlanName);
			testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
			testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
			testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
			testPlanTree.add(testPlan);
		} catch (Exception lException) {
			lException.printStackTrace();
		}
		return testPlan;
	}

	/**
	 * Method to create Thread Group and add it into the Test Plan tree
	 * 
	 * @author sanoj.swaminathan
	 * @since 30-Jan-2021
	 * @param testPlan
	 * @param threadGroupName
	 * @param numberOfUsers
	 * @param rampUpTime
	 * @param loopCount
	 * @return
	 */
	public static HashTree createThreadGroup(TestPlan testPlan, String threadGroupName, int numberOfUsers,
			int rampUpTime, int loopCount) {
		ThreadGroup threadGroup = null;
		HashTree threadGroupHashTree = null;
		try {
			threadGroup = new ThreadGroup();
			threadGroup.setName(threadGroupName);
			threadGroup.setNumThreads(numberOfUsers);
			threadGroup.setRampUp(rampUpTime);
			threadGroup.setSamplerController(createLoopContrloller(loopCount));
			threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
			threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
			threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return threadGroupHashTree;
	}

	/**
	 * Method to create GET HTTP Sampler
	 * 
	 * @author sanoj.swaminathan
	 * @since 31-Jan-2021
	 * @param threadGroupName
	 * @param samplerName
	 * @param serverIP
	 * @param portNo
	 * @param resourcePath
	 * @param reqParamName
	 * @param reqParamValue
	 * @return
	 */
	public static HashTree createGETHTTPSampler(HashTree threadGroupName, String samplerName, String serverIP,
			String portNo, String resourcePath, String reqParamName, String reqParamValue) {
		try {
			HTTPSample = new HTTPSamplerProxy();
			HTTPSample.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
			HTTPSample.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
			HTTPSample.setName(samplerName);
			HTTPSample.setProtocol("https");
			HTTPSample.setDomain(serverIP.replace("http://", "").replace("/", ""));
			if (portNo.length() != 0) {
				HTTPSample.setPort(Integer.parseInt(portNo.replace(".0", "")));
			}
			HTTPSample.setPath(resourcePath);
			HTTPSample.setMethod("GET");
			// Setting Request Parameters
			HTTPSample.addEncodedArgument(reqParamName, reqParamValue, "");
			insideRequest = threadGroupName.add(HTTPSample);
		} catch (Exception e) {
		}
		return insideRequest;
	}

	/**
	 * Method to create POST HTTP Sampler with request body
	 * 
	 * @author sanoj.swaminathan
	 * @since 31-Jan-2021
	 * @param threadGroupName
	 * @param samplerName
	 * @param serverIP
	 * @param portNo
	 * @param resourcePath
	 * @param requestBody
	 * @return
	 */
	public static HashTree createPOSTHTTPSampler(HashTree threadGroupName, String samplerName, String serverIP,
			String portNo, String resourcePath, String requestBody) {
		try {
			HTTPSample = new HTTPSamplerProxy();
			HTTPSample.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
			HTTPSample.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
			HTTPSample.setName(samplerName);
			HTTPSample.setDomain(serverIP.replace("http://", "").replace("/", ""));
			if (portNo.length() != 0) {
				HTTPSample.setPort(Integer.parseInt(portNo.replace(".0", "")));
			}
			HTTPSample.setPath(resourcePath);
			HTTPSample.setMethod("POST");

			HTTPSample.setFollowRedirects(true);
			// Setting POST request body
			HTTPSample.addNonEncodedArgument("HTTPArgument", requestBody, "");
			HTTPSample.setPostBodyRaw(true);

			// Use multipart/form-data for HTTP POST
			HTTPSample.setDoMultipart(true);
			HTTPSample.setDoBrowserCompatibleMultipart(true);
			insideRequest = threadGroupName.add(HTTPSample);
		} catch (Exception e) {
		}
		return insideRequest;
	}

	/**
	 * Method to create POST HTTP Sampler with request body and upload file
	 * 
	 * @author sanoj.swaminathan
	 * @since 31-Jan-2021
	 * @param threadGroupName
	 * @param samplerName
	 * @param serverIP
	 * @param portNo
	 * @param resourcePath
	 * @param requestBody
	 * @param filePathToUpload
	 * @param attachmentParameterName
	 * @param attachmentMIMEType
	 * @return
	 */
	public static HashTree createPOSTHTTPSampler(HashTree threadGroupName, String samplerName, String serverIP,
			String portNo, String resourcePath, String requestBody, String filePathToUpload,
			String attachmentParameterName, String attachmentMIMEType) {
		try {
			HTTPSample = new HTTPSamplerProxy();
			HTTPSample.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
			HTTPSample.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
			HTTPSample.setName(samplerName);
			HTTPSample.setDomain(serverIP.replace("http://", "").replace("/", ""));
			if (portNo.length() != 0) {
				HTTPSample.setPort(Integer.parseInt(portNo.replace(".0", "")));
			}
			HTTPSample.setPath(resourcePath);
			HTTPSample.setMethod("POST");

			HTTPSample.setFollowRedirects(true);
			// Setting POST request body
			HTTPSample.addNonEncodedArgument("HTTPArgument", requestBody, "");
			HTTPSample.setPostBodyRaw(true);

			// Use multipart/form-data for HTTP POST
			HTTPSample.setDoMultipart(true);
			HTTPSample.setDoBrowserCompatibleMultipart(true);
			insideRequest = threadGroupName.add(HTTPSample);

			// Upload File
			HTTPSample.setHTTPFiles(new HTTPFileArg[] {
					new HTTPFileArg(filePathToUpload, attachmentParameterName, attachmentMIMEType) });
		} catch (Exception e) {
		}
		return insideRequest;
	}

	/**
	 * Create header manager for requests/samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 17-Jan-2021
	 * @param threadGroupOrSampler
	 * @param headerData
	 * @param headerName
	 * @return
	 */
	public static HeaderManager createHeaderManager(HashTree threadGroupOrSampler, String headerData,
			String headerName) {
		HeaderManager HeaderMgr = new HeaderManager();
		HeaderMgr.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
		HeaderMgr.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
		HeaderMgr.setName(headerName + "_Header");
		if (headerData.trim() != "") {
			String lines[] = headerData.trim().split("\\r?\\n");
			for (int i = 0; i < lines.length; i++) {
				Header requestHead = new Header();
				String headerval = (lines[i].trim());
				String[] result = headerval.split(":");
				requestHead.setName(result[0]);
				requestHead.setValue(result[1]);
				HeaderMgr.add(requestHead);
			}
		}
		threadGroupOrSampler.add(HeaderMgr);
		return HeaderMgr;
	}

	/**
	 * Creating Response Code Assertion and adding to samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 30-Jan-2021
	 * @param respCode
	 * @param patternMatchingRule
	 * @return
	 */
	public static ResponseAssertion createReponseCodeAssertion(HashTree threadGroupOrSampler, String respCode,
			String patternMatchingRule, String assertionName) {
		if (respCode.trim() != "") {
			ResponseAssertion responseCode = new ResponseAssertion();
			responseCode.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
			responseCode.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
			responseCode.setProperty(TestElement.ENABLED, true);
			responseCode.setName(assertionName);
			responseCode.setScopeParent();
			responseCode.setTestFieldResponseCode();
			responseCode = getPatternMatchingRule(responseCode, patternMatchingRule);
			responseCode.addTestString(respCode.trim().replace(".0", ""));
			threadGroupOrSampler.add(responseCode);
			return responseCode;
		}
		return null;
	}

	/**
	 * Creating Response Message Assertion and adding to samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 31-Jan-2021
	 * @param threadGroupOrSampler
	 * @param respMessage
	 * @param patternMatchingRule
	 * @param assertionName
	 * @return
	 */
	public static ResponseAssertion createResponseMessageAssertion(HashTree threadGroupOrSampler, String respMessage,
			String patternMatchingRule, String assertionName) {
		if (respMessage.trim() != "") {
			ResponseAssertion responseMessage = new ResponseAssertion();
			responseMessage.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
			responseMessage.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
			responseMessage.setProperty(TestElement.ENABLED, true);
			responseMessage.setName(assertionName);
			responseMessage.setScopeParent();
			responseMessage.setTestFieldResponseMessage();
			responseMessage = getPatternMatchingRule(responseMessage, patternMatchingRule);
			responseMessage.addTestString(respMessage.trim());
			threadGroupOrSampler.add(responseMessage);
			return responseMessage;
		}
		return null;
	}

	/**
	 * Creating Response Body Assertion and adding to samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 30-Jan-2021
	 * @param respBody
	 * @param patternMatchingRule
	 * @param assertionName
	 * @return
	 */
	public static ResponseAssertion createResponseBodyAssertion(HashTree threadGroupOrSampler, String respBody,
			String patternMatchingRule, String assertionName) {
		if (respBody.trim() != "") {
			ResponseAssertion responseBody = new ResponseAssertion();
			responseBody.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
			responseBody.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
			responseBody.setProperty(TestElement.ENABLED, true);
			responseBody.setName(assertionName);
			responseBody.setScopeParent();
			responseBody.setTestFieldResponseData();
			responseBody = getPatternMatchingRule(responseBody, patternMatchingRule);
			responseBody.addTestString(respBody.trim());
			threadGroupOrSampler.add(responseBody);
			return responseBody;
		}
		return null;
	}

	/**
	 * Creating Response Time Assertion and adding to samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 30-Jan-2021
	 * @param durationTime
	 * @param assertionName
	 * @return
	 */
	public static DurationAssertion createDurationAssertion(HashTree threadGroupOrSampler, String durationTime,
			String assertionName) {
		if (durationTime.trim() != "") {
			DurationAssertion responseDuration = new DurationAssertion();
			responseDuration.setProperty(TestElement.TEST_CLASS, DurationAssertion.class.getName());
			responseDuration.setProperty(TestElement.GUI_CLASS, DurationAssertionGui.class.getName());
			responseDuration.setProperty(TestElement.ENABLED, true);
			responseDuration.setName(assertionName);
			responseDuration.setScopeParent();
			responseDuration.setProperty("DurationAssertion.duration", durationTime.trim().replace(".0", ""));
			threadGroupOrSampler.add(responseDuration);
			return responseDuration;
		}
		return null;
	}

	/**
	 * Create CSV Data Set and adding to samplers. The test data CSV file should be
	 * added in "TestData" folder inside "src/test/resources"
	 * 
	 * @author sanoj.swaminathan
	 * @since 30-Jan-2021
	 * @param threadGroupOrSampler
	 * @param filePath
	 * @param variableNames
	 * @param dataSetName
	 * @return
	 * @throws IOException
	 */
	public static CSVDataSet createCSVDataSet(HashTree threadGroupOrSampler, String fileName, String variableNames,
			String dataSetName) throws IOException {
		CSVDataSet sampleCSV = new CSVDataSet();
		sampleCSV.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
		sampleCSV.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
		sampleCSV.setName(dataSetName + "_CSV Data Set");
		sampleCSV.setDelimiter(",");
		sampleCSV.setProperty(TestElement.ENABLED, true);
		sampleCSV.setProperty("delimiter", ",");
		sampleCSV.setProperty("fileEncoding", "");
		sampleCSV.setProperty("quotedData", false);
		sampleCSV.setProperty("recycle", false);
		sampleCSV.setProperty("stopThread", true);
		sampleCSV.setProperty("shareMode", "shareMode.all");

		String actualFilePath = null;
		String testDataFilePath = System.getProperty("user.dir") + AutomationConstants.TEST_DATA_PATH + fileName
				+ ".csv";
		File file = new File(testDataFilePath);
		if (file.exists()) {
			actualFilePath = testDataFilePath;
		}
		sampleCSV.setProperty("filename", actualFilePath.trim());
		sampleCSV.setProperty("variableNames", variableNames.trim());
		// This code returns the number of rows in CSV file
		int csvcount = getCSVRowCount(actualFilePath.trim());
		System.out.println("CSV data count is " + csvcount);
		threadGroupOrSampler.add(sampleCSV);
		return sampleCSV;
	}

	/**
	 * Method to create Bean shell assertion/Regular expression extractor for each
	 * global property
	 * 
	 * @author sanoj.swaminathan
	 * @since 31-Jan-2021
	 * @param globalPropertyValues
	 * @return
	 * @throws IOException
	 */
	public static BeanShellPostProcessor createBeanShellAssertion(HashTree threadGroupOrSampler,
			String globalPropertyValues) throws IOException {
		String globalProperty[] = globalPropertyValues.split(",");
		BeanShellPostProcessor beanShellProcessor = null;
		try {
			for (int i = 0; i < globalProperty.length; i++) {
				beanShellProcessor = extractJsonTagValue(globalProperty[i].trim());
				threadGroupOrSampler.add(beanShellProcessor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return beanShellProcessor;
	}

	/**
	 * Load samplers for perform test. You have to map all the API details in the
	 * template SampleAPIs.xlsx that available in
	 * \src\test\resources\PerformanceTesting\SamplerTemplate location
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param testPlanName
	 * @param threadGroupName
	 * @param sheetNameofApiFile
	 * @return
	 * @throws Exception
	 */
	public static void loadSamplers(String testPlanName, String threadGroupName, String sheetNameofApiFile)
			throws Exception {
		HTTPSamplerProxy HTTPSample = null;
		HashTree testPlanTree = new HashTree();
		int rowStart = 1;
		int columnStart = 0;
		int Totalcolumns;
		int Size = 0;

		String apiFilePath = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
				AutomationConstants.API_FILE_PATH);

		XSSFSheet sheet = getExcelSheetRef(apiFilePath, sheetNameofApiFile);
		Totalcolumns = sheet.getRow(0).getPhysicalNumberOfCells();
		Size = Totalcolumns + 1;
		String[] TC_Data = new String[Size];

		int rowEnd = sheet.getPhysicalNumberOfRows();

		// Creating Test Plan and ThreadGroup

		TestPlan testPlan = new TestPlan(testPlanName);
		testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
		testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
		testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
		// Construct Test Plan from previously initialized elements
		testPlanTree.add(testPlan);
		HashTree threadGroupHashTree = testPlanTree.add(testPlan, createThreadGroup(threadGroupName));
		HashTree insideRequest;

		for (int Currentrow = rowStart; Currentrow < rowEnd; Currentrow++) {

			// Checking end of InputExcelfile i.e if RequestType column data is
			// blank.
			if (ReadCell(sheet, Currentrow, RequestType_Index).length() == 0) {
				break;
			}
			// Checking if current row has Testcase ID; New ThreadGroup needs to
			// be created for each Testcase.
			if (ReadCell(sheet, Currentrow, TestCaseID_Index).length() > 0) {
				// Checking if "Include for execution ?" value is "Yes"; If No,
				// then the Testcase is skipped.
				for (int Loopvar = 0; Loopvar < Size; Loopvar++) {
					TC_Data[Loopvar] = "";
				}
				for (int Currentcolumn = columnStart; Currentcolumn < Totalcolumns; Currentcolumn++) {
					TC_Data[Currentcolumn] = ReadCell(sheet, Currentrow, Currentcolumn);
				}
				loadExcelData(TC_Data);
			}

			HTTPSample = new HTTPSamplerProxy();
			HTTPSample.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
			HTTPSample.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
			HTTPSample.setName(TestCaseID);
			HTTPSample.setComment(TestCaseComment);
			HTTPSample.setDomain(ServerIP.replace("http://", "").replace("/", ""));
			if (PortNo.length() != 0) {
				HTTPSample.setPort(Integer.parseInt(PortNo.replace(".0", "")));
			}
			HTTPSample.setPath(ResourcePath);
			HTTPSample.setMethod(ReqType.toUpperCase());

			// Setting Request Parameters
			if (ResourceParamsName != "" && ResourceParamsValue != "") {
				HTTPSample.addEncodedArgument(ResourceParamsName, ResourceParamsValue, "");
			}

			// Setting POST request body
			if (RequestBody != "") {
				HTTPSample.addNonEncodedArgument("HTTPArgument", RequestBody, "");
				HTTPSample.setPostBodyRaw(true);
			}

			// Use multipart/form-data for HTTP POST
			if (UseMultipart.toUpperCase().equals("YES")) {
				HTTPSample.setDoMultipart(true);
			}

			// Use browser Compatible headers when using multipart/form-data
			if (UseBrowserCompHeaders.toUpperCase().equals("YES")) {
				HTTPSample.setDoBrowserCompatibleMultipart(true);
			}

			// Send attachments if provided
			if (SendFiles.toUpperCase().trim().equals("YES")) {
				HTTPSample.setHTTPFiles(new HTTPFileArg[] {
						new HTTPFileArg(AttachmentFilePath, AttachmentParameterName, AttachmentMMEType) });
			}
			insideRequest = threadGroupHashTree.add(HTTPSample);

			if (Header != "") {
				HeaderManager requestHeaderManager = createHeaderManager(Header, TestCaseID);
				insideRequest.add(requestHeaderManager);
			}

			if (ExpectedResponseCode != "") {
				ResponseAssertion ResponseCodeAssertion = createResponseCodeAssertion(ExpectedResponseCode, TestCaseID);
				insideRequest.add(ResponseCodeAssertion);
			}

			if (ExpectedResponseMessage != "") {
				ResponseAssertion ResponseMessageAssertion = createResponseMessageAssertion(ExpectedResponseMessage,
						TestCaseID);
				insideRequest.add(ResponseMessageAssertion);
			}
			if (ExpectedResponseBody != "") {
				ResponseAssertion ResponseBodyAssertion = createResponseBodyAssertion(ExpectedResponseBody, TestCaseID);
				insideRequest.add(ResponseBodyAssertion);
			}
			if (ExpectedResponseTime != "") {
				DurationAssertion ResponseDurationAssertion = createDurationAssertion(ExpectedResponseTime, TestCaseID);
				insideRequest.add(ResponseDurationAssertion);
			}

			if (CSVParams != "" && CSVPath != "") {
				CSVDataSet CSV = createCSVDataSet(CSVPath, CSVParams, TestCaseID);
				// Adding created CSV to ThreadGroup
				insideRequest.add(CSV);
			}

			// Create Bean shell assertion/Regular expression extractor for each Global
			// property
			if (GlobalProp != "") {
				String GlobalProperty[] = GlobalProp.split(",");
				for (int i = 0; i < GlobalProperty.length; i++) {
					if (WebServiceType.equalsIgnoreCase("REST")) {
						BeanShellPostProcessor BSProcessor = extractJsonTagValue(GlobalProperty[i].trim());
						insideRequest.add(BSProcessor);

					} else if (WebServiceType.equalsIgnoreCase("SOAP")) {
						RegexExtractor extractor = extractSoapTagValue(GlobalProperty[i].trim());
						insideRequest.add(extractor);
					}
				}
			}
		}

		// Save generated test plan to JMeter's .jmx file format

		if (!new File(System.getProperty("user.dir") + "\\Execution_Reports\\JMX_Script").exists()) {
			(new File(System.getProperty("user.dir") + "\\Execution_Reports")).mkdir();
			(new File(System.getProperty("user.dir") + "\\Execution_Reports\\JMX_Script")).mkdir();
			(new File(System.getProperty("user.dir") + "\\Execution_Reports\\Results")).mkdir();
		}

		String jmxFilePath = System.getProperty("user.dir") + "\\Execution_Reports\\JMX_Script\\" + threadGroupName
				+ "_" + getCurrentDateAndTime() + ".jmx";

		SaveService.saveTree(testPlanTree, new FileOutputStream(jmxFilePath));
		System.out.println("JMX file created successfully.........");
		System.out.println("======================================");

		// add Summarizer output to get test progress in stdout like:
		// summary = 2 in 1.3s = 1.5/s Avg: 631 Min: 290 Max: 973 Err: 0 (0.00%)
		Summariser summer = null;
		String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
		if (summariserName.length() > 0) {
			summer = new Summariser(summariserName);
		}

		String jtlFilePath = System.getProperty("user.dir") + "\\Execution_Reports\\Results\\" + threadGroupName + "_"
				+ getCurrentDateAndTime() + ".jtl";
		ResultCollector logger = new ResultCollector(summer);
		logger.setFilename(jtlFilePath);
		testPlanTree.add(testPlanTree.getArray()[0], logger);

		// Run Test Plan
		StandardJMeterEngine jmeter = new StandardJMeterEngine();
		jmeter.configure(testPlanTree);
		jmeter.run();

		System.out.println("JMeter .jmx script is available at " + System.getProperty("user.dir")
				+ "\\Execution_Reports\\JMX\\" + threadGroupName + "_" + getCurrentDateAndTime() + ".jmx");

		// Code to execute jtl file to generate HTML report

		String jmeterPath = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
				AutomationConstants.JMETER_PATH);

		String needRemoteHost = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
				AutomationConstants.NEED_REMOTE_EXECUTIONS);
		try {
			Runtime rt = Runtime.getRuntime();
			if (needRemoteHost.equalsIgnoreCase("yes")) {
				String remoteHosts = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
						AutomationConstants.REMOTE_HOSTS);
				Process prr = rt.exec(jmeterPath + "\\bin\\jmeter.bat -n -t " + jmxFilePath + " -R " + remoteHosts
						+ " -l " + jtlFilePath);
				prr.waitFor();
				prr.destroy();
			}
			Thread.sleep(2000);
			JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, System.getProperty("user.dir")
					+ "\\Execution_Reports\\Results\\" + threadGroupName + "_" + getCurrentDateAndTime());
			ReportGenerator generator = new ReportGenerator(jtlFilePath, null);
			generator.generate();
			Thread.sleep(2000);

			// Send Execution Report in mail
			String sendMail = new DataHandler().getProperty(AutomationConstants.EMAIL_CONFIG,
					AutomationConstants.NEED_EMAIL_REPORT);
			if (sendMail.equalsIgnoreCase("yes")) {
				if (!new File(System.getProperty("user.dir") + "\\Email_Execution_Reports\\Results").exists()) {
					(new File(System.getProperty("user.dir") + "\\Email_Execution_Reports")).mkdir();
					(new File(System.getProperty("user.dir") + "\\Email_Execution_Reports\\Results")).mkdir();
				}
				JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, System.getProperty("user.dir")
						+ "\\Email_Execution_Reports\\Results\\" + threadGroupName + "_" + getCurrentDateAndTime());
				ReportGenerator generatorEmail = new ReportGenerator(jtlFilePath, null);
				generatorEmail.generate();
				Thread.sleep(2000);
				Utilities.sendMail();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load samplers for perform test with number of users, ramp-up time and loop
	 * count. You have to map all the API details in the template SampleAPIs.xlsx
	 * that available in the path:
	 * \src\test\resources\PerformanceTesting\SamplerTemplate
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param testPlanName
	 * @param threadGroupName
	 * @param numberOfUsers
	 * @param rampUpTime
	 * @param loopCount
	 * @param sheetNameofApiFile
	 * @return
	 * @throws Exception
	 */
	public static void loadSamplers(String testPlanName, String threadGroupName, int numberOfUsers, int rampUpTime,
			int loopCount, String sheetNameofApiFile) throws Exception {
		HTTPSamplerProxy HTTPSample = null;
		HashTree testPlanTree = new HashTree();
		int rowStart = 1;
		int columnStart = 0;
		int Totalcolumns;
		int Size = 0;

		String apiFilePath = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
				AutomationConstants.API_FILE_PATH);

		XSSFSheet sheet = getExcelSheetRef(apiFilePath, sheetNameofApiFile);
		Totalcolumns = sheet.getRow(0).getPhysicalNumberOfCells();

		Size = Totalcolumns + 1;
		String[] TC_Data = new String[Size];

		int rowEnd = sheet.getPhysicalNumberOfRows();

		// Creating Test Plan and ThreadGroup

		TestPlan testPlan = new TestPlan(testPlanName);
		testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
		testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
		testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
		// Construct Test Plan from previously initialized elements
		testPlanTree.add(testPlan);
		HashTree threadGroupHashTree = testPlanTree.add(testPlan,
				createThreadGroup(threadGroupName, numberOfUsers, rampUpTime, loopCount));
		HashTree insideRequest;

		for (int Currentrow = rowStart; Currentrow < rowEnd; Currentrow++) {

			// Checking end of InputExcelfile i.e if RequestType column data is
			// blank.
			if (ReadCell(sheet, Currentrow, RequestType_Index).length() == 0) {
				break;
			}
			// Checking if current row has Testcase ID; New ThreadGroup needs to
			// be created for each Testcase.
			if (ReadCell(sheet, Currentrow, TestCaseID_Index).length() > 0) {
				// Checking if "Include for execution ?" value is "Yes"; If No,
				// then the Testcase is skipped.
				for (int Loopvar = 0; Loopvar < Size; Loopvar++) {
					TC_Data[Loopvar] = "";
				}
				for (int Currentcolumn = columnStart; Currentcolumn < Totalcolumns; Currentcolumn++) {
					TC_Data[Currentcolumn] = ReadCell(sheet, Currentrow, Currentcolumn);
				}
				loadExcelData(TC_Data);
			}

			HTTPSample = new HTTPSamplerProxy();
			HTTPSample.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
			HTTPSample.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
			HTTPSample.setName(TestCaseID);
			HTTPSample.setDomain(ServerIP.replace("http://", "").replace("/", ""));
			if (PortNo.length() != 0) {
				HTTPSample.setPort(Integer.parseInt(PortNo.replace(".0", "")));
			}
			HTTPSample.setPath(ResourcePath);
			HTTPSample.setMethod(ReqType.toUpperCase());

			// Setting Request Parameters
			if (ResourceParamsName != "" && ResourceParamsValue != "") {
				HTTPSample.addEncodedArgument(ResourceParamsName, ResourceParamsValue, "");
			}

			// Setting POST request body
			if (RequestBody != "") {
				HTTPSample.addNonEncodedArgument("HTTPArgument", RequestBody, "");
				HTTPSample.setPostBodyRaw(true);
			}

			// Use multipart/form-data for HTTP POST
			if (UseMultipart.toUpperCase().equals("YES")) {
				HTTPSample.setDoMultipart(true);
			}

			// Use browser Compatible headers when using multipart/form-data
			if (UseBrowserCompHeaders.toUpperCase().equals("YES")) {
				HTTPSample.setDoBrowserCompatibleMultipart(true);
			}

			// Send attachments if provided
			if (SendFiles.toUpperCase().trim().equals("YES")) {
				HTTPSample.setHTTPFiles(new HTTPFileArg[] {
						new HTTPFileArg(AttachmentFilePath, AttachmentParameterName, AttachmentMMEType) });
			}

			insideRequest = threadGroupHashTree.add(HTTPSample);

			if (Header != "") {
				HeaderManager requestHeaderManager = createHeaderManager(Header, TestCaseID);
				insideRequest.add(requestHeaderManager);
			}
			if (ExpectedResponseCode != "") {
				ResponseAssertion ResponseCodeAssertion = createResponseCodeAssertion(ExpectedResponseCode, TestCaseID);
				insideRequest.add(ResponseCodeAssertion);
			}
			if (ExpectedResponseMessage != "") {
				ResponseAssertion ResponseMessageAssertion = createResponseMessageAssertion(ExpectedResponseMessage,
						TestCaseID);
				insideRequest.add(ResponseMessageAssertion);
			}
			if (ExpectedResponseBody != "") {
				ResponseAssertion ResponseBodyAssertion = createResponseBodyAssertion(ExpectedResponseBody, TestCaseID);
				insideRequest.add(ResponseBodyAssertion);
			}
			if (ExpectedResponseTime != "") {
				DurationAssertion ResponseDurationAssertion = createDurationAssertion(ExpectedResponseTime, TestCaseID);
				insideRequest.add(ResponseDurationAssertion);
			}

			if (CSVParams != "" && CSVPath != "") {
				CSVDataSet CSV = createCSVDataSet(CSVPath, CSVParams, TestCaseID);
				// Adding created CSV to ThreadGroup
				insideRequest.add(CSV);
			}
			// Create Bean shell assertion/Regular expression extractor for each Global
			// property
			if (GlobalProp != "") {
				String GlobalProperty[] = GlobalProp.split(",");
				for (int i = 0; i < GlobalProperty.length; i++) {
					if (WebServiceType.equalsIgnoreCase("REST")) {
						BeanShellPostProcessor BSProcessor = extractJsonTagValue(GlobalProperty[i].trim());
						insideRequest.add(BSProcessor);
					} else if (WebServiceType.equalsIgnoreCase("SOAP")) {
						RegexExtractor extractor = extractSoapTagValue(GlobalProperty[i].trim());
						insideRequest.add(extractor);
					}
				}
			}
		}

		// Save generated test plan to JMeter's .jmx file format

		if (!new File(System.getProperty("user.dir") + "\\Execution_Reports\\JMX_Script").exists()) {
			(new File(System.getProperty("user.dir") + "\\Execution_Reports")).mkdir();
			(new File(System.getProperty("user.dir") + "\\Execution_Reports\\JMX_Script")).mkdir();
			(new File(System.getProperty("user.dir") + "\\Execution_Reports\\Results")).mkdir();
		}

		String jmxFilePath = System.getProperty("user.dir") + "\\Execution_Reports\\JMX_Script\\" + threadGroupName
				+ "_" + getCurrentDateAndTime() + ".jmx";

		SaveService.saveTree(testPlanTree, new FileOutputStream(jmxFilePath));

		// add Summarizer output to get test progress in stdout like:
		// summary = 2 in 1.3s = 1.5/s Avg: 631 Min: 290 Max: 973 Err: 0 (0.00%)
		Summariser summer = null;
		String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
		if (summariserName.length() > 0) {
			summer = new Summariser(summariserName);
		}

		String jtlFilePath = System.getProperty("user.dir") + "\\Execution_Reports\\Results\\" + threadGroupName + "_"
				+ getCurrentDateAndTime() + ".jtl";
		ResultCollector logger = new ResultCollector(summer);
		logger.setFilename(jtlFilePath);
		testPlanTree.add(testPlanTree.getArray()[0], logger);

		// Run Test Plan
		StandardJMeterEngine jmeter = new StandardJMeterEngine();
		jmeter.configure(testPlanTree);
		jmeter.run();

		System.out.println("JMeter .jmx script is available at " + System.getProperty("user.dir")
				+ "\\Execution_Reports\\JMX\\" + threadGroupName + "_" + getCurrentDateAndTime() + ".jmx");

		// Code to execute jtl file to generate HTML report

		String jmeterPath = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
				AutomationConstants.JMETER_PATH);
		String needRemoteHost = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
				AutomationConstants.NEED_REMOTE_EXECUTIONS);
		try {
			Runtime rt = Runtime.getRuntime();
			if (needRemoteHost.equalsIgnoreCase("yes")) {
				String remoteHosts = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
						AutomationConstants.REMOTE_HOSTS);
				Process prr = rt.exec(jmeterPath + "\\bin\\jmeter.bat -n -t " + jmxFilePath + " -R " + remoteHosts
						+ " -l " + jtlFilePath);
				prr.waitFor();
				prr.destroy();
			}
			Thread.sleep(2000);
			JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, System.getProperty("user.dir")
					+ "\\Execution_Reports\\Results\\" + threadGroupName + "_" + getCurrentDateAndTime());
			ReportGenerator generator = new ReportGenerator(jtlFilePath, null);
			generator.generate();
			Thread.sleep(2000);

			// Send Execution Report in mail
			String sendMail = new DataHandler().getProperty(AutomationConstants.EMAIL_CONFIG,
					AutomationConstants.NEED_EMAIL_REPORT);
			if (sendMail.equalsIgnoreCase("yes")) {
				if (!new File(System.getProperty("user.dir") + "\\Email_Execution_Reports\\Results").exists()) {
					(new File(System.getProperty("user.dir") + "\\Email_Execution_Reports")).mkdir();
					(new File(System.getProperty("user.dir") + "\\Email_Execution_Reports\\Results")).mkdir();
				}
				JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, System.getProperty("user.dir")
						+ "\\Email_Execution_Reports\\Results\\" + threadGroupName + "_" + getCurrentDateAndTime());
				ReportGenerator generatorEmail = new ReportGenerator(jtlFilePath, null);
				generatorEmail.generate();
				Thread.sleep(2000);
				Utilities.sendMail();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Execute JMX file when user pass it as input file
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param jmxFilePath
	 * @throws Exception
	 */
	public static void executeJMX(String jmxFilePath) throws Exception {
		try {
			String jmeterPath = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
					AutomationConstants.JMETER_PATH);

			if (jmxFilePath.equals("")) {
				System.out.println("Please provide JMX file path");
				System.exit(0);
			}
			String jmxFileExtension = FilenameUtils.getExtension(jmxFilePath);
			if (!jmxFileExtension.equalsIgnoreCase("jmx")) {
				System.out.println("Please provide valid JMX file");
				System.exit(0);
			}

			// Save the jtl and execution results
			if (!new File(System.getProperty("user.dir") + "\\Execution_Reports\\Results").exists()) {
				(new File(System.getProperty("user.dir") + "\\Execution_Reports")).mkdir();
				(new File(System.getProperty("user.dir") + "\\Execution_Reports\\Results")).mkdir();
			}
			String jtlFilePath = System.getProperty("user.dir") + "\\Execution_Reports\\Results\\" + "Test_Result" + "_"
					+ getCurrentDateAndTime() + ".jtl";

			String needRemoteHost = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
					AutomationConstants.NEED_REMOTE_EXECUTIONS);

			Runtime rt = Runtime.getRuntime();
			if (needRemoteHost.equalsIgnoreCase("yes")) {
				String remoteHosts = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
						AutomationConstants.REMOTE_HOSTS);
				Process prr = rt.exec(jmeterPath + "\\bin\\jmeter.bat -n -t " + jmxFilePath + " -R " + remoteHosts
						+ " -l " + jtlFilePath);
				prr.waitFor();
				prr.destroy();
			} else {
				Process pr = rt.exec(jmeterPath + "\\bin\\jmeter.bat -n -t " + jmxFilePath + " -l " + jtlFilePath);
				pr.waitFor();
				pr.destroy();
			}

			Thread.sleep(5000);
			System.out.println("JMX file executed and JTL file created in " + jtlFilePath);

			JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, System.getProperty("user.dir")
					+ "\\Execution_Reports\\Results\\" + "Test_Result" + "_" + getCurrentDateAndTime());
			ReportGenerator generator = new ReportGenerator(jtlFilePath, null);
			generator.generate();

			System.out.println("Execution completed sucessfully and generated the reports in "
					+ System.getProperty("user.dir") + "\\Execution_Reports\\Results\\");
			Thread.sleep(2000);

			// Send Execution Report in mail
			String sendMail = new DataHandler().getProperty(AutomationConstants.EMAIL_CONFIG,
					AutomationConstants.NEED_EMAIL_REPORT);
			if (sendMail.equalsIgnoreCase("yes")) {
				if (!new File(System.getProperty("user.dir") + "\\Email_Execution_Reports\\Results").exists()) {
					(new File(System.getProperty("user.dir") + "\\Email_Execution_Reports")).mkdir();
					(new File(System.getProperty("user.dir") + "\\Email_Execution_Reports\\Results")).mkdir();
				}
				JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, System.getProperty("user.dir")
						+ "\\Email_Execution_Reports\\Results\\" + "_" + getCurrentDateAndTime());
				ReportGenerator generatorEmail = new ReportGenerator(jtlFilePath, null);
				generatorEmail.generate();
				Thread.sleep(2000);
				Utilities.sendMail();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to execute the JMX scripts based on the give file name. The JMX script
	 * should be kept in the /src/test/resources/PerformanceTesting/JMX_Scripts/
	 * folder in the project structure
	 * 
	 * @author sanoj.swaminathan
	 * @since 26-04-2023
	 * @param jmxFileName
	 * @throws Exception
	 */
	public static void executeJMXScript(String jmxFileName) throws Exception {
		try {
			String jmeterPath = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
					AutomationConstants.JMETER_PATH);

			String jmxFilePath = System.getProperty("user.dir") + AutomationConstants.JMX_SCRIPTS_PATH + jmxFileName
					+ ".jmx";

			// Save the jtl and execution results
			if (!new File(System.getProperty("user.dir") + "\\Execution_Reports\\Results").exists()) {
				(new File(System.getProperty("user.dir") + "\\Execution_Reports")).mkdir();
				(new File(System.getProperty("user.dir") + "\\Execution_Reports\\Results")).mkdir();
			}
			String jtlFilePath = System.getProperty("user.dir") + "\\Execution_Reports\\Results\\" + "Test_Result" + "_"
					+ getCurrentDateAndTime() + ".jtl";

			String needRemoteHost = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
					AutomationConstants.NEED_REMOTE_EXECUTIONS);

			Runtime rt = Runtime.getRuntime();
			if (needRemoteHost.equalsIgnoreCase("yes")) {
				String remoteHosts = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
						AutomationConstants.REMOTE_HOSTS);
				Process prr = rt.exec(jmeterPath + "\\bin\\jmeter.bat -n -t " + jmxFilePath + " -R " + remoteHosts
						+ " -l " + jtlFilePath);
				prr.waitFor();
				prr.destroy();
			} else {
				Process pr = rt.exec(jmeterPath + "\\bin\\jmeter.bat -n -t " + jmxFilePath + " -l " + jtlFilePath);
				pr.waitFor();
				pr.destroy();
			}

			Thread.sleep(5000);
			System.out.println("JMX file executed and JTL file created in " + jtlFilePath);

			JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, System.getProperty("user.dir")
					+ "\\Execution_Reports\\Results\\" + "Test_Result" + "_" + getCurrentDateAndTime());
			ReportGenerator generator = new ReportGenerator(jtlFilePath, null);
			generator.generate();

			System.out.println("Execution completed sucessfully and generated the reports in "
					+ System.getProperty("user.dir") + "\\Execution_Reports\\Results\\");
			Thread.sleep(2000);

			// Send Execution Report in mail
			String sendMail = new DataHandler().getProperty(AutomationConstants.EMAIL_CONFIG,
					AutomationConstants.NEED_EMAIL_REPORT);
			if (sendMail.equalsIgnoreCase("yes")) {
				if (!new File(System.getProperty("user.dir") + "\\Email_Execution_Reports\\Results").exists()) {
					(new File(System.getProperty("user.dir") + "\\Email_Execution_Reports")).mkdir();
					(new File(System.getProperty("user.dir") + "\\Email_Execution_Reports\\Results")).mkdir();
				}
				JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, System.getProperty("user.dir")
						+ "\\Email_Execution_Reports\\Results\\" + "_" + getCurrentDateAndTime());
				ReportGenerator generatorEmail = new ReportGenerator(jtlFilePath, null);
				generatorEmail.generate();
				Thread.sleep(2000);
				Utilities.sendMail();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to execute and generate reports
	 * 
	 * @author sanoj.swaminathan
	 * @since 31-Jan-2021
	 * @param threadGroupName
	 */
	public static void executeAndGenerateReport(String threadGroupName) {
		try {
			// Generate JMX file and JTL file
			if (!new File(System.getProperty("user.dir") + "\\Execution_Reports\\JMX_Script").exists()) {
				(new File(System.getProperty("user.dir") + "\\Execution_Reports")).mkdir();
				(new File(System.getProperty("user.dir") + "\\Execution_Reports\\JMX_Script")).mkdir();
				(new File(System.getProperty("user.dir") + "\\Execution_Reports\\Results")).mkdir();
			}

			String jmxFilePath = System.getProperty("user.dir") + "\\Execution_Reports\\JMX_Script\\" + threadGroupName
					+ "_" + getCurrentDateAndTime() + ".jmx";
			SaveService.saveTree(testPlanTree, new FileOutputStream(jmxFilePath));

			System.out.println("JMX file created successfully.........");
			System.out.println("JMeter .jmx script is available at " + System.getProperty("user.dir")
					+ "\\Execution_Reports\\JMX\\" + threadGroupName + "_" + getCurrentDateAndTime() + ".jmx");

			// Add Summarizer output to get test progress in stdout like:
			// summary = 2 in 1.3s = 1.5/s Avg: 631 Min: 290 Max: 973 Err: 0 (0.00%)
			Summariser summer = null;
			String summariserName = JMeterUtils.getPropDefault("summariser.name", "summary");
			if (summariserName.length() > 0) {
				summer = new Summariser(summariserName);
			}
			String jtlFilePath = System.getProperty("user.dir") + "\\Execution_Reports\\Results\\" + threadGroupName
					+ "_" + getCurrentDateAndTime() + ".jtl";
			ResultCollector logger = new ResultCollector(summer);
			logger.setFilename(jtlFilePath);
			testPlanTree.add(testPlanTree.getArray()[0], logger);

			// Run Test Plan
			StandardJMeterEngine jmeter = new StandardJMeterEngine();
			jmeter.configure(testPlanTree);
			jmeter.run();

			// Execute JTL file to generate HTML report
			JMeterUtils.setProperty(JMETER_REPORT_OUTPUT_DIR_PROPERTY, System.getProperty("user.dir")
					+ "\\Execution_Reports\\Results\\" + threadGroupName + "_" + getCurrentDateAndTime());
			ReportGenerator generator = new ReportGenerator(jtlFilePath, null);
			generator.generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create LoopController with count
	 * 
	 * @author sanoj.swaminathan
	 * @since 30-Jan-2021
	 * @param loopCount
	 * @return
	 */
	private static LoopController createLoopContrloller(int loopCount) {
		LoopController loopController = null;
		try {
			loopController = new LoopController();
			loopController.setLoops(loopCount);
			loopController.setFirst(true);
			loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
			loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
			loopController.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loopController;
	}

	/**
	 * Method to get the pattern matching rule
	 * 
	 * @author sanoj.swaminathan
	 * @since 31-Jan-2021
	 * @param responseMessage
	 * @param patternMatchingRule
	 * @return
	 */
	private static ResponseAssertion getPatternMatchingRule(ResponseAssertion responseMessage,
			String patternMatchingRule) {
		if (patternMatchingRule.equalsIgnoreCase("equals")) {
			responseMessage.setToEqualsType();
		}
		if (patternMatchingRule.equalsIgnoreCase("contains")) {
			responseMessage.setToContainsType();
		}
		if (patternMatchingRule.equalsIgnoreCase("matches")) {
			responseMessage.setToMatchType();
		}
		if (patternMatchingRule.equalsIgnoreCase("substring")) {
			responseMessage.setToSubstringType();
		}
		if (patternMatchingRule.equalsIgnoreCase("not")) {
			responseMessage.setToNotType();
		}
		if (patternMatchingRule.equalsIgnoreCase("or")) {
			responseMessage.setToOrType();
		}
		return responseMessage;
	}

	/**
	 * To get the number of rows from CSV file.
	 * 
	 * @author sanoj.swaminathan
	 * @since 30-Jan-2021
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	private static int getCSVRowCount(String filename) throws IOException {
		BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}

	/**
	 * This function creates Regular expression to validate JSON response body.
	 *
	 * @author sanoj.swaminathan
	 * @since 31-Jan-2021
	 * @param RegularExp
	 * @return
	 */
	private static BeanShellPostProcessor extractJsonTagValue(String regularExpression) {
		if (regularExpression.trim() != "") {
			String regexp = "\"" + regularExpression.trim() + "\\\":(.*?)[,}]\"";
			BeanShellPostProcessor BSPostProcessor = new BeanShellPostProcessor();
			BSPostProcessor.setProperty(TestElement.TEST_CLASS, BeanShellPostProcessor.class.getName());
			BSPostProcessor.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
			BSPostProcessor.setProperty("filename", "");
			BSPostProcessor.setProperty("parameters", "");
			BSPostProcessor.setProperty("resetInterpreter", false);
			BSPostProcessor.setName("Set Global Property_" + regularExpression);
			String part1 = ("import com.eclipsesource.json.*;" + "\n" + "import java.util.regex.Pattern;" + "\n"
					+ "import java.util.regex.Matcher;");
			String part2 = ("\n" + "String a=\"null\";" + "\n" + "String resp=prev.getResponseDataAsString();" + "\n"
					+ "Pattern p = Pattern.compile(" + regexp + ");");
			String part3 = ("\n" + "Matcher m = p.matcher(resp);" + "\n" + "if (m.find())" + "\n" + "{" + "\n"
					+ "a=m.group(1);" + "\n" + "String a1=a.replace(\"\\\"\", \"\");" + "\n" + "vars.put(\""
					+ regularExpression.trim() + "\",a1);}");
			BSPostProcessor.setProperty("script", part1 + part2 + part3);
			return BSPostProcessor;
		}
		return null;
	}

	/**
	 * Get excel sheet reference
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param InputExcel
	 * @param SheetName
	 * @return
	 */
	private static XSSFSheet getExcelSheetRef(String InputExcel, String SheetName) {

		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(InputExcel);
		} catch (IOException e) {
			e.printStackTrace();
		}
		XSSFSheet SheetRef = workbook.getSheet(SheetName);
		return SheetRef;
	}

	/**
	 * Read cell data
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param Sheet
	 * @param Row
	 * @param Column
	 * @return
	 */
	private static String ReadCell(XSSFSheet Sheet, int Row, int Column) {
		return Sheet.getRow(Row).getCell(Column).toString().trim();
	}

	/**
	 * Load data
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param Data
	 */
	private static void loadExcelData(String[] Data) {
		try {
			TestCaseID = Data[TestCaseID_Index].trim();
			TestCaseComment = Data[TestCaseComment_Index].trim();
			ReqType = Data[RequestType_Index].trim();
			ServerIP = Data[ServerIP_Index].trim();
			PortNo = Data[PortNo_Index].trim();
			ResourcePath = Data[ResourcePath_Index].trim();
			Header = Data[Header_Index].trim();
			RequestBody = Data[RequestBody_Index].trim();
			ExpectedResponseCode = Data[ExpectedResponseCode_Index].trim();
			ExpectedResponseMessage = Data[ExpectedResponseMessage_Index].trim();
			ExpectedResponseBody = Data[ExpectedResponseBody_Index].trim();
			ExpectedResponseTime = Data[ExpectedResponseTime_Index].trim();
			UseMultipart = Data[UseMultipart_Index].trim();
			UseBrowserCompHeaders = Data[UseBrowserCompHeaders_Index].trim();
			SendFiles = Data[SendFiles_Index].trim();
			AttachmentFilePath = Data[AttachmentFilePath_Index].trim();
			AttachmentParameterName = Data[AttachmentParameterName_Index].trim();
			AttachmentMMEType = Data[AttachmentMMEType_Index].trim();
			CSVParams = Data[CSVParams_Index].trim();
			CSVPath = Data[CSVPath_Index].trim();
			ResourceParamsName = Data[ResourceParamsName_Index].trim();
			ResourceParamsValue = Data[ResourceParamsValue_Index].trim();
			WebServiceType = Data[WebServiceType_Index].trim();
			GlobalProp = Data[GlobalProp_Index].trim();
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Create LoopController
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @return
	 */
	private static LoopController createLoopContrloller() {
		LoopController loopController = null;
		try {
			loopController = new LoopController();
			loopController.setLoops(1);
			loopController.setFirst(true);
			loopController.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
			loopController.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
			loopController.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loopController;
	}

	/**
	 * Create basic ThreadGroup
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @return
	 */
	private static ThreadGroup createThreadGroup(String threadGroupName) {
		ThreadGroup threadGroup = null;
		try {
			threadGroup = new ThreadGroup();
			threadGroup.setName(threadGroupName);
			threadGroup.setNumThreads(1);
			threadGroup.setRampUp(1);
			threadGroup.setSamplerController(createLoopContrloller());
			threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
			threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return threadGroup;
	}

	/**
	 * Create ThreadGroup with number of users, ramp-up time and loop count
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @return
	 */
	private static ThreadGroup createThreadGroup(String threadGroupName, int numberOfUsers, int rampUpTime,
			int loopCount) {
		ThreadGroup threadGroup = null;
		try {
			threadGroup = new ThreadGroup();
			threadGroup.setName(threadGroupName);
			threadGroup.setNumThreads(numberOfUsers);
			threadGroup.setRampUp(rampUpTime);
			threadGroup.setSamplerController(createLoopContrloller(loopCount));
			threadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
			threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return threadGroup;
	}

	/**
	 * Create header manager for requests/samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param headerData
	 * @param headerName
	 * @return
	 */
	private static HeaderManager createHeaderManager(String headerData, String headerName) {
		HeaderManager HeaderMgr = new HeaderManager();
		HeaderMgr.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
		HeaderMgr.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
		HeaderMgr.setName(headerName + "_Header");
		if (headerData.trim() != "") {
			String lines[] = headerData.trim().split("\\r?\\n");
			for (int i = 0; i < lines.length; i++) {
				Header requestHead = new Header();
				String headerval = (lines[i].trim());
				String[] result = headerval.split(":");
				requestHead.setName(result[0]);
				requestHead.setValue(result[1]);
				HeaderMgr.add(requestHead);
			}
		}
		return HeaderMgr;
	}

	/**
	 * Creating Response Code Assertion and adding to samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param RespCode
	 * @return
	 */
	private static ResponseAssertion createResponseCodeAssertion(String RespCode, String TestCaseID) {
		if (RespCode.trim() != "") {
			ResponseAssertion ResponseCode = new ResponseAssertion();
			ResponseCode.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
			ResponseCode.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
			ResponseCode.setProperty(TestElement.ENABLED, true);
			ResponseCode.setName(TestCaseID + "_Assert Response Code");
			ResponseCode.setScopeParent();
			ResponseCode.setTestFieldResponseCode();
			ResponseCode.setToEqualsType();
			ResponseCode.addTestString(RespCode.trim().replace(".0", ""));
			return ResponseCode;
		}
		return null;
	}

	/**
	 * Creating Response Message Assertion and adding to samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param RespMessage
	 * @return
	 */
	private static ResponseAssertion createResponseMessageAssertion(String RespMessage, String TestCaseID) {
		if (RespMessage.trim() != "") {
			ResponseAssertion ResponseMessage = new ResponseAssertion();
			ResponseMessage.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
			ResponseMessage.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
			ResponseMessage.setProperty(TestElement.ENABLED, true);
			ResponseMessage.setName(TestCaseID + "_Assert Response Message");
			ResponseMessage.setScopeParent();
			ResponseMessage.setTestFieldResponseMessage();
			ResponseMessage.setToEqualsType();
			ResponseMessage.addTestString(RespMessage.trim());
			return ResponseMessage;
		}
		return null;
	}

	/**
	 * Creating Response Body Assertion and adding to samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param RespMessage
	 * @param testCaseID
	 * @return
	 */
	private static ResponseAssertion createResponseBodyAssertion(String RespMessage, String TestCaseID) {
		if (RespMessage.trim() != "") {
			ResponseAssertion ResponseBody = new ResponseAssertion();
			ResponseBody.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
			ResponseBody.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
			ResponseBody.setProperty(TestElement.ENABLED, true);
			ResponseBody.setName(TestCaseID + "_Assert Response Body");
			ResponseBody.setScopeParent();
			ResponseBody.setTestFieldResponseData();
			ResponseBody.setToContainsType();
			ResponseBody.addTestString(RespMessage.trim());
			return ResponseBody;
		}
		return null;
	}

	/**
	 * Creating Response Time Assertion and adding to samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param DurationTime
	 * @param TestCaseID
	 * @return
	 */
	private static DurationAssertion createDurationAssertion(String DurationTime, String TestCaseID) {
		if (DurationTime.trim() != "") {
			DurationAssertion ResponseDuration = new DurationAssertion();
			ResponseDuration.setProperty(TestElement.TEST_CLASS, DurationAssertion.class.getName());
			ResponseDuration.setProperty(TestElement.GUI_CLASS, DurationAssertionGui.class.getName());
			ResponseDuration.setProperty(TestElement.ENABLED, true);
			ResponseDuration.setName(TestCaseID + "_Assert Response Time");
			ResponseDuration.setScopeParent();
			ResponseDuration.setProperty("DurationAssertion.duration", DurationTime.trim().replace(".0", ""));
			return ResponseDuration;
		}
		return null;
	}

	/**
	 * create CSV Data Set and adding to samplers
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param FileName
	 * @param VariableNames
	 * @param TestCaseID
	 * @return
	 * @throws IOException
	 */
	private static CSVDataSet createCSVDataSet(String FileName, String VariableNames, String TestCaseID)
			throws IOException {
		CSVDataSet SampleCSV = new CSVDataSet();
		SampleCSV.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
		SampleCSV.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
		SampleCSV.setName(TestCaseID + "_CSV Data Set");
		SampleCSV.setDelimiter(",");
		SampleCSV.setProperty(TestElement.ENABLED, true);
		SampleCSV.setProperty("delimiter", ",");
		SampleCSV.setProperty("fileEncoding", "");
		SampleCSV.setProperty("quotedData", false);
		SampleCSV.setProperty("recycle", false);
		SampleCSV.setProperty("stopThread", true);
		SampleCSV.setProperty("shareMode", "shareMode.all");
		SampleCSV.setProperty("filename", FileName.trim());
		SampleCSV.setProperty("variableNames", VariableNames.trim());

		// This code returns the number of rows in CSV file
		int csvcount = getCSVRowCount(FileName.trim());
		System.out.println("CSV count is" + csvcount);
		return SampleCSV;
	}

	/**
	 * This function creates a regular expression to extract required tag & value
	 *
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @param Tag
	 * @return
	 */
	private static RegexExtractor extractSoapTagValue(String Tag) {
		String regexp = "&lt;" + Tag.trim() + "&gt;(.*)&lt;/" + Tag.trim() + "&gt";
		RegexExtractor regularexpextractor = new RegexExtractor();
		regularexpextractor.setProperty(TestElement.TEST_CLASS, RegexExtractor.class.getName());
		regularexpextractor.setProperty(TestElement.GUI_CLASS, RegexExtractorGui.class.getName());
		regularexpextractor.setProperty("RegexExtractor.useHeaders", false);
		regularexpextractor.setProperty("RegexExtractor.refname", Tag.trim());
		regularexpextractor.setProperty("RegexExtractor.regex", regexp);
		regularexpextractor.setProperty("RegexExtractor.template", "$1$");
		regularexpextractor.setProperty("RegexExtractor.default", "NULL");
		regularexpextractor.setProperty("RegexExtractor.match_number", 1);
		regularexpextractor.setName("Set Global Property_" + Tag);
		return regularexpextractor;
	}

	/**
	 * To get the current date and time
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @return
	 */
	private static String getCurrentDateAndTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat timeFormat = new SimpleDateFormat("HH-mm-ss");
		Date date = new Date();
		String currdate = dateFormat.format(date);
		String currtime = timeFormat.format(date);
		return currdate + "_" + currtime;
	}
}
