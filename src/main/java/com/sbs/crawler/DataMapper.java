package sbs.crawler;

import java.util.Map;
import java.util.List;
import sbs.config.JSONCfg;

/*
 * The DataMapper is the interface which is used so as, Sportsbook specific functionality will be implented in its class.
 */
public interface DataMapper {

	/*
	 * When the core mehtod retrieves the data, this method is responsible to get them and map them as concrete class defines.
	 */
	public Object mapResultStructure(String hierarchyLevel, Map<String,List<String>> rawData);

	/*
	 * Before Sportsbook specific mapping method run we need to make sure tha strictly coupled attribute aliasses
	 * with class's implementation are defined into configuration.
	 * With that way we'll avoid program crash
	 */
	public boolean areJSONCfgAttributeAliasesValid(JSONCfg cfg);
}