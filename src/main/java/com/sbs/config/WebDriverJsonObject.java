package sbs.config; 

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.util.Properties;

import org.json.JSONObject;
import org.json.JSONException;

public class WebDriverJsonObject {
	private static JSONObject driversJsonObject = null;
	private String name = null;
	private String url = null;
	private EventHierarchyJsonObject hierarchy = null;
	private SportsbookLoginJsonObject login = null;
	private SportsbookBetslipJsonObject bslip = null;
	private static Map<String,WebDriverJsonObject> drivers = null;
	private static final String className = WebDriverJsonObject.class.getName();
	
	private WebDriverJsonObject () {}
	
	/*
	 * Constructor method that gets Drivers alias name.
	 * Since more than one driverd can exist.
	 */
	private WebDriverJsonObject (String name) {
		this.name = name;
	}

	/*
	 * Factory method for driver objects
	 *
	 * Driver configuration will be read, and the respectiveinstances will be created.
	 * Each one will have all the info that WebDriver needs, e.g EventHierarchy,url etc
	 * 
	 */
	protected static boolean create (JSONObject driversObject) {

		// keep JSONObject
		WebDriverJsonObject.driversJsonObject = driversObject;
		

		//If there aren't drivers in the object then continue
		if (driversObject.length() == 0) {
			// TODO add logs WARNING this time
			System.out.println("Driver instances not found");
			JSONCfgError.add("Driver instances not found");
			return false;
		}
    
		// Create new Map instance
		if (WebDriverJsonObject.drivers == null) {
			WebDriverJsonObject.drivers = new HashMap<String,WebDriverJsonObject>();
		}

		// Create WebdriverJsonObject instance for each driver
		String[] dnames = driversObject.getNames(driversObject);
		for (int i = 0; i < dnames.length; i++) {
			WebDriverJsonObject driver = new WebDriverJsonObject(dnames[i]);
			boolean driverOk = driver.loadDriverProperties(dnames[i], driversObject.getJSONObject(dnames[i]));

			if (!driverOk) {
				//TODO add logs
				System.out.println("Failed to load WebDriverJsonObject configuration, reason: " + JSONCfgError.getLastError());
				JSONCfgError.add("Failed to load WebDriverJsonObject configuration");
				return false;
			}
			drivers.put(dnames[i],driver);
		}

		return true;
	}

	/*
	 * This method will create instances for the parts that are described in its
	 * JSONObject instance and has a respective Class in code base.
	 *
	 * For example:
	 * EventHierchy instance for a web driver is one of the components that include all Driver's Event hierarchy.
	 */
	private boolean loadDriverProperties (String driverName, JSONObject jsonDriver) {

		JSONObject hierarchy = null;
		try {
			if (!validPropertiesForDriver(jsonDriver)) {
				//TODO add logs
				System.out.println("Driver " + name + " has invalid properties, reason: " + JSONCfgError.getLastError());
				JSONCfgError.add("Driver " + name + " has invalid properties");
				return false;
			}
			url = jsonDriver.getString("url");

			// Create the event hierarchy object
			hierarchy = jsonDriver.getJSONObject("EventHierarchy");
			this.hierarchy = new EventHierarchyJsonObject(hierarchy); 

			if (JSONCfgError.hasErrors()) {
				//TODO add logs
				System.out.println("Failed to load hierarchy configuration, reason: " + JSONCfgError.getLastError());
				JSONCfgError.add("Failed to load hiercrchy configuration");
				return false;
			}

			// Login object isn't mandatory configuration element
			JSONObject loginObject = jsonDriver.optJSONObject("Login");
			if(loginObject != null) {
				this.login = new SportsbookLoginJsonObject(loginObject);
				if(JSONCfgError.hasErrors()) {
					System.out.println("ERROR: Something wrong happened during initialization of login module, reason: " + JSONCfgError.getLastError());
					JSONCfgError.add("ERROR: Something wrong happened during initialization of login module");
					return false;
				}
			}

			// Its not mandatory to have the Betslip object into your configuration
			JSONObject betslipObject = jsonDriver.optJSONObject("Betslip");
			if(betslipObject != null) {
				this.bslip = new SportsbookBetslipJsonObject(betslipObject);
				if(JSONCfgError.hasErrors()) {
					System.out.println("ERROR: Something wrong happened during initialization of betslip module, reason: " + JSONCfgError.getLastError());
					JSONCfgError.add("ERROR: Something wrong happened during initialization of betslip module");
					return false;
				}
			}
		} catch (JSONException e) {
			//TODO add logs
			System.out.println("Failed to load JSONObject, reason: " + e.toString());
			JSONCfgError.add("Failed to load JSONObject, reason: " + e.toString());
			return false;
		}
		return true;
	}

	/*
	 * Validation method for Drivers mandatory properties
	 */
	private boolean validPropertiesForDriver(JSONObject driver) {
		if (!driver.has("url")) {
			//TODO add logs
			System.out.println("WebDriverJsonObject property url not found");
			JSONCfgError.add("WebDriverJsonObject property url not found");
			return false;
		}

		if (!driver.has("EventHierarchy")) {
			//TODO add logs
			System.out.println("Mandatory object EventHierarchy doesn't exist");
			JSONCfgError.add("Mandatory object EventHierarchy doesnt't exist");
			return false;
		}

		return true;
	}

	/*
	 * This is the entry point for each driver config object
	 */
	public static WebDriverJsonObject getDriverWithName(String name) {
		return drivers.get(name);
	}

	public EventHierarchyJsonObject hierarchy() {
		return hierarchy;
	}

	public String url() {
		return this.url;
	}

	public SportsbookLoginJsonObject login() {
		return login;
	}

	public SportsbookBetslipJsonObject betslip() {
		return bslip;
	}
}
