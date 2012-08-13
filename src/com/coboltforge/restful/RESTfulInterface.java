package com.coboltforge.restful;


import org.json.JSONObject;

public interface RESTfulInterface {

	interface OnGetStringCompleteListener {
		public void onComplete(String returned);
	};
	
	interface OnGetRawDataCompleteListener {
		public void onComplete(byte[] returned);
	};
	
	interface OnGetJSONCompleteListener {
		public void onComplete(JSONObject returned);
	}
	
	interface OnPostJSONCompleteListener {
		public void onComplete(String returned);
	}

}
