package sbs.config;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.json.*;
import java.io.*;

public class JSONCfg {
	/*
	 * Singleton instance
	 */
	private static JSONCfg cfg = null;

	/*
	 * The whole JSONObject configuration
	 */
	private JSONObject cfgJSONObject = null;

	/*
	 * Configuration file
	 */
	private static String fileName = null;


	/*
	 * Constructor class that will load the json file that is passed as argument
	 * into JSONObject format
	 */
	private JSONCfg() {
		File f = new File(fileName);
		if (!f.exists() && !f.isFile()) {
			JSONCfgError.add("File with name "+fileName+" doesn't exist");
			return;
		}
		cfgJSONObject = createJSONObjectFromFile(fileName);

		if (JSONCfgError.hasErrors()) {
			//TODO Add logs
			System.out.println("Error occured during json file read, reason: " + JSONCfgError.getLastError());
			JSONCfgError.add("Error occured during json file read");
		}

		// Initialise the WebDriver Factory
		if (cfgJSONObject.has("Drivers")) {
			JSONObject drivers = cfgJSONObject.getJSONObject("Drivers");
			WebDriverJsonObject.create(drivers);
		} else {
			//TODO add logs
			System.out.println("Warning no property 'Drivers' identified");
			JSONCfgError.add("Warning no property 'Drivers' identified");
			return;
		}

	}

	/* 
	 * This method handles the initialisation process.
	 *
	 * Read property that defines the main configuration file, and creates a new JSONCfg 
	 * instance.
	 */
	public static JSONCfg create(String fileName) {

		if (cfg != null) {
			return cfg;
		}

		JSONCfg.fileName = fileName;
		if (fileName == null) {
			System.out.println("Missing mandatory \"app.config\" property");
			JSONCfgError.add("Missing mandatory \"app.config\" property");
			return null;
		}
		cfg = new JSONCfg();

		return cfg;
	}

	/*
	 * Reads the main json file as well as the other files that are under includes json property.
	 * This method covers overridance and inheritance functionality between json configuration files.
	 */
	private JSONObject createJSONObjectFromFile (String filename) {
		BufferedReader br = null;
		String fileBuffer = "";
		
		// Put file's content into buffer
		try {
		      String sCurrentLine;
		      br = new BufferedReader(new FileReader(filename));
		      while ((sCurrentLine = br.readLine()) != null) {
			      fileBuffer += sCurrentLine;
		      }
		} catch (IOException e) {
		      System.out.println("Unable to read file reason: " + e.toString());
		      JSONCfgError.add("Unable to read file reason: " + e.toString());
		      return null;
		} finally {
		      try {
			      if (br != null) br.close();
		      } catch (Exception e) {
				System.out.println("Failed to close stream, reason: " + e.toString());
				JSONCfgError.add("Failed to close stream, reason: " + e.toString());
				return null;
		      }
		}

		// Search for 'includes' property and read recursively the children files 
		JSONObject parent = null;
		JSONObject child  = null;
		try {
			parent = new JSONObject(fileBuffer);
			if (parent.has("includes")) {
				JSONArray includedFiles = parent.getJSONArray("includes");
				parent.remove("includes");
				for (int i = 0; i < includedFiles.length(); i++) {
					child = createJSONObjectFromFile(includedFiles.getString(i));
				}
			}
		} catch (JSONException e) {
			System.out.println("Unable create JSONObject component failed, reason: " + e.toString());
			JSONCfgError.add("Unable to create JSONObject component failed, reason: " + e.toString());
		}
		return mergeJSONObjects(parent,child);
	}

	/*
	 * This is a helper method for createJSONObjectFromFile that completes property merging, and
	 * taking into account that if a property already exists on parent file then, this property will not be changed
	 * into the parent/main file.
	 */
	private JSONObject mergeJSONObjects(JSONObject parent, JSONObject child) {
		if (child == null) {
			return parent;
		}
		try {
			JSONArray propertyNames = child.names();
			for (int i = 0; i < propertyNames.length(); i++) {
				String childrenProperty = propertyNames.getString(i);
				if (!parent.has(childrenProperty)) {
					parent.put(propertyNames.getString(i),child.get(childrenProperty));
				}
			}
		} catch (JSONException e) {
			System.out.println("Unable create JSONObject component failed, reason: " + e.toString());
			JSONCfgError.add("Unable to create JSONObject component failed, reason: " + e.toString());
		}
		return parent;
	}

	/*
	 * Simple getter method that returns the JSONObject that has been created from all 
	 *
	 */
	public JSONObject getJSONObject() {
		return cfgJSONObject;
	}

	/*
	 * This is the entry point for all drivers included in configuration
	 */
	public WebDriverJsonObject driver (String driverName) {
		return WebDriverJsonObject.getDriverWithName(driverName);
	}
}