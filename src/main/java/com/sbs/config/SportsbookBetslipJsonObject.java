package sbs.config;

import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;


public class SportsbookBetslipJsonObject {
	private List<String> descriptionXpaths;
	private String amountField;
	private String button;

	private SportsbookBetslipJsonObject() {}

	protected SportsbookBetslipJsonObject(JSONObject betslipObject) {
		if(!betslipConfigIsValid(betslipObject)) {
			System.out.println("ERROR: Betslip object is not valid, please edit your configuration");
			JSONCfgError.add("ERROR: Betslip object is not valid, please edit your configuration");
			return;
		}

		try {
			// Get the descriptionXpaths
			descriptionXpaths = new ArrayList<String>();
			JSONArray xpathArray = betslipObject.getJSONArray("selectionDescription");
			for(int i = 0; i < xpathArray.length(); i++) {
				descriptionXpaths.add(xpathArray.getString(i));
			}

			//Get the amountField
			amountField = betslipObject.getString("amountField");

			//Get the button
			button = betslipObject.getString("placeBetButton");
		} catch (JSONException e) {
			//TODO add logs
			System.out.println("Failed to load JSONObject, reason: " + e.toString());
			JSONCfgError.add("Failed to load JSONObject, reason: " + e.toString());
			return;
		}
	}

	/*
	 * Validation method that check json object format and Mandatory items for betslip object
	 */
	private boolean betslipConfigIsValid(JSONObject betslipObject) {
		if(
			betslipObject.has("selectionDescription") && betslipObject.optJSONArray("selectionDescription") != null
		) {
			try {
				// Check for invalid values into array
				JSONArray xpathArray = betslipObject.getJSONArray("selectionDescription");
				for(int i = 0; i < xpathArray.length(); i++) {
					if(xpathArray.optString(i, null) == null) {
						System.out.println("ERROR: selectionDescription array item " + i + " is not String");
						JSONCfgError.add("ERROR: selectionDescription array item " + i + " is not String");
						return false;
					}
				}
			} catch (JSONException e) {
				//TODO add logs
				System.out.println("Failed to load JSONObject, reason: " + e.toString());
				JSONCfgError.add("Failed to load JSONObject, reason: " + e.toString());
				return false;
			}
		} else {
			System.out.println("ERROR: Mandatory selectionDescription property not found or has invalid format");
			JSONCfgError.add("ERROR: Mandatory selectionDescription property not found or has invalid format");
			return false;
		}

		if(!betslipObject.has("amountField") || betslipObject.optString("amountField", null) == null) {
			System.out.println("ERROR: Mandatory element amountField is not set or has invalid value");
			JSONCfgError.add("ERROR: Mandatory element amountField is not set or has invalid value");
			return false;
		}

		if(!betslipObject.has("placeBetButton") || betslipObject.optString("placeBetButton", null) == null) {
			System.out.println("ERROR: Mandatory element placeBetButton is not set or has invalid value");
			JSONCfgError.add("ERROR: Mandatory element placeBetButton field is not set or has invalid value");
			return false;
		}

		return true;
	}

	/*
	 * Getter methods
	 */


	public List<String> selectionDescription() {
		return descriptionXpaths;
	}

	public String amount() {
		return amountField;
	}

	public String placeBetButton() {
		return button;
	}
}