/**
 * A threaded REST client implementation.
 *
 * @author Christian Beier
 *
 * Copyright (C) 2012 CoboltForge
 *
 * This is proprietary software, all rights reserved!
 * You MUST contact hello@coboltforge.com if you want to use this software in your own product!
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

	/**
	 * Returns the number of sent bytes for the whole POST, i.e. all files.
	 * @author bk
	 *
	 */
	interface OnPostMultipartProgressListener {
		public void onProgress(long sentBytes);
	}

	interface OnPostMultipartCompleteListener {
		public void onComplete(String returned);
	}

}
