package com.joq.base;

import java.io.File;

import org.apache.jmeter.util.JMeterUtils;

import com.joq.keywords.DataHandler;
import com.joq.utils.AutomationConstants;
import com.joq.utils.KeyManagement;

public class AutomationBase {

	/**
	 * Setup JMeter properties for initialization. The JMeter path should be mapped
	 * in performance_test_config.properties file. For example:
	 * jmeterPath=C:\\apache-jmeter-5.4
	 * 
	 * @author sanoj.swaminathan
	 * @since 10-Dec-2020
	 * @return
	 * @throws Exception
	 */
	public static void setUpEnvironment() throws Exception {

		// Checking the execution expire
		KeyManagement.checkExpiry();
		
		String jmeterPath = new DataHandler().getProperty(AutomationConstants.PERFROMANCE_TEST_CONFIG,
				AutomationConstants.JMETER_PATH);

		File jmeterHome = new File(jmeterPath.trim());
		String slash = System.getProperty("file.separator");
		try {
			if (jmeterHome.exists()) {
				File jmeterProperties = new File(jmeterHome.getPath() + slash + "bin" + slash + "jmeter.properties");
				if (jmeterProperties.exists()) {
					// JMeter initialization (properties, log levels, locale, etc)
					JMeterUtils.setJMeterHome(jmeterHome.getPath());
					JMeterUtils.loadJMeterProperties(jmeterProperties.getPath());
					JMeterUtils.initLocale();
					System.out.println("JMeter settings are perfect..........");
					System.out.println("======================================");
				} else {
					System.err.println("Jmeter property is not set or pointing to incorrect location.");
					System.exit(1);
				}
			} else {
				System.err.println("JmeterHome property is not set or pointing to incorrect location");
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
