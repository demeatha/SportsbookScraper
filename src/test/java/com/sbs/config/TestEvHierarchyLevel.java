package sbs.config;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.json.JSONObject;
import sbs.config.WebDriverJsonObject;
import org.json.JSONException;


public class TestEvHierarchyLevel {
	private JSONObject object;
	private EvHierarchyLevel level;
	
	@Before
	public void setUp () {
		String driverConfigSample = "{Elements: [{xpath:\"//div/a/blabla\",attributes:{url:\"href\",name:\"text\"}},"
			+"{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",name:\"texteeee\"}}],linkedElements: [[1,2]]}";
		JSONObject object = new JSONObject(driverConfigSample);
		level = new EvHierarchyLevel(object);
	}

	@After
	public void tearDown () {
		JSONCfgError.flush();
	}

	@Test
	public void testElementJsonObjectIsReturned () {
		Assert.assertNotNull("Unit test failed, reason: Failed to get a ElementJsonObject instance",level.getElement(0));
	}

	@Test
	public void testNullReturnedInstedOfElementJsonObjectIsReturned () {
		// Based on sample config we have only one element.
		Assert.assertNull("Unit test failed, reason: Null Expected, ElementJsonObject returned",level.getElement(1));
	}

	@Test
	public void testElementsLength () {
		// Check that length is ok
		Assert.assertEquals("Unit test failed, reason: Null Expected, ElementJsonObject",1,level.elementsList().size());
	}
}