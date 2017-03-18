package com.android.tool;

import java.util.HashMap;
import java.util.Map;

public class VolleyRequestParams extends HashMap<String, String> {

	public VolleyRequestParams() {
	}

	public VolleyRequestParams(int capacity) {
		super(capacity);
	}

	public VolleyRequestParams(Map<? extends String, ? extends String> map) {
		super(map);
	}

	public VolleyRequestParams(int capacity, float loadFactor) {
		super(capacity, loadFactor);
	}
	
	private static final long serialVersionUID = 8112047472727256876L;

	public VolleyRequestParams with(String key, String value) {
		put(key, value);
		return this;
	}

}
