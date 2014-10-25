package sbs.model;

class Event extends EvObject {
	private String name;
	private String url;
	private String startTime;

	public Event () {
		super();
	}

	public Event (String name, String url, String startTime) {
		super(name,url);
		this.startTime = regsubStartTime(startTime);
	}

	public String getName () {
		return name;
	}

	public String getUrl () {
		return url;
	}

	public String getStartTime () {
		return startTime;
	}

	public void setStartTime (String startTime) {
		
	}

	private String regsubStartTime (String startTime) {
		return startTime.replaceAll("EEST", "").trim();
	}
}