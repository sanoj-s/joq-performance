package com.joq.testcases;

import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.joq.keywords.PerformanceActions;
import com.joq.runner.TestRunner;
import com.joq.testhelpers.PerformanceTestHelper;

public class PerformanceTest extends TestRunner {
	TestPlan testplan;
	HashTree threadGroup;

	@BeforeClass
	public void setUp() throws Exception {
		testplan = PerformanceActions.createTestPlan("TestNewPlan");
		threadGroup = PerformanceActions.createThreadGroup(testplan, "TestNewThreadGroup", 5, 5, 1);
	}

	@Test(enabled = true)
	public void TC001_googleSearch() throws Exception {
		new PerformanceTestHelper().googleSearch(threadGroup);
	}

	@Test(enabled = true)
	public void TC002_createUser() throws Exception {
		new PerformanceTestHelper().createUser(threadGroup);
	}

	@AfterClass
	public void tearDown() throws Exception {
		PerformanceActions.executeAndGenerateReport("TestNewThreadGroup");
	}
}
