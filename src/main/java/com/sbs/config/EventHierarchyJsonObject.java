package sbs.config;

import org.json.JSONObject;

/*
 * The main class that keeps all Event hierarchy specific levels
 */
public class EventHierarchyJsonObject {
	private EvClassJsonObject evClass = null;
	private EvTypeJsonObject  evType  = null;
	private EventJsonObject   event   = null;
	private final static String className = EventHierarchyJsonObject.class.getName();

	private EventHierarchyJsonObject () {}

	protected EventHierarchyJsonObject (JSONObject evHierObj) {
		if(!hierarchyIsValid(evHierObj)) {
			// TODO add logs
			System.out.println(className + " Invalid EventHierarchy structure");
			JSONCfgError.add(className + " Invalid EventHierarchy structure");
			return;
		}

		// Now create all event levels
		evClass = new EvClassJsonObject(evHierObj.getJSONObject("EvClass"));
		evType = new EvTypeJsonObject(evHierObj.getJSONObject("EvType"));
		event  = new EventJsonObject(evHierObj.getJSONObject("Event"));
	}


	private boolean hierarchyIsValid (JSONObject evHierObj) {
		// Check if mandatory levels are valid
		if (!evHierObj.has("EvClass")) {
			// TODO add logs
			System.out.println(className + " property with name EvClass not found");
			JSONCfgError.add(className + " property with name EvClass not found");
			return false;
		}

		if (!evHierObj.has("EvType")) {
			// TODO add logs
			System.out.println(className + " property with name EvType not found");
			JSONCfgError.add(className + " property with name EvType not found");
			return false;
		}
		
		if (!evHierObj.has("Event")) {
			// TODO add logs
			System.out.println(className + " property with name Event not found");
			JSONCfgError.add(className + " property with name Event not found");
			return false;
		}

		return true;
	}

	// Level object getters
	public EvClassJsonObject levelScopeClass() {
		return evClass;
	}

	public EvTypeJsonObject levelScopeType() {
		return evType;
	}

	public EventJsonObject levelScopeEvent() {
		return event;
	}
}