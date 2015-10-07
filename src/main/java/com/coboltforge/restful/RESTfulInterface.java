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
		void onComplete(String returned);
	}

	interface OnGetRawDataCompleteListener {
		void onComplete(byte[] returned);
	}

	interface OnGetJSONCompleteListener {
		void onComplete(JSONObject returned);
	}

	interface OnPostJSONCompleteListener {
		void onComplete(String returned);
	}

	/**
	 * Returns the number of sent bytes for the whole POST, i.e. all files.
	 * @author bk
	 *
	 */
	interface OnPostMultipartProgressListener {
		void onProgress(long sentBytes);
	}

	interface OnPostMultipartCompleteListener {
		void onComplete(String returned);
	}

	interface OnGetFileProgressListener {
		void onProgress(long rcvdBytes, long totalRcvdBytes);
	}

	interface OnGetFileCompleteListener {
		void onComplete(String returned);
	}

	interface OnGetSizeCompleteListener {
		void onComplete(long accumulateSize);
	}

}
