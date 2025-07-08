package acceptance;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("acceptance/features")
@ConfigurationParameter(key = "cucumber.glue", value = "acceptance.stepdefs")
public class RunCucumberTest {}
