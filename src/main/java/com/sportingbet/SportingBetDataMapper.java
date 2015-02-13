package sportingbet;

import sbs.crawler.DataMapper;
import java.util.List;
import sbs.config.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;


/*
 * This is a SportingBet data manipulation class that maps the data that came from crawler's main procedure.
 * Raw data passed as a Map of lists from the main algorithm, and each one has the data retrieved from the specific html element attributes.
 * Each element attribute is defined as an xpath into the JSONCFG configuration and is aligned with a specific level of parsing.
 * As input, acceptable levels of parsing are: EvClass,EvType,Event and EvMarketSelections.
 *
 * Creating an object of this class, and passing this object into SportsbookCrawler component, gives you the option to
 * map the raw data you get with a way you believe that suites to this site.
 *
 * The raw data you'll get, will always be in Map<String,List<String>> format. One list for each attribute.
 * Based on the fact, that html elements parsed sequentially, is positive to consider that attributes stored into different lists
 * can be easilly aligned and mapped. Although, associations are not always one by one between the values you get, and for that reason,
 * based on that elements that each site encapsulates data in them, you need to handle each one programmatically, so that you can associate their values.
 */
public class SportingBetDataMapper implements DataMapper {
	private boolean isInvalid = false;

	public SportingBetDataMapper(JSONCfg cfg) {
		if(!areJSONCfgAttributeAliasesValid(cfg)) {
			System.out.println("ERROR: configured element attributes for SportingBetDriver are invalid, please change the configuration object");
			isInvalid = true;
			return;
		}
	}
	/*
	 * 
	 *
	 * We put placeholders for Event, EvType, and Event because the association between elements is on by one
	 * placeholders are only to help us to parse the list simultaneously.
	 */
	@Override
	public Object mapResultStructure(String hierarchyLevel, Map<String,List<String>> rawData) {
		if(isInvalid) {
			System.out.println("ERROR: Sportingbet data mapper blocked attributes are invalid");
			return null;
		}

		if (hierarchyLevel.equals("EvClass")) {
			return putPlaceHolders(rawData);
		}

		if (hierarchyLevel.equals("EvType")) {
			return putPlaceHolders(rawData);
		}

		if (hierarchyLevel.equals("Event")) {
			return putPlaceHolders(rawData);
		}

		if (hierarchyLevel.equals("EvMarketSelection")) {
			return mapEvMArketSelections(rawData);
		}
		return null;
	}

	/*
	 * We need to make sure that data mapping will work correctly.
	 * Each of these alias names must be included into SportingBetDriver-EventHierarchy configuration object.
	 * If one of them is not included then validation will fail.
	 */
	@Override
	public boolean areJSONCfgAttributeAliasesValid(JSONCfg cfg) {
		if(cfg.driver("SportingBetDriver") == null) {
			System.out.println("ERROR: SportingBetDriver should have been defined.");
			return false;
		}

		List<ElementJsonObject> elementsList = cfg.driver("SportingBetDriver")
			.hierarchy()
			.levelScopeMarketSelection()
			.elementsList();

		boolean isValid = true;
		for(ElementJsonObject configElement : elementsList) {
			for(String nameAlias : configElement.attributeKeys()) {
				if(
					   !nameAlias.equals("marketName")
					&& !nameAlias.equals("selectionNameRow")
					&& !nameAlias.equals("selectionNameCol")
					&& !nameAlias.equals("selectionGroup")
					&& !nameAlias.equals("selectionPrice")
					&& !nameAlias.equals("selectionButton")
					&& !nameAlias.equals("selectionHandicap")
					&& !nameAlias.equals("name")
					&& !nameAlias.equals("url")
				) {
					isValid = false;
				}
			}
		}

		return isValid;
	}

	/*
	 * Padding method put empty strings as placeholders into the raw data map lists
	 *
	 * This proccess will help in future lists manipulation en mass
	 */
	private Map<String,List<String>> putPlaceHolders(Map<String,List<String>> rawData) {
		// Determine which is the bigest list
		int sizeMax = 0;
		for(String key : rawData.keySet()) {
			if(sizeMax < rawData.get(key).size()) {
				sizeMax = rawData.get(key).size();
			}
		}

		// Complete empty string padding
		List<String> paddingList = new ArrayList<String>();
		for(int i = 0; i < sizeMax; i++) {
			  paddingList.add(" ");
		}

		for(String key : rawData.keySet()) {
			List<String> lst = rawData.get(key);
			if(lst.size() != sizeMax) {
				lst.addAll(paddingList);
			}
		}
		return rawData;
	}

	private Map<String,Object> mapEvMArketSelections(Map<String,List<String>> rawData) {
		Map<String,Object> marketSelection = new HashMap<String,Object>();
		
		// Store market name
		marketSelection.put("marketName",rawData.get("marketName").get(0));

		// Selection groups case
		if(rawData.get("selectionGroup").size() != 0) {
			marketSelection.put("selections",mapGroupedSelections(rawData));
		} else {
			marketSelection.put("selections",mapRegularSelections(rawData));
		}

		return marketSelection;
	}

	/*
	 * This method is Sportingbet specific and is used to align selection with prices in case of those are grouped.
	 *
	 * Grouped market selections is a special case into Sportingbet where the table the customer views is not separated equally
	 * into rows and columns. innerHTML of selectionGroup into config will give us the option to identify for each html column
	 * the number of the selection elements, then will get the raw data to map them and return Lists into a main List.
	 */
	private List<List<String>> mapGroupedSelections(Map<String,List<String>> rawData) {
		// Size of selection groups will be identical with the selectionNameCol
		List<List<String>> selectionsList = new ArrayList<List<String>>();
		int indexFactor= 0;
		for(int i = 0; i < rawData.get("selectionGroup").size(); i++) {
			String group = rawData.get("selectionGroup").get(i);
			Document doc = Jsoup.parse(group);
			Elements selections = doc.select(".m_event");
			int size = selections.size();

			for(int j=0; j < size; j++) {
				int index = j+indexFactor;
				List<String> selectionList = new ArrayList<String>();

				// Construct the selection name and add it inot list
				String selectionName = rawData.get("selectionNameCol").get(i) +  " " + rawData.get("selectionNameRow").get(index);
				selectionList.add(selectionName);

				// Construct selection price with or without handicap
				String selectionPrice = rawData.get("selectionPrice").get(index);
				// If handicap isn't empty list then will exist one handicap for each price
				if(rawData.get("selectionHandicap").size() != 0) {
					selectionPrice += " handicap: " +  rawData.get("selectionHandicap").get(index);
				}
				selectionList.add(selectionPrice);

				// Getting the selections 'button' unique id
				String selectionButton = rawData.get("selectionButton").get(index);
				selectionList.add(selectionButton);

				// Add to main list
				selectionsList.add(selectionList);
			}
			indexFactor += size;
		}
		return selectionsList;
	}

	/*
	 * Regular means that this market has no groupped selections.
	 *
	 * Regular mapping is separated into two different cases.
	 * The first one is for selections that their description is constructed by the row+column and their total number is row*column
	 * The second one is for selections that their description is aligned with the price and the number of selections are the same with the number of rows.
	 * The elements which are mapped from that method are selectionNameRow-Col selectionPrice, selectionHandicap and selectionButton
	 */
	private List<List<String>> mapRegularSelections(Map<String,List<String>> rawData) {

		// For regular parsing there are three cases, the first one is that
		// there is no header(columns) and rows
		List<List<String>> selectionsList = new ArrayList<List<String>>();
		for(int i = 0; i < rawData.get("selectionNameRow").size(); i++) {
			// There is a case where column does exist, but selection prices
			// are identical to rows. We must avoid this case e handled as a row-column combination of selection names
			if(rawData.get("selectionNameCol").size() * rawData.get("selectionNameRow").size() == rawData.get("selectionPrice").size()) {
				for(int j=0; j < rawData.get("selectionNameCol").size(); j++) {
					selectionsList.add(mapRegularSelection(rawData,i,j));
				}
			} else {
				selectionsList.add(mapRegularSelection(rawData,i,-1));
			}
		}
		return selectionsList;
	}

	/*
	 * This helper method will map the rawData based on given row and column.
	 * 
	 * In case of indexCol is set as -1 then the method will map only the row with the price
	 */
	private List<String> mapRegularSelection(Map<String,List<String>> rawData, int indexRow, int indexCol) {
		List<String> selectionList = new ArrayList<String>();

		int columnSize = rawData.get("selectionNameCol").size();

		// The default index for getting data from the rawData map
		int indexDefault = 0;
		if(indexCol > -1) {
			indexDefault = indexRow * columnSize;
		} else {
			indexDefault = indexRow;
		}

		// Construct the selection name and add it into list
		String selectionName = rawData.get("selectionNameRow").get(indexRow);
		if(indexCol > -1) {
			indexDefault += indexCol;
			selectionName += " " + rawData.get("selectionNameCol").get(indexCol);
		}
		selectionList.add(selectionName);

		// Construct selection price with or without handicap
		String selectionPrice = rawData.get("selectionPrice").get(indexDefault);
		// If handicap isn't empty list then will exist one handicap for each price, add the hadnicaps
		if(rawData.get("selectionHandicap").size() != 0) {
			selectionPrice += " handicap: " +  rawData.get("selectionHandicap").get(indexDefault);
		}
		selectionList.add(selectionPrice);

		// Getting the selections 'button' unique id
		String selectionButton = rawData.get("selectionButton").get(indexDefault);
		selectionList.add(selectionButton);

		return selectionList;
	}
}