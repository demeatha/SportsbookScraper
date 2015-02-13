package sbs.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedCondition;
import java.util.List;
import sbs.config.*;
import org.openqa.selenium.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

public class SportsbookCrawler {
	private WebDriver webDriver = null;
	private JSONCfg cfg = null;
	private Map<String,DataMapper> dataMappers = null;
	private static SportsbookCrawler crawler = null;


	private SportsbookCrawler() {}

	private SportsbookCrawler(WebDriver webDriver, JSONCfg cfg) {
		this.webDriver = webDriver;
		this.cfg = cfg;
		dataMappers = new HashMap<String,DataMapper>();
	}

	public static SportsbookCrawler create(WebDriver webDriver, JSONCfg cfg) {
		if(crawler == null) {
			crawler = new SportsbookCrawler(webDriver,cfg);
			return crawler;
		}
		return crawler;
	}

	/*
	 * This is the entry point method for SportsbookCrawler functionality.
	 *
	 * This method will get the hierarchyLevel you want to parse, the configuration name of the sporsbook you want to parse
	 * and the url which will be a GET request to a the specific sportsbook page.
	 * This method is no more than a wrapper of the two major method implement for crawling purpose.
	 * These methods are:crawlHTMLAttributeValuesUsingXpathInDepth and crawlHTMLAttributeValues.
	 *
	 * The flow of this methos is as follows
	 * Send a get request using 'url'
	 * Loop through the config elements that are almost map an instance of an HTML element for the specific hierarchy level
	 * and return the collection of data wich is the outcome of the results scraped from the page
	 * This collection will be stored into list and fianlly this list will be returned
	 */
	public List<Object> crawlHTML(String hierarchyLevel, String configDriverName, String url) {

		// The list that will contain all the values taken from each element config
		List<Object> resultList = new ArrayList<Object>();

		// Check if url is null and get the currrent url
		if(url == null) {
			url = webDriver.getCurrentUrl();
			if(url.equals("") || url == null) {
				//TODO add logs
				System.out.println("ERROR: Url is not set");
				return null;
			}
		} else {
			// We only need to get the url only if the url param is set
			webDriver.get(url);
		}

		// Get config elements for the given hierarchy level and loop through each config element
		List<ElementJsonObject> configElements = getElements(configDriverName, hierarchyLevel);
		if(isXpathInDepthEnabled(configDriverName,hierarchyLevel)) {
			for(ElementJsonObject configElement : configElements) {
				List<Object> elementCollection = crawlHTMLAttributeValuesUsingXpathInDepth(hierarchyLevel, configDriverName, configElement, webDriver);
				resultList.add(elementCollection);
			}
		} else {
			for(ElementJsonObject configElement : configElements) {
				Object elementCollection = crawlHTMLAttributeValues(hierarchyLevel, configDriverName ,configElement, webDriver);
				resultList.add(elementCollection);
			}
		}
		return resultList;
	}

	public List<Object> crawlHTMLAttributeValuesUsingXpathInDepth(String hierarchyLevel, String configDriverName, ElementJsonObject configElement, SearchContext searchContext) {
		String xpath = configElement.getParentXpath();
		List<WebElement> rootWebElements = searchContext.findElements(By.xpath(xpath));
		List<Object> resultList = new ArrayList<Object>();
		for(WebElement rootElement : rootWebElements) {
			Object result = crawlHTMLAttributeValues(hierarchyLevel, configDriverName, configElement, rootElement);
			resultList.add(result);
		}
		return resultList;
	}

	/*
	 * This is the crawling algorithm that uses Selenium WebDriver to get all html attribute values that are needed for each level.
	 *
	 * This algorithm will get the attribute values by parsing all xpaths one by one.
	 * This technique can only be used only in case of all of the attributes values are associated one by one.
	 * One by association is considered as a default, raw, association between values.
	 * A callback called defineFinalStracture will be called in case we don't want one by one association.
	 *
	 * The loops into algorithm are parsing:
	 * The first loop is getting the attribute alias specified from the config.
	 * The second one gets the xpaths setted for this attribute. In this loop an extra check is applied so that duplicate result get avoided.
	 * The third one gets all html attribute values that matched with the given xpath.
	 * This method will be executed for each ElementJsonObject instance that EventHierarchy level may have
	 */
	public Object crawlHTMLAttributeValues(String hierarchyLevel, String configDriverName, ElementJsonObject configElement, SearchContext searchContext) {

		// All the values stored into this map. Each List has the values retrieved from the specific attribute
		Map<String, List<String>> resultMap = new HashMap<String,List<String>>();
		
		// Get the attribute aliases for this ElementJsonObject
		Set<String> attributeList = configElement.attributeKeys();
		for (String attributeAlias : attributeList) {

			List<String> htmlAttributeValues = new ArrayList<String>();
			
			// Loop through attributes xpath
			// Size will be greater than 1 when the element is result of linked Elements or attribute value that needs concatanation.
			int attrXpathSize = configElement.xpathFor(attributeAlias).size();
			for (int i = 0; i < attrXpathSize; i++) {
				// Get the webElements for the attribute
				String xpath = configElement.xpathFor(attributeAlias).get(i);
				List<WebElement> webElements = searchContext.findElements(By.xpath(xpath));
				for (int e = 0; e < webElements.size(); e++) {
					WebElement webElement = webElements.get(e);
					/* In case of multiple xpaths then, attribute values merging must be done
					 * If WebElement lists hasn't the same size then the result will continue with emtpy strings
					 * If is greater than the initial list then an exception will be thrown and data will be added as new list elements
					 * if is smaller then merging will be completed up to a point
					 */
					if(i > 0) {
						try {
							String currentValue = htmlAttributeValues.get(e);
							String newValue = getAttributeText(configElement.attributeFor(attributeAlias).get(i),webElement,webDriver);
							htmlAttributeValues.set(e, currentValue + " " + newValue.trim());
						} catch(IndexOutOfBoundsException exception) {
							//TODO add logs
							System.out.println("WARNING: WebElement lists mismatch between the initial and the next list values");
							String newValue = getAttributeText(configElement.attributeFor(attributeAlias).get(i),webElement,webDriver);
							htmlAttributeValues.add(newValue.trim());
						}
					} else {
						String value = getAttributeText(configElement.attributeFor(attributeAlias).get(i),webElement,webDriver);
						htmlAttributeValues.add(value.trim());
						//System.out.println("Element name " + attributeAlias + ": " + value.trim());
					}
				}
			}
			resultMap.put(attributeAlias,htmlAttributeValues);
		}

		/*
		 * The fomrat of result map will be Map{attrAlias:list(value)}
		 *
		 * Although the return value can has a totally different format
		 */
		if(getDataMapperFor(configDriverName) == null) {
			//TODO add logs
			System.out.println("WARNING: Data mapper for " + configDriverName + " not found, result defaults to raw data format.");
			return resultMap;
		}

		return getDataMapperFor(configDriverName).mapResultStructure(hierarchyLevel,resultMap);
	}

	/*
	 * A simple getter for objects that implement DataMapper interface
	 */
	public DataMapper getDataMapperFor(String configDriverName) {
		if(dataMappers.isEmpty()) {
			//TODO Add logs
			System.out.println("WARNING: No DataMapper is set in SportsbookCrawler");
		}
		return dataMappers.get(configDriverName);
	}

	/*
	 * Simple setter method to add data mappers into the object
	 */
	public void addDataMapper(String nameAlias, DataMapper mapper) {
		//TODO add logs
		System.out.println("INFO: DataMapper with name " + nameAlias + " set");
		dataMappers.put(nameAlias,mapper);
	}

	/*
	 * Helper method to retrieve attributes or elements enclosed text, either the element is hidden or not. 
	 *
	 * This is a wrapping method, that returns attribute or nodes text without check what of these two have actually be requested.
	 * If attribute cannot be found then is searching for the text. Actually assumes that if attribute not found into xpath is set,
	 * then the default value will return is the text enclosed into the node that is reffered from the xpath.
	 */
	public String getAttributeText(String attributeName, WebElement e, SearchContext searchContext) {
		if(attributeName.equals("text")) {
			return getText(searchContext,e);
		}
		String attr  = e.getAttribute(attributeName);
		if(attr == null) {
			return " ";
		}
		return attr;
	}

	/*
	 * Helper method that wraps the configDriver so as we get only the Element which currently needed for this component.
	 *
	 * Based on the hierarchy level you set a list of configElements will be returned.
	 */
	public List<ElementJsonObject> getElements(String driverName, String hierarchyLevel) {
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
		} else if (hierarchyLevel == "EvMarketSelection") {
			return cfg.driver(driverName)
				.hierarchy()
				.levelScopeMarketSelection()
				.elementsList();
		} else {
			System.out.println("Wrong hierarchy level set");
			return null;
		}
	}

	/*
	 * Helper method that wraps the configDriver so as we get the flag that will define if we need to call xpath in depth or regular parsing.
	 *
	 * A boolean value will be returned that defines if xpathInDepth attribute is set for the specific hierarchy Level.
	 */
	public boolean isXpathInDepthEnabled(String driverName, String hierarchyLevel) {
		if (hierarchyLevel == "EvClass") {
			return cfg.driver(driverName)
				.hierarchy()
				.levelScopeClass()
				.isXpathInDepthEnabled();
		} else if (hierarchyLevel == "EvType") {
			return cfg.driver(driverName)
				.hierarchy()
				.levelScopeType()
				.isXpathInDepthEnabled();
		} else if (hierarchyLevel == "Event") {
			return cfg.driver(driverName)
				.hierarchy()
				.levelScopeEvent()
				.isXpathInDepthEnabled();
		} else if (hierarchyLevel == "EvMarketSelection") {
			return cfg.driver(driverName)
				.hierarchy()
				.levelScopeMarketSelection()
				.isXpathInDepthEnabled();
		} else {
			System.out.println("Wrong hierarchy level set");
			return false;
		}
	}

	/*
	 * Helper method that inject a jQuery snippet to retrive the text from both hidden and non hidden elements.
	 */
	public String getText(SearchContext searchContext, WebElement element){
	    return (String) ((JavascriptExecutor) searchContext).executeScript(
		"return jQuery(arguments[0]).text();", element);
	}
}