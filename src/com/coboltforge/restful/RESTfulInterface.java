/**
 * A threaded REST client implementation.
 * 
 * @author Christian Beier 
 * 
 * Copyright (C) 2012 CoboltForge
 * 
 * This is proprietary software, all rights reserved!
 * 
 */

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
	
	
	interface OnPostMultipartProgressListener {
		public void onProgress(long sentBytes);
	}
	
	interface OnPostMultipartCompleteListener {
		public void onComplete();
	}

}
