/*
 * A threaded REST client implementation.
 *
 * author Christian Beier
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
		/**
		 * Returns the file download progress.
		 * @param rcvdBytes Number of bytes received since the last onProgress call.
		 * @param totalRcvdBytes Number of bytes received since start of downloading this file.
		 * @param expectedBytes Length of file.
		 */
		void onProgress(long rcvdBytes, long totalRcvdBytes, long expectedBytes);
	}

	interface OnGetFileCompleteListener {
		void onComplete(String returned);
	}

	interface OnGetSizeCompleteListener {
		void onComplete(long accumulateSize);
	}

}
