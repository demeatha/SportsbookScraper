package sbs;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import java.util.List;
import sbs.config.*;
import sbs.model.*;
import org.openqa.selenium.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;


/*
 * Gathering and testing GhostDriver and selenium WebDriver API
 */
public class TestGhostDriver {
	private static WebDriver driver;
	private static JSONCfg cfg;

	@Before
	public void setUp () throws Exception {
		/*
		* Configure GhostDriver
		*/
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setJavascriptEnabled(true);
		caps.setCapability("takesScreenshot", false);
		//This line will start phantomjs service
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,"phantomJS/phantomjs-1.9.7-linux-x86_64/bin/phantomjs");
		driver = new PhantomJSDriver(caps);

		// setup JSOCfg
		 cfg = JSONCfg.create("resources/crawler.json");
	}

	@Test
	public void testTheAlgo () {
	
		//String url = cfg.driver("SportingBetDriver").url();
		TestGhostDriver.mainTestAlgo();
		driver.quit();
	}

	public static void mainTestAlgo() {
		try {
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		} catch (TimeoutException ex) {
			System.out.println("Timeout exception");
		}
		System.out.println(cfg.driver("SportingBetDriver").url());
		driver.get(cfg.driver("SportingBetDriver").url());

		List<ElementJsonObject> elements = TestGhostDriver.getElements("SportingBetDriver", "EvClass");
		Map<String, List<String>> classes =null;
		for(ElementJsonObject element : elements) {
			classes = TestGhostDriver.crawlHTMLAttributeValues("EvClass", element, driver);
		}
		
		
		elements = TestGhostDriver.getElements("SportingBetDriver", "EvType");
		for (String typeUrl : classes.get("url")) {
			System.out.println("Searching types into: " + typeUrl);
			driver.get(typeUrl);
			Map<String, List<String>> types = null;
			for(ElementJsonObject element : elements) {
				types = TestGhostDriver.crawlHTMLAttributeValues("EvType", element, driver);
			}

			try {
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			} catch (TimeoutException ex) {
				System.out.println("Timeout exception");
			}

			elements = TestGhostDriver.getElements("SportingBetDriver", "Event");
			for (String eventUrl : types.get("url")) {
				System.out.println("Searching events into: " + eventUrl);
				driver.get(eventUrl);
				for(ElementJsonObject element : elements) {
					Map<String, List<String>> events = TestGhostDriver.crawlHTMLAttributeValues("Event", element, driver);
				}
			}
		}
	}

	public static List<EvObject> getEvHierarchyObjects (String configDriverName, EvObject eventObject, String hierarchyLevel) {
		// send the request
		String url = eventObject.getUrl();
		driver.get(url);

		List<ElementJsonObject>  elements = TestGhostDriver.getElements(configDriverName, hierarchyLevel);
		Map<String, List<String>> elementValues = null;
		for(ElementJsonObject element : elements) {
			elementValues = TestGhostDriver.crawlHTMLAttributeValues(hierarchyLevel, element, driver);
		}
		return elementsMapToElementObjects(eventObject, elementValues);
	}

	public static List<EvObject> elementsMapToElementObjects(EvObject parent, Map<String,List<String>> elementValues) {
		
	}

	/*
	 * This is the crawling algorithm that uses Selenium WebDriver to get all html attribute values that are needed for each level.
	 *
	 * The first loop is getting the attribute specified from the config.
	 * The second one gets the xpaths setted for this attribute. In this loop an extra check is applied so that duplicate result get avoided.
	 * The third one gets all html attribute values that matched with the given xpath.
	 * This method will be executed for each ElementJsonObject instance that EventHierarchy level may have
	 */
	public static Map<String,List<String>> crawlHTMLAttributeValues(String hierarchyLevel, ElementJsonObject configElement, WebDriver driver) {

		// All the values stored into this map. Each List has the values retrieved from the specific attribute
		Map<String, List<String>> resultMap = new HashMap<String,List<String>>();
		
		// Get the attribute keys for this ElementJsonObject
		Set<String> expectedAttrList = configElement.attributeKeys();
		for (String attributeName : expectedAttrList) {
			List<String> htmlAttributeValues = new ArrayList<String>();
			
			// Loop through attributes xpath
			// Size will be greater than 1 when the element is result of linked Elements or attribute value that needs concatanation.
			int attrXpathSize = configElement.xpathFor(attributeName).size();
			for (int i = 0; i < attrXpathSize; i++) {
				// Get the webElements for the attribute
				String xpath = configElement.xpathFor(attributeName).get(i);
				List<WebElement> evClassElements = driver.findElements(By.xpath(xpath));
				for (WebElement e: evClassElements) {
					String value = TestGhostDriver.getAttributeText(configElement.attributeFor(attributeName).get(i),e,driver);
					if (value != null) {
						htmlAttributeValues.add(value.trim());
						System.out.println("Element name " + attributeName + ": " + value.trim());
					}
				}
			}
			resultMap.put(attributeName,htmlAttributeValues);
		}
		return resultMap;
	}

	/*
	 * Helper method to retrieve attributes or elements enclosed text, either the element is hidden or not. 
	 *
	 * This is a wrapping method, that returns attribute or nodes text without check what of these two have actually be requested.
	 * If attribute cannot be found then is searching for the text. Actually assumes that if attribute not found into xpath is set,
	 * then the default value will return is the text enclosed into the node that is reffered from the xpath.
	 */
	public static String getAttributeText(String attributeName, WebElement e, WebDriver d) {
		String attr  = e.getAttribute(attributeName);
		if (attr == null) {
			attr = TestGhostDriver.getText(d,e);
		}
		return attr;
	}

	/*
	 * Helper method that wraps the configDriver so as we get only the Element which currently needed for this component.
	 *
	 * Based on the hierarchy level you set a list of configElements will be returned.
	 */
	public static List<ElementJsonObject> getElements(String driverName, String hierarchyLevel) {
		if (hierarchyLevel == "EvClass") {
			return cfg.driver(driverName)
				.hierarchy()
				.levelScopeClass()
				.elementsList();
		} else if (hierarchyLevel == "EvType") {
			return cfg.driver(driverName)
				.hierarchy()
				.levelScopeType()
				.elementsList();
		} else if (hierarchyLevel == "Event") {
			return cfg.driver(driverName)
				.hierarchy()
				.levelScopeEvent()
				.elementsList();
		} else {
			System.out.println("Wrong hierarchy level set");
			return null;
		}
	}

	/*
	 * Helper method that inject a jQuery snippet to retrive the text from both hidden and non hidden elements.
	 */
	public static String getText(WebDriver driver, WebElement element){
	    return (String) ((JavascriptExecutor) driver).executeScript(
		"return jQuery(arguments[0]).text();", element);
	}
}