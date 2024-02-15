package com.applitools.listeners;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.EyesRunner;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsSummary;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.selenium.ClassicRunner;
import com.applitools.eyes.selenium.Configuration;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;
import com.applitools.eyes.visualgrid.services.RunnerOptions;
import com.applitools.eyes.visualgrid.services.VisualGridRunner;
import com.google.gson.internal.LinkedTreeMap;
import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.core.TestBaseProvider;
import com.qmetry.qaf.automation.step.QAFTestStepListener;
import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import com.qmetry.qaf.automation.step.StepExecutionTracker;
import com.qmetry.qaf.automation.step.client.TestNGScenario;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;

@QAFTestStepProvider
public class ApplitoolsListener implements QAFTestStepListener, ISuiteListener, ITestListener {
	public EyesRunner runner;

	String appName = "AppUnderTest";

	private static BatchInfo batchInfo;

	String testThreadId = "";
	
	boolean isEyesEnabled;
	
	private String getScenarioOutlineExampleParameters(ITestResult test) {
		String parameterDetails = "";
		
		TestNGScenario scenario = (TestNGScenario) test.getMethod();
		Map<String, Object> metadata = scenario.getMetaData();
		
		// Get cucumber tags/groups which were applied for the Cucumber test run
		List<String> cucumberTags = (List<String>) metadata.get("groups");
		
		boolean useAllScenarioOutlineParametersInTestName = false;
		
		if (cucumberTags != null) {
			useAllScenarioOutlineParametersInTestName = cucumberTags
					.contains("applitoolsUseAllParametersInTestName");
		}
		Object[] parameters = test.getParameters();

		if (parameters.length > 0) {
			parameterDetails = " (";
			for (int i = 0; i < parameters.length; i++) {
				LinkedTreeMap<Object, Object> parameterLinkMap = (LinkedTreeMap<Object, Object>) parameters[i];
				Object[] parameterKeys = parameterLinkMap.keySet().toArray();

				System.out.println(
						"Applitools Eyes - getScenarioOutlineExampleParameters: Detected " + parameterKeys.length + " parameter(s)");

				int parameterLabelCount = 1;

				// Use all Scenario Outline Example parameters in the Applitools test name
				// if specified in configuration, so we can have unique baselines for each test
				if (useAllScenarioOutlineParametersInTestName) {
					parameterLabelCount = parameterKeys.length;
				}

				for (int p = 0; p < parameterLabelCount; p++) {
					if ((String) parameterKeys[p] != "__index") {
						if (p == 0) {
							parameterDetails += (String) parameterKeys[p] + ": "
									+ parameterLinkMap.get(parameterKeys[p]);
						} else {
							parameterDetails += ", " + (String) parameterKeys[p] + ": "
									+ parameterLinkMap.get(parameterKeys[p]);
						}

						System.out.println(String.format(
								"Applitools Eyes - getScenarioOutlineExampleParameters: Detected %s: %s at Example Line: %d",
								(String) parameterKeys[p], (String) parameterLinkMap.get(parameterKeys[p]),
								(int) parameterLinkMap.get("__index")));
					}

				}
			}
			parameterDetails += ")";

		}
		
		return parameterDetails;
	}
	
	private Eyes getEyes() {
		Eyes eyes;
		boolean isVisualGrid = Boolean.parseBoolean(
				ConfigurationManager.getBundle().getPropertyValueOrNull("applitools.eyes.use_visual_grid"));
		if (isVisualGrid) {
			runner = new VisualGridRunner(new RunnerOptions().testConcurrency(5));
			eyes = new Eyes(runner);
			Configuration config = eyes.getConfiguration();
			config.setLayoutBreakpoints(true);
			config.addBrowser(800, 600, BrowserType.CHROME);
			config.addBrowser(700, 500, BrowserType.FIREFOX);

			config.addBrowser(1600, 1200, BrowserType.IE_11);
			config.addBrowser(1024, 768, BrowserType.EDGE_CHROMIUM);
			config.addBrowser(800, 600, BrowserType.SAFARI);

			config.addDeviceEmulation(DeviceName.iPhone_X, ScreenOrientation.PORTRAIT);
			config.addDeviceEmulation(DeviceName.Pixel_2, ScreenOrientation.PORTRAIT);
			eyes.setConfiguration(config);

		} else {
			runner = new ClassicRunner();
			eyes = new Eyes(runner);
		}

		eyes.setSendDom(true);

		eyes.setBatch(batchInfo);
		System.out.println("Applitools Eyes - beforeScenario - Batch Id: " + batchInfo.getId());

		eyes.setApiKey((String) ConfigurationManager.getBundle().getProperty("applitools.eyes.api_key"));
		
		return eyes;
		
	}

	public void beforeScenario(ITestResult test) {
		TestNGScenario scenario = (TestNGScenario) test.getMethod();
		Map<String, Object> metadata = scenario.getMetaData();
		String featureFileName = (String) metadata.get("reference");
		if(featureFileName == null) {
			featureFileName = "";
		}

		String parameterDetails = getScenarioOutlineExampleParameters(test);


		System.out.println("Applitools Eyes - beforeScenario: " + test.getName());
		System.out.println("Applitools Eyes - beforeScenario: Parameters: " + parameterDetails);
		System.out.println("Applitools Eyes - beforeScenario: Feature File: " + featureFileName);
		System.out.println("Applitools Eyes - beforeScenario: metadata\n" + metadata.toString());

		testThreadId = test.getMethod().getId();

		Eyes eyes = (Eyes) TestBaseProvider.instance().get().getContext().getProperty("eyes");

		if (eyes == null) {
			eyes = getEyes();
		}

		eyes.addProperty("Feature File", featureFileName);

		Integer viewportWidth = Integer.getInteger(
				(String) ConfigurationManager.getBundle().getProperty("applitools.eyes.viewport_width"), 1024);
		Integer viewportHeight = Integer.getInteger(
				(String) ConfigurationManager.getBundle().getProperty("applitools.eyes.viewport_height"), 600);

		// Start the test
		eyes.open((QAFExtendedWebDriver) TestBaseProvider.instance().get().getUiDriver(), appName,
				test.getName() + parameterDetails, new RectangleSize(viewportWidth, viewportHeight));

		System.out.println("Applitools Eyes - beforeScenario - Test Thread ID: " + testThreadId);
		TestBaseProvider.instance().get().getContext().setProperty("eyes", eyes);
	}

	@Override
	public void afterExecute(StepExecutionTracker stepExecutionTracker) {
		Eyes eyes = (Eyes) TestBaseProvider.instance().get().getContext().getProperty("eyes");
		if (eyes != null && eyes.getIsOpen()) {
//			boolean isAutoCheckAfterStep = Boolean.parseBoolean(
//					ConfigurationManager.getBundle().getPropertyValueOrNull("applitools.eyes.auto_check_after_step"));
			String[] cucumberTags = stepExecutionTracker.getScenario().getGroups();
			System.out.println("Applitools Eyes - afterExecute - Groups: " + Arrays.toString(cucumberTags));
			boolean isAutoCheckAfterStep = Arrays.asList(cucumberTags).contains("applitoolsAutoCheckAfterStep");
			
			
			
			if (isAutoCheckAfterStep) {
				String stepDesc = stepExecutionTracker.getStep().getDescription().replaceAll("\"", "");
				System.out.println("Applitools Eyes - afterExecute - Step: " + stepDesc);
				eyes.check(Target.window().fully().withName(stepDesc));
			}
		}
	}

	@Override
	public void onStart(ISuite suite) {
//		isEyesEnabled = Boolean
//				.parseBoolean(ConfigurationManager.getBundle().getPropertyValueOrNull("applitools.eyes.enabled"));
		
		isEyesEnabled = suite.getMethodsByGroups().containsKey("applitools");
		
		if (!isEyesEnabled) {
			System.out.println("Applitools Eyes - onStart - Eyes not enabled!");
			return;
		}
		
		if (batchInfo == null)
			batchInfo = new BatchInfo(suite.getName());
	}

	@Override
	public void onFinish(ISuite suite) {
		if(runner != null) {
			TestResultsSummary results = runner.getAllTestResults(false);
			System.out.println("Applitools Eyes - onFinish - Eyes Test Results:\n" + results.toString());
		}
	}

	@Override
	public void onTestStart(ITestResult result) {
		if (!isEyesEnabled)
			return;
		beforeScenario(result);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		String threadId = result.getMethod().getId();
		Eyes eyes = (Eyes) TestBaseProvider.instance().get().getContext().getProperty("eyes");
		if (eyes != null && eyes.getIsOpen()) {
			TestResults results = eyes.close(false);
			System.out.println(
					"Applitools Eyes - onTestSuccess - Closed Eyes Session for Passed Test Thread ID: " + threadId);
			/****************************
			 * See below for an example of soft asserting test result details
			 ****************************/
//			org.testng.asserts.SoftAssert sa = new org.testng.asserts.SoftAssert();
//			sa.assertFalse(results.isDifferent(), 
//					String.format("Mismatch(es) detected for %d Applitools Eyes steps. Check results at: %s", 
//						results.getMismatches(), 
//						results.getUrl()));
//			sa.assertAll();
		}
	}

	@Override
	public void onTestFailure(ITestResult result) {
		Eyes eyes = (Eyes) TestBaseProvider.instance().get().getContext().getProperty("eyes");
		if (eyes != null && eyes.getIsOpen()) {
			eyes.abort();
			String threadId = result.getMethod().getId();
			System.out.println(
					"Applitools Eyes - onTestFailure - Aborted Eyes Session for Failed Test Thread ID: " + threadId);
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		Eyes eyes = (Eyes) TestBaseProvider.instance().get().getContext().getProperty("eyes");
		if (eyes != null && eyes.getIsOpen()) {
			eyes.abort();
			String threadId = result.getMethod().getId();
			System.out.println(
					"Applitools Eyes - onTestSkipped - Aborted Eyes Session for Skipped Test Thread ID: " + threadId);
		}
	}

	@Override
	public void onFailure(StepExecutionTracker stepExecutionTracker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforExecute(StepExecutionTracker stepExecutionTracker) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart(ITestContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish(ITestContext context) {
		// TODO Auto-generated method stub

	}
}
