package sbs.config;


import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import java.util.Properties;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.Collections;
import java.util.Arrays;

/*
 * Class that maps properties with xpath and attribute values.
 *
 */
public class ElementJsonObject {

	/*
	 * Element's mandatory properties
	 */
	private Map<String,List<String>> xpathMap     = null;
	private Map<String,List<String>> attributeMap = null;

	/*
	 * Only for xpathInDepth flag is used for the parent xpath
	 */
	 private String parentXpath = null;
 
	/*
	 * Keeping json components
	 */
	private List<JSONObject> elements      = null;
	/*
	 * Class name
	 */
	private static final String className         = ElementJsonObject.class.getName();

	private ElementJsonObject() {}

	protected ElementJsonObject (JSONObject element, boolean xpathInDepth) {

		this.elements = new ArrayList<JSONObject>();
		this.elements.add(element);

		xpathMap     = new HashMap<String,List<String>>();
		attributeMap = new HashMap<String,List<String>>();
		if(mapElementProperties(element) == false) {
			//TODO add logs
			System.out.println(className + " Object mapping failure");
			JSONCfgError.add(className + " object mapping failure");
			return;
		}

		// If xpathInDepth option has been provided then extra handling need to be done on xpathMap and attributeMap as well.
		boolean mappingOK = mapAttributeListsForXpathInDepth(xpathInDepth);
		if(!mappingOK) {
			//TODO add logs
			System.out.println(className + " Object mapping failure, reason: " + JSONCfgError.getLastError());
			JSONCfgError.add(className + " Object mapping failure");
		}
	}

	protected ElementJsonObject (List<JSONObject> elements) {
		this.elements = elements;
		xpathMap     = new HashMap<String,List<String>>();
		attributeMap = new HashMap<String,List<String>>();

		for (int i = 0; i < elements.size(); i++) {
			boolean mappingOK = mapElementProperties(elements.get(i));
			
			if(!mappingOK) {
				//TODO add logs
				System.out.println(className + " Object mapping failure, reason: " + JSONCfgError.getLastError());
				JSONCfgError.add(className + " Object mapping failure");
				return;
			}
		}
	}

	/*
	 * Map all the aliases with an xpath, and attribute
	 *
	 * This method will map the attribute aliases with the xpath and also we'll keep in a separate
	 * Map the aliases with the html attribute which defines them.
	 */
	private boolean mapElementProperties (JSONObject element) {
		if(validateElementProperties(element) == false) {
			//TODO add logs
			System.out.println(className + " validation failure: Element properties failed to pass validation check");
			JSONCfgError.add(className + " validation failure: Element properties failed to pass validation check");
			xpathMap     = null;
			attributeMap = null;
			return false;
		}

		// Map the xpathMap and the attributeMap base on attribute property names
		String xpath = element.getString("xpath");
		//In case of xpathInDepth flag this xpath above will be the unique parent xpath
		parentXpath = xpath;
		JSONObject attributes = element.getJSONObject("attributes");

		String[] attrPropertyNames  = JSONObject.getNames(attributes);
		for (int i = 0; i < attrPropertyNames.length; i++) {
			String key = attrPropertyNames[i];
			/*
			 * Unify process
			 * we need to identify if key has special postfix or prefix symbol '+'
			 */
			String keyTrimmedPref = key.replaceAll("^\\+", "");
			String keyTrimmedPostf = key.replaceAll("\\+$", "");
			if (!keyTrimmedPref.equals(key) && !keyTrimmedPostf.equals(key)) {
				//TODO Add logs
				System.out.println(className + " Error: '+' is a special property charachter which you can't use simultaneously as prefix and postfix");
				JSONCfgError.add(className + " Error: '+' is a special property charachter which you can't use simultaneously as prefix and postfix");
				xpathMap     = null;
				attributeMap = null;
				return false;
			}

			if (!keyTrimmedPref.equals(key)) {
				List<String> xpathMapList = xpathMap.get(keyTrimmedPref);
				List<String> attrMapList  = attributeMap.get(keyTrimmedPref);
				if(xpathMapList != null) {
					xpathMapList.add(xpath);
					attrMapList.add(attributes.getString(key));
					continue;
				} else {
					System.out.println("Warning the " + key + " is the first element identified, omitting '+' prefix");
				}
			}

			if (!keyTrimmedPostf.equals(key)) {
				List<String> xpathMapList = xpathMap.get(keyTrimmedPostf);
				List<String> attrMapList  = attributeMap.get(keyTrimmedPostf);
				if(xpathMapList != null) {
					int xpathLstSize = xpathMapList.size();
					xpathMapList.add(xpathLstSize-1, xpath);
					int attrLstSize = attrMapList.size();
					attrMapList.add(attrLstSize-1, attributes.getString(key));
					continue;
				} else {
					System.out.println("Warning the " + key + " is the first element identified, omitting '+' postfix");
				}
			}


			/*
			 * In case of linked elements List overidance may happen. Warn the user, and append the element that has added.
			 * Postfix cases will be catched from the if conditions above but in case of duplicate key in different object,
			 * this snippet will prevent List overidance
			 */
			List<String> xpathLst = null;
			List<String> attrLst =  null;
			if (attributeMap.get(key) != null) {
				//TODO add logs
				System.out.println("Warning the " + key + " has already been added, Append element into the list.");
				xpathLst = xpathMap.get(key);
				attrLst  = attributeMap.get(key);
				
				
			} else {
				xpathLst =  new ArrayList<String>();
				attrLst =  new ArrayList<String>();
			}

			// if a user mistakenly added first element with + we trim them and save it
			String keyTrimmed = key.replaceAll("^\\+", "").replaceAll("\\+$", "");

			xpathLst.add(xpath);
			xpathMap.put(keyTrimmed,xpathLst);


			attrLst.add(attributes.getString(key));
			attributeMap.put(keyTrimmed,attrLst);
		}
		return true;
	}

	/*
	 * This method will get object attributes xpathMap and attributeMap and will split based on '--' special character
	 * all the xpaths from attribute key value and will keep only the attribute name into the attributeMap
	 * 
	 * Before: attributeMap://div/span/a--href 
	 * After:  xpathMap: //div/span/a, attributeMap: href
	 *
	 */
	private boolean mapAttributeListsForXpathInDepth(boolean xpathInDepth) {
		if(!xpathInDepth) {
			return true;
		}

		// Get the attributes one by one for both attributeMap and xpathMap have the same Keys
		for(String key : attributeMap.keySet()) {
			List<String> newAttrLst  = new ArrayList<String>();
			List<String> newXpathLst = new ArrayList<String>();
			for (String attributeVal : attributeMap.get(key)) {
				String[] attrSplitted = attributeVal.split("--");
				if(attrSplitted.length != 2) {
					System.out.println(className + " ERROR array length is invalid please check your configuration.");
					JSONCfgError.add(className + " ERROR array length is invalid please check your configuration.");
					return false;
				}
				newXpathLst.add(attrSplitted[0]);
				newAttrLst.add(attrSplitted[1]);
			}
			xpathMap.put(key,newXpathLst);
			attributeMap.put(key,newAttrLst);
		}

		return true;
	}

	/*
	 * Validation method for ElementJsonObject component
	 *
	 * Check if attributes and xpath properties exist.
	 * Check if the properties has value with correct type.
	 */
	private boolean validateElementProperties (JSONObject element) {
		if (!element.has("attributes")) {
			//TODO add logs
			JSONCfgError.add(className + ": Mandatory property attributes is missing");
			System.out.println(className + ": Mandatory property attributes is missing");
			return false;
		}

		if (!element.has("xpath")) {
			//TODO add logs
			JSONCfgError.add(className + ": Mandatory element property xpath is missing");
			System.out.println(className + ": Mandatory element property xpath is missing");
		}

		String xpath = element.optString("xpath",null);
		if (xpath == null) {
			//TODO add logs
			JSONCfgError.add(className + ": Json property xpath has invalid value");
			System.out.println(className + ": Json property xpath has invalid value");
			return false;
		}

		JSONObject attribute = element.optJSONObject("attributes");
		if (attribute == null) {
			//TODO add logs
			JSONCfgError.add(className + ": Json property attributes has invalid value"); 
			System.out.println("Json property attributes has invalid value");
			return false;
		}
		return true;
	}

	/*
	 * Get the xpath for the specific value you plan to retrieve
	 */
	public List<String> xpathFor(String key) {
		if (xpathMap == null) {
			return null;
		}
		return xpathMap.get(key);
	}

	/*
	 * Get the html attribute for the specific value you plan to retrieve
	 */
	public List<String> attributeFor(String key) {
		if (attributeMap == null) {
			return null;
		}
		return attributeMap.get(key);
	}

	/*
	 * Return a set of keys that exist in ElementJsonObject instance
	 */
	public Set<String> attributeKeys() {
		if (attributeMap == null) {
			return null;
		}
		return attributeMap.keySet();
	}

	/*
	 * In case of xpathInDepth is enabled then parent will be available
	 */
	public String getParentXpath() {
		return parentXpath;
	}
}