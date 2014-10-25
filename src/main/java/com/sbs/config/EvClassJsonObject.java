package sbs.config;

import org.json.JSONObject;



public class EvClassJsonObject extends EvHierarchyLevel {

	private EvClassJsonObject() {
		super();
	}
	
	protected EvClassJsonObject(JSONObject evClass) {
		super(evClass);
	}

}