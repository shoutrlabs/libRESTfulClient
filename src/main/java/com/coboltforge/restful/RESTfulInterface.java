/*
 * A threaded REST client implementation.
 *
 * Author Christian Beier
 *
 * Copyright (c) 2020, shoutr labs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
