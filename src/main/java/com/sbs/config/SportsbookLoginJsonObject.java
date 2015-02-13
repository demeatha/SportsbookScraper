package sbs.config;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class SportsbookLoginJsonObject {
	private JSONObject loginObject = null;
	Map<String,String> xpaths = null;
	Map<String,String> credentials = null;
	/*
	 * Shouldn't get initilised by external object/class
	 */
	private SportsbookLoginJsonObject() {}

	protected SportsbookLoginJsonObject(JSONObject loginObject) {
		if(!loginObjectIsValid(loginObject)) {
			System.out.println("ERROR: Login object has invalid or missing properties, please correct the configuration item");
			JSONCfgError.add("ERROR: Betslip configuration is not set properly.");
			return;
		}
		this.loginObject  = loginObject;
		xpaths = new HashMap<String,String>();
		credentials = new HashMap<String,String>();

		// Get and save the properties
		try {
			// Get the xpaths
			JSONArray xpathNames = loginObject.getJSONObject("xpaths").names();
			for(int i = 0; i < xpathNames.length(); i++) {
				xpaths.put(xpathNames.getString(i),loginObject.getJSONObject("xpaths").getString(xpathNames.getString(i)));
			}

			// Get the credentials
			JSONArray credentialNames = loginObject.getJSONObject("credentials").names();
			for(int i = 0; i < credentialNames.length(); i++) {
				credentials.put(credentialNames.getString(i),loginObject.getJSONObject("credentials").getString(credentialNames.getString(i)));
			}
		} catch (JSONException e) {
			//TODO add logs
			System.out.println("Failed to load JSONObject, reason: " + e.toString());
			JSONCfgError.add("Failed to load JSONObject, reason: " + e.toString());
			return;
		}
		
	}

	/*
	 * Simple validation method so that we can make sure that when the Login object is set into configuration
	 * the mandatory attributes are correctly set up.
	 */
	private boolean loginObjectIsValid(JSONObject loginObject) {
		// Check first the nested objects
		if(!loginObject.has("xpaths")) {
			return false;
		}

		if(!loginObject.has("credentials")) {
			return false;
		}

		// Check the atributes
		if(
			   (!loginObject.getJSONObject("xpaths").has("login") || loginObject.getJSONObject("xpaths").optString("login",null) == null)
			|| (!loginObject.getJSONObject("xpaths").has("username") || loginObject.getJSONObject("xpaths").optString("username",null) == null)
			|| (!loginObject.getJSONObject("xpaths").has("password") || loginObject.getJSONObject("xpaths").optString("password",null) == null)
			|| (!loginObject.getJSONObject("xpaths").has("button") || loginObject.getJSONObject("xpaths").optString("button",null) == null)
			|| (!loginObject.getJSONObject("xpaths").has("proofOfLogin") || loginObject.getJSONObject("xpaths").optString("proofOfLogin",null) == null)
			|| (!loginObject.getJSONObject("credentials").has("username") || loginObject.getJSONObject("credentials").optString("username",null) == null)
			|| (!loginObject.getJSONObject("credentials").has("password") || loginObject.getJSONObject("credentials").optString("password",null) == null)
		) {
			return false;
		}

		return true;
	}

	/* GETTERS */

	public Map<String,String> loginCredentials() {
		if (credentials == null) {
			System.out.println("WARNING: You requested credentials that haven't been set up");
		}
		return credentials;
	}
	
	public Map<String,String> loginXpaths() {
		if (xpaths == null) {
			System.out.println("WARNING: You requested credentials that haven't been set up");
		}
		return xpaths;
	}

	public String loginUsername(String type) {
		if(type != "xpath" && type != "credentials") {
			System.out.println("WARNING: Incorect type used, wrong values may be returned");
		}
		return (type == "xpath") ? xpaths.get("username") : credentials.get("username");
	}

	public String loginPassword(String type) {
		if(type != "xpath" && type != "credentials") {
			System.out.println("WARNING: Incorect type used, wrong values may be returned");
		}
		return (type == "xpath") ? xpaths.get("password") : credentials.get("password");
	}

	public String loginButton() {
		return xpaths.get("button");
	}

	public String proofOfLogin() {
		return xpaths.get("proofOfLogin");
	}

	public String loginStartButton() {
		return xpaths.get("login");
	}
}