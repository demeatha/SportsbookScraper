package sbs.config;

import org.json.JSONObject;

public class EventJsonObject extends EvHierarchyLevel {

	private EventJsonObject() {
		super();
	}
	
	protected EventJsonObject(JSONObject event) {
		super(event);
	}

}