package sbs.config;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

 /*
  * This is the class that will store Elements object for each event hierarchy level.
  * 
  * Supported Hierarchy levels are evClass evType and event elements
  */
public class EvHierarchyLevel {
	private JSONObject hierLevelObj           = null;
	private List<ElementJsonObject> elements  = null;
	private static final String className     = EvHierarchyLevel.class.getName();
	private boolean xpathInDepth              = false;

	protected EvHierarchyLevel () {}
	
	protected EvHierarchyLevel (JSONObject hierLevelObj) {

		this.hierLevelObj = hierLevelObj;

		// Validate elements
		elements = new ArrayList<ElementJsonObject>();
		if (!evHierarchyJsonObjectIsValid()) {
			//TODO add logs
			System.out.println(className + " failed to pass validation, reason: " + JSONCfgError.getLastError());
			JSONCfgError.add(className + " linkedElements array must only include nested arrays");
			return;
		}


		try {

			  JSONArray elementsArray  = hierLevelObj.getJSONArray("Elements");
			  JSONArray linkedElements = hierLevelObj.optJSONArray("linkedElements");
			  
			  // Set the xpathInDepth flag to true if is set
			  xpathInDepth = hierLevelObj.optBoolean("xpathInDepth",false);

			  // Check if there are linked elements that need to be handled as one
			  if (linkedElements != null) {
				  if (xpathInDepth) {
					  //TODO add logs
					  System.out.println(className + " elements fail to link, reason: xpathInDepth attribute can't co-exist with linkedElements");
					  JSONCfgError.add(className + " elements fail to link, reason: xpathInDepth attribute can't co-exist with linkedElements" );
					  return;
				  }
				  boolean elementsListOk = storeLinkedElements(linkedElements, elementsArray);
				  if (!elementsListOk) {
					  System.out.println(className + " elements fail to link, reason: "+ JSONCfgError.getLastError());
					  JSONCfgError.add(className + " elements fail to link, reason: "+ JSONCfgError.getLastError());
					  return;
				  }
			  }

			  // Parse and store the rest of the elements
			  for (int i = 0; i < elementsArray.length(); i++) {
				  ElementJsonObject element = new ElementJsonObject(elementsArray.getJSONObject(i), xpathInDepth);
				  if (JSONCfgError.hasErrors()) {
					  System.out.println(className + " object initialisation failure for Element, reason: " + JSONCfgError.getLastError());
					  JSONCfgError.add(className + " object initialisation failure for Element");
					  return;
				  }
				  elements.add(element);
			  }
		  } catch (JSONException e) {
				  System.out.println(className + "JSONObject failed to be initialised, reason: " + e.toString());
				  JSONCfgError.add(className + "JSONObject failed to be initialised, reason:  , reason: " + e.toString());
		  }

	}

	/*
	 * A simple getter that returns the ElementJsonObject based on the index you passed as argument
	 *
	 * @param elementIndex the index that item is located in elements list
	 */
	public ElementJsonObject getElement(int elementIndex) {
		if(elementIndex >= elements.size() || elementIndex < 0) {
			System.out.println(className + "Index " + elementIndex+ " out of bounds, list size is: "+ elements.size());
			JSONCfgError.add(className + "Index " + elementIndex+ " out of bounds, list size is: "+ elements.size());
			return null;
		}
		return elements.get(elementIndex);
	}

	/*
	 * Returns elements list
	 */
	public List<ElementJsonObject> elementsList() {
		return elements;
	}

	/*
	 * Return the xpathInDepth flag
	 */
	public boolean isXpathInDepthEnabled() {
		return xpathInDepth;
	}

	/*
	 * In case we want to handle two different elements as one there is the linkedElements option
	 * A json Array that keeps arrays with all the element pair indexes you want to link.
	 *
	 * @param linkedElements the linked element indexes that need to be parsed.
	 * @param elementsArray  the array that includes all the element json objects
	 */
	private boolean storeLinkedElements (JSONArray linkedElements, JSONArray elementsArray) {
		for (int i = 0; i < linkedElements.length(); i++) {
			JSONArray linkedGroupElements = linkedElements.getJSONArray(i);
			List<JSONObject> linkedObjLst = new ArrayList<JSONObject>();

			// Loop through Elements array to get the linked objects.
			// After each array item removal, array indexes will be re-arranged,
			// so counter j is also used as removal factor which sifts the search +1 after each element removal.
			for (int j = 0;  j < linkedGroupElements.length(); j++) {
			  linkedObjLst.add(elementsArray.getJSONObject(linkedGroupElements.getInt(j)-1-j));
			  elementsArray.remove(linkedGroupElements.getInt(j)-1-j);
			}

			ElementJsonObject element = new ElementJsonObject(linkedObjLst);
			if (JSONCfgError.hasErrors()) {
				System.out.println(className + " object initialisation failure for Element, reason: " + JSONCfgError.getLastError());
				JSONCfgError.add(className + " object initialisation failure for Element");
				return false;
			}
			elements.add(element);
		}
		return true;
	}

	/*
	 * Validation method for EvHierarchyJsonObject instance
	 *
	 * Check that all mandatory json elements have valid property names and values
	 *
	 */
	private boolean evHierarchyJsonObjectIsValid () {

		if (!hierLevelObj.has("Elements")) {
			// TODO add logs
			System.out.println(className + " property with name Elements not found");
			JSONCfgError.add(className + " property with name Elements not found");
			return false;
		}

		// Validate the format of linkedElements object since there is no respective class for it
		JSONArray linkedElements = hierLevelObj.optJSONArray("linkedElements");
		if (linkedElements != null) {
			for (int i = 0; i < linkedElements.length(); i++) {
				JSONArray linkedElement = linkedElements.optJSONArray(i);
				if (linkedElement == null) {
					// TODO add logs
					System.out.println(className + " linkedElements array must only include nested arrays");
					JSONCfgError.add(className + " linkedElements array must only include nested arrays");
					return false;
				}
				for (int j = 0; j <  linkedElement.length(); j++) {
					int res = linkedElement.optInt(j,-1);
					if (res == -1) {
						//TODO add logs
						System.out.println(className + " Invalid linkedElements nested array digit (" + i +"," + j + ")");
						JSONCfgError.add(className + " Invalid linkedElements nested array digit (" + i +"," + j + ")");
						return false;
					}
				}
			}
		}
		return true;
	}
}
