package com.qmetry.qaf.example.steps;

import org.openqa.selenium.By;

import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.fluent.Target;
import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.core.TestBaseProvider;
import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebDriver;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

@QAFTestStepProvider
public class ApplitoolsSteps {
	@When("I go to the Applitools Demo Page url")
    public void i_go_to_the_applitools_demo_page_url() {
		QAFExtendedWebDriver driver = (QAFExtendedWebDriver) TestBaseProvider.instance().get().getUiDriver();
        driver.get("https://sandbox.applitools.com/bank?layoutAlgo=true");
    }
	
	@When("I go to the \"([^\"]*)\" at url \"([^\"]*)\"")
    public void i_go_to_the_page_at_url(String pageName, String pageUrl) {
		QAFExtendedWebDriver driver = (QAFExtendedWebDriver) TestBaseProvider.instance().get().getUiDriver();
        driver.get(pageUrl);
    }
	
	@Then("^I use Applitools Eyes to verify the Page \"([^\"]*)\" is visually perfect$")
	public void i_use_eyes_to_verify_the_page_displays(String page) throws Throwable {
		Eyes eyes = (Eyes) TestBaseProvider.instance().get().getContext().getProperty("eyes");
		if(eyes != null) {
			eyes.check(Target.window().withName(page));
		}
	}
	
	@Then("^I use Applitools Eyes to verify the entire Page \"([^\"]*)\" is visually perfect$")
	public void i_use_eyes_to_verify_entire_the_page_displays(String page) throws Throwable {
		Eyes eyes = (Eyes) TestBaseProvider.instance().get().getContext().getProperty("eyes");
		if(eyes != null) {
			eyes.check(Target.window().fully().withName(page));
		}
	}
	
	@Then("^I use Applitools Eyes to verify the region of Page \"([^\"]*)\" with CSS selector: \"([^\"]*)\" is visually perfect$")
	public void i_use_eyes_to_verify_the_region_of_page_displays(String page, String cssSelector) throws Throwable {
		Eyes eyes = (Eyes) TestBaseProvider.instance().get().getContext().getProperty("eyes");
		if(eyes != null) {
			eyes.check(Target.region(By.cssSelector(cssSelector)).withName(page));
		}
	}
	
	@QAFTestStep(description = "check {0} with Applitools Eyes")
	public static void checkWithApplitoolsEyes(String checkpointTag) {
		Eyes eyes = (Eyes) TestBaseProvider.instance().get().getContext().getProperty("eyes");
		if(eyes != null) {
			eyes.check(Target.window().fully().withName(checkpointTag));
		}
	}
}
