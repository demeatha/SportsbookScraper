package sbs.model;

public class EventHierarchyFactory {

	private  Map<String,List<String>> evClassList = null;
	private  Map<String,List<String>> evTypeList  = null;
	private  Map<String,List<String>>  eventList   = null;
	private  List<String> classTypeAssociationFactor = null;
	private  List<String> typeEventAssociationFactor = null;
	private  List<String> eventMarketSelectionAssociationFactor = null;
	private  static EventHierarchyFactory factory = null; 


	private EventHierarchyFactory() {}

	public EventHierarchyFactory create() {
		if (factory != null) {
			return factory;
		}
		return factory = new EventHierarchyFactory();
	}

	public void putClassList () {
	
	}

	public void putTypeList () {
	}

	public void putEventList () {
	
	}

	public void putMarketSelectionList () {
	}
}