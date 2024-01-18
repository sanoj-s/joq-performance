package com.joq.runner;

import java.io.IOException;

import org.testng.annotations.BeforeClass;

import com.joq.base.AutomationBase;

public class TestRunner extends AutomationBase {

	@BeforeClass(alwaysRun = true)
	public void SetUp() throws InterruptedException, IOException {
		try {
			setUpEnvironment();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
