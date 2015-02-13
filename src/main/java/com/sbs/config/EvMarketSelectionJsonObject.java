package sbs.config;

import org.json.JSONObject;

public class EvMarketSelectionJsonObject extends EvHierarchyLevel {

	private EvMarketSelectionJsonObject() {
		super();
	}
	
	protected EvMarketSelectionJsonObject(JSONObject evMarketSelection) {
		super(evMarketSelection);
	}

}