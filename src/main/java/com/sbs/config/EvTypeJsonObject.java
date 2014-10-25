package sbs.config;

import org.json.JSONObject;

public class EvTypeJsonObject extends EvHierarchyLevel {

	private EvTypeJsonObject() {
		super();
	}
	
	protected EvTypeJsonObject(JSONObject evType) {
		super(evType);
	}

}