package sbs.config;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

public class TestElementJsonObject{
	private JSONObject object;
	private ElementJsonObject element;
	
	@Before
	public void setUp () {
		String driverConfigSample = "{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",name:\"text\",+name:\"second text\"}}";
		JSONObject object = new JSONObject(driverConfigSample);
		element = new ElementJsonObject(object);
	}

	/*
	 * Flushing, to avoid blocking tests with errors raised on other unit tests.
	 */
	@After
	public void tearDown () {
		JSONCfgError.flush();
	}

	@Test
	public void testAttributeForReturnedAllValuesIncludingPrefixOnes () {
		List<String> urlAttr = element.attributeFor("url");
		List<String> nameAttr = element.attributeFor("name");
		
		Assert.assertEquals("Unit test failed, reason: Couldn't match url attribute value href","href",urlAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","text",nameAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","second text",nameAttr.get(1));
	}

	@Test
	public void testFailedToFindAttributeForKeyNullValueReturned () {
		// Based on sample config we have only one element.
		Assert.assertNull("Unit test failed, reason: Null Expected, ElementJsonObject attribute value is not empty",element.attributeFor("notExist"));
	}

	@Test
	public void testAllXpathsMatch () {
		List<String> urlXpath = element.xpathFor("url");
		List<String> nameXpath = element.xpathFor("name");

		Assert.assertEquals("Unit test failed, reason: Couldn't match url xpath value href","//div/a/bloblou/blou",urlXpath.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name xpath value name","//div/a/bloblou/blou",nameXpath.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name xpath value name","//div/a/bloblou/blou",nameXpath.get(1));
	}

	@Test
	public void testFailedToFindXpathForKeyNullValueReturned () {
		// Based on sample config we have only one element.
		Assert.assertNull("Unit test failed, reason: Null Expected, ElementJsonObject xpath value is not empty",element.xpathFor("notExist"));
	}

	@Test
	public void testAttributeForReturnedAllValuesIncludingPostfixOnes () {
		String driverConfigSample = "{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",name:\"text\",name+:\"second text\"}}";
		JSONObject object = new JSONObject(driverConfigSample);
		element = new ElementJsonObject(object);

		List<String> urlAttr = element.attributeFor("url");
		List<String> nameAttr = element.attributeFor("name");
		
		Assert.assertEquals("Unit test failed, reason: Couldn't match url attribute value href","href",urlAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","second text",nameAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","text",nameAttr.get(1));
	}

	@Test
	public void testAttributeForWarnsWithMessageForPostfixPrecedence () {
		String driverConfigSample = "{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",name+:\"text\",name:\"second text\"}}";
		JSONObject object = new JSONObject(driverConfigSample);
		element = new ElementJsonObject(object);

		List<String> urlAttr = element.attributeFor("url");
		List<String> nameAttr = element.attributeFor("name");
		
		Assert.assertEquals("Unit test failed, reason: Couldn't match url attribute value href","href",urlAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","text",nameAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","second text",nameAttr.get(1));
	}

	@Test
	public void testAttributeForWarnsWithMessageForPrefixPrecedence () {
		String driverConfigSample = "{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",+name:\"text\",name:\"second text\"}}";
		JSONObject object = new JSONObject(driverConfigSample);
		element = new ElementJsonObject(object);

		List<String> urlAttr = element.attributeFor("url");
		List<String> nameAttr = element.attributeFor("name");
		
		Assert.assertEquals("Unit test failed, reason: Couldn't match url attribute value href","href",urlAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","text",nameAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","second text",nameAttr.get(1));
	}

	@Test
	public void testAttributeForValuesComeFromMultipleJSONObjectInstances () {
		String driverConfigSampleA = "{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",name:\"text from A\",name+:\"second text from A\"}}";
		String driverConfigSampleB = "{xpath:\"//div/a/bloblou/blou\",attributes: {+name:\"text from B\",name:\"second text from B\"}}";
		List<JSONObject> objects = new ArrayList<JSONObject>();
		JSONObject objectA = new JSONObject(driverConfigSampleA);
		JSONObject objectB = new JSONObject(driverConfigSampleB);
		objects.add(objectA);
		objects.add(objectB);
		element = new ElementJsonObject(objects);

		List<String> urlAttr = element.attributeFor("url");
		List<String> nameAttr = element.attributeFor("name");
		
		Assert.assertEquals("Unit test failed, reason: Couldn't match url attribute value href","href",urlAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","second text from A",nameAttr.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","text from A",nameAttr.get(1));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","text from B",nameAttr.get(2));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","second text from B",nameAttr.get(3));
	}

	@Test
	public void testXpathForValuesComeFromMultipleJSONObjectInstances () {
		String driverConfigSampleA = "{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",name:\"text from A\",name+:\"second text from A\"}}";
		String driverConfigSampleB = "{xpath:\"//div/a/blabla/bla\",attributes: {name+:\"text from B\",name:\"second text from B\"}}";
		List<JSONObject> objects = new ArrayList<JSONObject>();
		JSONObject objectA = new JSONObject(driverConfigSampleA);
		JSONObject objectB = new JSONObject(driverConfigSampleB);
		objects.add(objectA);
		objects.add(objectB);
		element = new ElementJsonObject(objects);

		List<String> urlXpath = element.xpathFor("url");
		List<String> nameXpath = element.xpathFor("name");
		
		Assert.assertEquals("Unit test failed, reason: Couldn't match url attribute value href","//div/a/bloblou/blou",urlXpath.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","//div/a/bloblou/blou",nameXpath.get(0));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","//div/a/blabla/bla",nameXpath.get(1));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","//div/a/bloblou/blou",nameXpath.get(2));
		Assert.assertEquals("Unit test failed, reason: Couldn't match name attribute value name","//div/a/blabla/bla",nameXpath.get(3));
	}

	@Test
	public void testXpathForAttributeForInvalidPropertyNameAdded () {
		String driverConfigSampleA = "{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",name:\"text from A\",name+:\"second text from A\"}}";
		String driverConfigSampleB = "{xpath:\"//div/a/blabla/bla\",attributes: {name+:\"text from B\",+name+:\"second text from B\"}}";
		List<JSONObject> objects = new ArrayList<JSONObject>();
		JSONObject objectA = new JSONObject(driverConfigSampleA);
		JSONObject objectB = new JSONObject(driverConfigSampleB);
		objects.add(objectA);
		objects.add(objectB);
		element = new ElementJsonObject(objects);

		List<String> urlXpath = element.xpathFor("url");
		List<String> nameXpath = element.xpathFor("name");
		
		Assert.assertNull("Unit test failed, reason: Null Expected, ElementJsonObject attribute value is not empty",element.attributeFor("name"));
		Assert.assertNull("Unit test failed, reason: Null Expected, ElementJsonObject attribute value is not empty",element.xpathFor("url"));
	}

	@Test
	public void testXpathForAttributeForMandatoryPropertiesAreMissing() {
		String driverConfigSampleA = "{xpath:\"//div/a/bloblou/blou\",attributes: {url:\"href\",name:\"text from A\",name+:\"second text from A\"}}";
		String driverConfigSampleB = "{xpathee:\"//div/a/blabla/bla\",attributesss: {name+:\"text from B\",+name+:\"second text from B\"}}";
		List<JSONObject> objects = new ArrayList<JSONObject>();
		JSONObject objectA = new JSONObject(driverConfigSampleA);
		JSONObject objectB = new JSONObject(driverConfigSampleB);
		objects.add(objectA);
		objects.add(objectB);
		element = new ElementJsonObject(objects);

		List<String> urlXpath = element.xpathFor("url");
		List<String> nameXpath = element.xpathFor("name");
		
		Assert.assertNull("Unit test failed, reason: Null Expected, ElementJsonObject attribute value is not empty",element.attributeFor("name"));
		Assert.assertNull("Unit test failed, reason: Null Expected, ElementJsonObject attribute value is not empty",element.xpathFor("url"));
	}
 
}