package com.joq.testcases;

import org.testng.annotations.Test;

import com.joq.keywords.PerformanceActions;
import com.joq.runner.TestRunner;

public class DataDrivenTest extends TestRunner {

	@Test(enabled = true, description = "Performance test execution by loading the sampler details from excel data")
	public void TC001_performanceTestByLoadingSamplers() throws Exception {
		PerformanceActions.loadSamplers("SamplePlan", "SampleGroup", 10, 2, 1, "TestCases");
	}

	@Test(enabled = true, description = "Performance test execution by running the JMX file")
	public void TC002_performanceTestByRunningJMX() throws Exception {
		// PerformanceActions.executeJMX("D:\\Test\\TestNewThreadGroup_2022-10-18_13-53-36.jmx");
		PerformanceActions.executeJMXScript("TestNewThreadGroup_2022-10-18_13-53-36");
	}

}
