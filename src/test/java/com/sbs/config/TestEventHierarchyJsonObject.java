package sbs.config;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.json.JSONObject;
import sbs.config.WebDriverJsonObject;
import org.json.JSONException;


public class TestEventHierarchyJsonObject {
	private JSONObject object;
	private EventHierarchyJsonObject eventHier;
	private EventHierarchyJsonObject eventHierF;
	@Before
	public void setUp () {
		String driverConfigSample = "{EvClass: {Elements: [{xpath:\"//div/a/blabla\",attributes:{url:\"href\",name:\"text\"}}]},"
		      + "EvType: {Elements: [{xpath:\"//div/a/blabla\",attributes:{url:\"href\",name:\"text\"}}]},"
		      +"Event: {Elements: [{xpath:\"//div/a/blabla\",attributes:{url:\"href\",name:\"text\"}}]}}";
		JSONObject object = new JSONObject(driverConfigSample);
		eventHier = new EventHierarchyJsonObject(object);

		JSONObject objectF = new JSONObject("{}");
		eventHierF = new EventHierarchyJsonObject(objectF);
	}

	@After
	public void tearDown () {
		JSONCfgError.flush();
	}

	@Test
	public void testEventClassJsonObjectReturned () {
		Assert.assertNotNull("Unit test failed, reason: EventClassObject not returned",eventHier.levelScopeClass());
	}

	@Test
	public void testEventTypeJsonObjectReturned () {
		// Based on sample config we have only one element.
		Assert.assertNotNull("Unit test failed, reason: EventTypeJsonObject not returned",eventHier.levelScopeType());
	}

	@Test
	public void testEventJsonObjectReturned () {
		// Check that length is ok
		Assert.assertNotNull("Unit test failed, reason: EventJsonObject not returned",eventHier.levelScopeEvent());
	}

	@Test
	public void testEventClassJsonObjectNotFound () {
		Assert.assertNull("Unit test failed, reason: Result should be null EventClassObject found",eventHierF.levelScopeClass());
	}

	@Test
	public void testEventTypeJsonObjectNotFound () {
		// Based on sample config we have only one element.
		Assert.assertNull("Unit test failed, reason: Result should be null EventTypeJsonObject found",eventHierF.levelScopeType());
	}

	@Test
	public void testEventJsonObjectNotFound () {
		// Check that length is ok
		Assert.assertNull("Unit test failed, reason: Result should be null EventJsonObject found",eventHierF.levelScopeEvent());
	}
}