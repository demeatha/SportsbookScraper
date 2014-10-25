package sbs.config;

import java.util.List;
import java.util.ArrayList;
/*
 * A simple error accumulator
 *
 */
class JSONCfgError {
	private static List<String> errors = null;
	private static String lastAdded    = null;
	
	protected static void add (String description) {
		//TODO add logs
		if (errors == null) {
			errors = new ArrayList<String>();
		}
		System.out.println("Adding error with description: " + description);
		errors.add(description);
		lastAdded = description;
	}

	public static boolean hasErrors () {
		if (errors == null || errors.size() == 0) {
			return false;
		}
		return true;
	}

	public static List<String> getErrors () {
		return errors;
	}

	protected static void flush() {
		lastAdded    = null;
		if (errors != null && errors.size() > 0) {
			errors.clear();
		}
	}

	protected static String getLastError() {
		return lastAdded;
	}


	public static void printJSONCfgStackTrace () {
		System.out.println("Errors found: " + errors.size());
		System.out.println("Errors:");
		for (int i = 0; i < errors.size(); i++) {
			//TODO add logs
			System.out.println("=========> "+ errors.get(i));
		}
	}
}