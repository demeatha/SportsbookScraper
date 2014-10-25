package sbs.config;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.json.JSONObject;
import sbs.config.WebDriverJsonObject;
import org.json.JSONException;

public class TestWebDriverJsonObject {
	private JSONObject object;

	@Before
	public void setUp () {
		String driverConfigSample = "{SportingBetDriver:{"
			+"url: \"http://www.sportinbet.gr\","
			+" EventHierarchy:{Evclass: {Elements: [{xpath:\"//div/a/blabla\",attributes:{url:\"href\",name:\"text\"}},"
			+"{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",name:\"texteeee\"}}],linkedElements: [[1,2]]}}}}";
		object = new JSONObject(driverConfigSample);
	}

	@After
	public void tearDown () {
		JSONCfgError.flush();
	}

	@Test
	public void testDriverJsonObjectInstanceWasSuccesfullyReturned () {
		WebDriverJsonObject.create(object);
		Assert.assertNotNull("Unit test failed, reason: Failed to get a WebDriverJsonObject instance",WebDriverJsonObject.getDriverWithName("SportingBetDriver"));
	}

	@Test
	public void testDriverJsonObjectInstanceIsNotReturned () {
		WebDriverJsonObject.create(object);
		Assert.assertNull("Unit test failed, reason: Null Expected, WebDriverJsonObject returned",WebDriverJsonObject.getDriverWithName("NonExistendDriverName"));
	}
}