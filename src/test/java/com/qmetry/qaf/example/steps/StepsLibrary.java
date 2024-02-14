package com.qmetry.qaf.example.steps;

import static com.qmetry.qaf.automation.step.CommonStep.click;
import static com.qmetry.qaf.automation.step.CommonStep.sendKeys;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.qmetry.qaf.automation.core.TestBaseProvider;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;

@QAFTestStepProvider
public class StepsLibrary{
	/**
	 * @param searchTerm
	 *            : search term to be searched
	 */
	@QAFTestStep(description = "search for {0}")
	public static void searchFor(String searchTerm) {
		QAFExtendedWebDriver driver = (QAFExtendedWebDriver) TestBaseProvider.instance().get().getUiDriver();
		
		if(driver.findElements(By.name("callout")).size() > 0) {
			driver.switchTo().frame("callout");
			click("button.staysignedout");
			driver.switchTo().defaultContent();
		}
		sendKeys(searchTerm + Keys.TAB, "input.search");
		click("button.search");
	}
}
