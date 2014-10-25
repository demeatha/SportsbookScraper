package sbs.model;

import sbs.model.EvObject;
import sbs.model.EvClass;

public class EvType extends EvObject {
	private EvClass parent;

	public EvType (EvClass parent, String name, String url) {
		super(name,url);
		this.parent = parent;
	}
	public void setParent(EvClass parent) {
		this.parent = parent;
	}

	public EvClass getParent() {
		return parent;
	}
}