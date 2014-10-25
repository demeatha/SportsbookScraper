package sbs.model;



public class EvObject {
	private String  name;
	private String  url;

	public EvObject() {
	}
	
	public EvObject(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public void setName(String name) {
	      this.name = name;
	}
  
	public String getName() {
		return name;
	}
  
	public void setUrl(String url) { 
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}