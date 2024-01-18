package com.joq.testhelpers;

import org.apache.jorphan.collections.HashTree;

import com.joq.keywords.PerformanceActions;

public class PerformanceTestHelper {
	/**
	 * Method to perform the google search and validation
	 * 
	 * @author sanoj.swaminathan
	 * @since 27-04-2023
	 * @param threadGroup
	 */
	public void googleSearch(HashTree threadGroup) {
		try {
			HashTree googleSearchSampler = PerformanceActions.createGETHTTPSampler(threadGroup, "GoogleSearch",
					"www.google.com", "", "/", "q", "${searchValue}");
			PerformanceActions.createHeaderManager(googleSearchSampler, "accept-language:en-US", "GoogleSearch");
			PerformanceActions.createCSVDataSet(googleSearchSampler, "TestData", "searchValue", "GoogleSearch");
			PerformanceActions.createReponseCodeAssertion(googleSearchSampler, "200", "equals", "VerifyResponseCode");
			PerformanceActions.createResponseMessageAssertion(googleSearchSampler, "${searchValue}", "contains",
					"VerifyResponseMessage");
			PerformanceActions.createResponseBodyAssertion(googleSearchSampler, "${searchValue}", "contains",
					"VerifyResponseBody");
			PerformanceActions.createDurationAssertion(googleSearchSampler, "300", "VerifyDuration");
			PerformanceActions.createBeanShellAssertion(googleSearchSampler, "JMeter");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method to create a user
	 * 
	 * @author sanoj.swaminthan
	 * @since 27-04-2023
	 * @param threadGroup
	 */
	public void createUser(HashTree threadGroup) {
		try {
			HashTree createUserSampler = PerformanceActions.createPOSTHTTPSampler(threadGroup, "CreateUser",
					"reqres.in", "", "/api/users",
					"{\r\n" + "    \"name\": \"morpheus\",\r\n" + "    \"job\": \"leader\"\r\n" + "}");
			PerformanceActions.createReponseCodeAssertion(createUserSampler, "200", "equals", "VerifyResponseCode");
			PerformanceActions.createResponseBodyAssertion(createUserSampler, "morpheus", "contains",
					"VerifyResponseBody");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
