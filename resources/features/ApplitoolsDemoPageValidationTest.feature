Feature: APPLITOOLS DEMO PAGE VALIDATION
	
	@applitools @applitoolsUseAllParametersInTestName @applitoolsAutoCheckAfterStep
  Scenario Outline: Launch Applitools Demo and verify visually
    When I go to the "<Page Name>" at url "<Page Url>"
    Then I use Applitools Eyes to verify the Page "<Page Name>" is visually perfect
    And I use Applitools Eyes to verify the entire Page "<Page Name>" is visually perfect
    And I use Applitools Eyes to verify the region of Page "<Page Name>" with CSS selector: "#log-in" is visually perfect

    Examples:
      | Page Name	|	Page Url	|
      | DEMO PAGE |	https://demo.applitools.com/	|
      | SANDBOX PAGE	|	https://sandbox.applitools.com/bank?layoutAlgo=true	|
