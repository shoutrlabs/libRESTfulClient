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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.util.Log;


public class RESTfulClient  {

	private final String TAG="RESTfulClient";
	private DefaultHttpClient mHttpClient; //only accessed by commThread
	private CommThread mCommThread;

	private boolean mDoLog;

	public static final int SC_OK = 42;
	public static final int SC_ERR = 666;

	private int status = SC_OK;

	public RESTfulClient () {
		this(null, 0, null, true);
	}

	public RESTfulClient (String user, String pass) {
		this(null, 0, null, true);
		mHttpClient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials(user, pass));
	}

	public RESTfulClient (Context ctx, int bksResource, String pass, boolean doLog) {

		mDoLog = doLog;

		if(ctx == null || bksResource == 0 || pass == null) {
			HttpParams httpParams = new BasicHttpParams();
			mHttpClient = new DefaultHttpClient(httpParams);
		}
		else {
			final SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", createAdditionalCertsSSLSocketFactory(ctx, bksResource, pass), 443));

			// create connection manager using scheme, we use ThreadSafeClientConnManager
			final HttpParams params = new BasicHttpParams();
			final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params,schemeRegistry);
			mHttpClient = new DefaultHttpClient(cm, params);
		}

		mCommThread = new CommThread();
		mCommThread.start();
	}





	public synchronized int getStatus()
	{
		return status;
	}


	public synchronized final List<Cookie> getCookies()
	{
		try {
			return mHttpClient.getCookieStore().getCookies();
		}
		catch(NullPointerException e) {
			return null;
		}
	}



	/**
	 * get unformatted string from url in a thread, callback will be executed on the main thread.
	 * @param h
	 * @param url
	 * @param callback Callback to invoke on completion. May be null.
	 */
	public synchronized void getString(Handler h, String url, RESTfulInterface.OnGetStringCompleteListener callback) {

		url = sanitizeUrl(url);

		if(mDoLog) Log.d(TAG, "queueing GETSTRING " + url);

		CommThread.Task gs = mCommThread.new Task(CommThread.Task.MODE_GETSTRING);
		gs.in_url= url;
		gs.callbackHandler = h;
		gs.getStringCallback = callback;
		mCommThread.addTask(gs);
	}


	/**
	 * get raw binary data from url in a thread, callback will be executed on the main thread.
	 * @param h
	 * @param url
	 * @param callback Callback to invoke on completion. May be null.
	 */
	public synchronized void getRawData(Handler h, String url, RESTfulInterface.OnGetRawDataCompleteListener callback) {

		url = sanitizeUrl(url);

		if(mDoLog) Log.d(TAG, "queueing GETRAWDATA " + url);

		CommThread.Task grd = mCommThread.new Task(CommThread.Task.MODE_GETRAWDATA);
		grd.in_url= url;
		grd.callbackHandler = h;
		grd.getRawDataCallback = callback;
		mCommThread.addTask(grd);
	}



	/**
	 * save data from url to file in a thread, callback will be executed on the main thread.
	 * @param h
	 * @param url
	*/
	public synchronized void getFile(Handler h, String url, String filename,
									 RESTfulInterface.OnGetFileProgressListener progressCallback,
									 RESTfulInterface.OnGetFileCompleteListener completeCallback) {

		url = sanitizeUrl(url);

		if(mDoLog) Log.d(TAG, "queueing GETFILE " + url);

		CommThread.Task gf = mCommThread.new Task(CommThread.Task.MODE_GETFILE);
		gf.in_url= url;
		gf.out_filename = filename;
		gf.callbackHandler = h;
		gf.getFileProgressCallback = progressCallback;
		gf.getFileCompleteCallback = completeCallback;
		mCommThread.addTask(gf);
	}



	/**
	 * get JSON from url in a thread, callback will be executed on the main thread.
	 * @param h
	 * @param url
	 * @param callback Callback to invoke on completion. May be null.
	 */
	public synchronized void getJSON(Handler h, String url, RESTfulInterface.OnGetJSONCompleteListener callback) {

		url = sanitizeUrl(url);

		if(mDoLog) Log.d(TAG, "queueing GETJSON " + url);

		CommThread.Task gj = mCommThread.new Task(CommThread.Task.MODE_GETJSON);
		gj.in_url= url;
		gj.callbackHandler = h;
		gj.getJSONCallback = callback;
		mCommThread.addTask(gj);
	}


	/**
	 * post JSON to url in a thread, callback will be executed on the main thread.
	 * @param h
	 * @param url
	 * @param data
	 * @param callback Callback to invoke on completion. May be null.
	 */
	public synchronized void postJSON(Handler h, String url, JSONObject data, RESTfulInterface.OnPostJSONCompleteListener callback) {

		url = sanitizeUrl(url);

		if(mDoLog) Log.d(TAG, "queueing POSTJSON " + url + " " + data.toString());

		CommThread.Task pj = mCommThread.new Task(CommThread.Task.MODE_POSTJSON);
		pj.in_url= url;
		pj.in_json = data;
		pj.callbackHandler = h;
		pj.postJSONCallback = callback;
		mCommThread.addTask(pj);
	}



	/**
	 * Post the given input streams as multipart form data to the given url.
	 * @param h
	 * @param url
	 * @param inStreams
	 * @param mimeTypes MIME type of the given data.
	 * @param fileNames
	 * @param progressCallback Callback to invoke on progress. May be null.
	 * @param completeCallback Callback to invoke on completion. May be null.
	 */
	public synchronized void postMultipart(
			Handler h,
			String url,
			InputStream[] inStreams,
			String[] mimeTypes,
			String[] fileNames,
			RESTfulInterface.OnPostMultipartProgressListener progressCallback,
			RESTfulInterface.OnPostMultipartCompleteListener completeCallback) {

		url = sanitizeUrl(url);

		if(mDoLog) Log.d(TAG, "queueing POSTMULTIPART " + url + " " + inStreams.toString());

		CommThread.Task pm = mCommThread.new Task(CommThread.Task.MODE_POSTMULTIPART);
		pm.in_url= url;
		pm.in_arr_is = inStreams;
		pm.in_arr_mimetypes = mimeTypes;
		pm.in_arr_filenames = fileNames;
		pm.callbackHandler = h;
		pm.postMultipartProgressCallback = progressCallback;
		pm.postMultipartCompleteCallback = completeCallback;
		mCommThread.addTask(pm);
	}


	public synchronized void cancelAll() {

		if(mDoLog) Log.d(TAG, "Cancelling all operations");

		// empty the task queue
		mCommThread.mTaskQueue.clear();
		// disconnect callbacks of current task
		try {
			mCommThread.mCurrentTask.postJSONCallback = null;
			mCommThread.mCurrentTask.getJSONCallback = null;
			mCommThread.mCurrentTask.getStringCallback = null;
		}
		catch(NullPointerException e) {
		}
		// and interrupt currently running op
		mCommThread.interrupt();

	}

	/**
	 * This is more a last-minute safety measure, as httpClient would otherwise hick up.
	 * @param url
	 * @return
	 */
	private String sanitizeUrl(String url) {
		// eat up senseless blanks, would cause httpClient to hickup
		url = url.replaceAll(" ", "");
		// also, remove double shlashes except first pair
		return url.replaceAll("(?<!:)//", "/");
	}

	/**
	 * All characters except letters ('a'..'z', 'A'..'Z') and numbers ('0'..'9') and characters '.', '-', '*', '_' are converted into their hexadecimal value prepended by '%'. For example: '#' -> %23. In addition, spaces are substituted by '+'.
	 * @param url
	 * @return
	 */
	public static String urlEncode(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8").trim();
		} catch (Exception e) {
			return url;
		}
	}


	/**
	 * Get size of remote file via HEAD request. *Not* threaded!
	 * @param url
	 * @return Size of remote file or -1 on error.
	 */
	public static long getSize(String url) {

		AndroidHttpClient httpCl = AndroidHttpClient.newInstance(url);

		HttpResponse response = null;
		try {
			response = httpCl.execute(new HttpHead(url));
			return response.getEntity().getContentLength();
		} catch (IOException e) {
			return -1;
		} finally {
			httpCl.close();
		}

	}


	private class CommThread extends Thread {

		private static final String TAG = "RESTfulCommThread";
		private Task mCurrentTask;

		public class Task {
			// constants
			public final static int MODE_GETSTRING = 0;
			public final static int MODE_GETJSON = 1;
			public final static int MODE_POSTJSON = 2;
			public final static int MODE_GETRAWDATA = 3;
			public final static int MODE_POSTMULTIPART = 4;
			public final static int MODE_GETFILE = 5;
			public final static int QUIT = 666;


			// data, acted upon according to mode
			private final int mode;
			private String in_url;
			private String out_string;
			private byte[] out_ba;
			private String out_filename;
			private JSONObject out_json;
			private JSONObject in_json; // for POST JSON
			private InputStream[] in_arr_is; // for POSTMULTIPART
			private String[] in_arr_filenames; // for POSTMULTIPART
			private String[] in_arr_mimetypes; // for POSTMULTIPART
			private Handler callbackHandler; // handler to post callbacks to
			private RESTfulInterface.OnGetStringCompleteListener getStringCallback;
			private RESTfulInterface.OnGetRawDataCompleteListener getRawDataCallback;
			private RESTfulInterface.OnGetJSONCompleteListener getJSONCallback;
			private RESTfulInterface.OnPostJSONCompleteListener postJSONCallback;
			private RESTfulInterface.OnPostMultipartProgressListener postMultipartProgressCallback;
			private RESTfulInterface.OnPostMultipartCompleteListener postMultipartCompleteCallback;
			private RESTfulInterface.OnGetFileProgressListener getFileProgressCallback;
			private RESTfulInterface.OnGetFileCompleteListener getFileCompleteCallback;


			public Task(int mode) {
				this.mode = mode;
			}
		}

		private ConcurrentLinkedQueue<Task> mTaskQueue = new ConcurrentLinkedQueue<Task>(); //BlockingQueue instead?



		public CommThread () {
		}


		public void run() {

			if(mDoLog) Log.d(TAG, "Saying Hellooo!");

			boolean quit = false;
			while(!quit) {

                synchronized (mTaskQueue) {

                    mCurrentTask = mTaskQueue.peek();

                    // if queue empty, wait and re-run loop
                    if (mCurrentTask == null) {

                        try {
                            if (mDoLog) Log.d(TAG, "nothing to do, waiting...");
                            mTaskQueue.wait(); // this releases the lock at puts the thread on the waiting list
                        } catch (InterruptedException e) {
                            if (mDoLog) Log.d(TAG, "woke up!!");
                        }

                        // get queue head
                        continue;
                    }

                }


				// there is something
				try {
					switch (mCurrentTask.mode) {

					case Task.QUIT:
						if(mDoLog) Log.d(TAG, "got QUIT");
						quit = true;
						break;

					case Task.MODE_GETJSON:
						if(mDoLog) Log.d(TAG, "got GETJSON " + mCurrentTask.in_url);
						printCookies();
						mCurrentTask.out_json = getJSON(mCurrentTask.in_url);
						// currentTask could be something other at time of runnable execution
						final RESTfulInterface.OnGetJSONCompleteListener gjc = mCurrentTask.getJSONCallback;
						final JSONObject gjjo = mCurrentTask.out_json;
						synchronized (RESTfulClient.this) { // do not interfere with cancelAll()
							mCurrentTask.callbackHandler.post(new Runnable() {
								@Override
								public void run() {
									try{
										gjc.onComplete(gjjo);
									}
									catch(NullPointerException e) {
									}
								}
							});
						}
						break;

					case Task.MODE_GETSTRING:
						if(mDoLog) Log.d(TAG, "got GETSTRING " + mCurrentTask.in_url);
						printCookies();
						mCurrentTask.out_string = getString(mCurrentTask.in_url);
						// currentTask could be something other at time of runnable execution
						final RESTfulInterface.OnGetStringCompleteListener gsc = mCurrentTask.getStringCallback;
						final String gss = mCurrentTask.out_string;
						synchronized (RESTfulClient.this) { // do not interfere with cancelAll()
							mCurrentTask.callbackHandler.post(new Runnable() {
								@Override
								public void run() {
									try {
										gsc.onComplete(gss);
									}
									catch(NullPointerException e) {
									}
								}
							});
						}
						break;

					case Task.MODE_GETRAWDATA:
						if(mDoLog) Log.d(TAG, "got GETRAWDATA " + mCurrentTask.in_url);
						printCookies();
						mCurrentTask.out_ba = getRawData(mCurrentTask.in_url);
						// currentTask could be something other at time of runnable execution
						final RESTfulInterface.OnGetRawDataCompleteListener grdc = mCurrentTask.getRawDataCallback;
						final byte[] grdba = mCurrentTask.out_ba;
						synchronized (RESTfulClient.this) { // do not interfere with cancelAll()
							mCurrentTask.callbackHandler.post(new Runnable() {
								@Override
								public void run() {
									try{
										grdc.onComplete(grdba);
									}
									catch(NullPointerException e) {
									}
								}
							});
						}
						break;

					case Task.MODE_POSTJSON:
						if(mDoLog) Log.d(TAG, "got POSTJSON " + mCurrentTask.in_url + " " + mCurrentTask.in_json.toString());
						printCookies();
						mCurrentTask.out_string = postJSON(mCurrentTask.in_url, mCurrentTask.in_json);
						synchronized (RESTfulClient.this) { // do not interfere with cancelAll()
							// currentTask could be something other at time of runnable execution
							final RESTfulInterface.OnPostJSONCompleteListener pjc = mCurrentTask.postJSONCallback;
							final String pjs = mCurrentTask.out_string;
							mCurrentTask.callbackHandler.post(new Runnable() {
								@Override
								public void run() {
									try {
										pjc.onComplete(pjs);
									}
									catch(NullPointerException e) {
									}
								}
							});
						}
						break;

					case Task.MODE_POSTMULTIPART:
						if(mDoLog) Log.d(TAG, "got POSTMULTIPART " + mCurrentTask.in_url + " count " + mCurrentTask.in_arr_is.length);
						printCookies();
						// here the callback is called from within the worker method
						mCurrentTask.out_string = postMultipart(
								mCurrentTask.in_url,
								mCurrentTask.in_arr_is,
								mCurrentTask.in_arr_mimetypes,
								mCurrentTask.in_arr_filenames,
								mCurrentTask.postMultipartProgressCallback);
						synchronized (RESTfulClient.this) { // do not interfere with cancelAll()
							// currentTask could be something other at time of runnable execution
							final RESTfulInterface.OnPostMultipartCompleteListener pmc = mCurrentTask.postMultipartCompleteCallback;
							final String pmps = mCurrentTask.out_string;
							if(pmc != null) // check for null
								mCurrentTask.callbackHandler.post(new Runnable() {
									@Override
									public void run() {
										try {
											pmc.onComplete(pmps);
										}
										catch(NullPointerException e) {
										}
									}
								});
						}
						break;


						case Task.MODE_GETFILE:
							if(mDoLog) Log.d(TAG, "got getfile " + mCurrentTask.in_url + " to " + mCurrentTask.out_filename);
							printCookies();
							// here the callback is called from within the worker method
							mCurrentTask.out_string = getFile(
									mCurrentTask.in_url,
									mCurrentTask.out_filename,
									mCurrentTask.getFileProgressCallback);
							synchronized (RESTfulClient.this) { // do not interfere with cancelAll()
								// currentTask could be something other at time of runnable execution
								final RESTfulInterface.OnGetFileCompleteListener gfc = mCurrentTask.getFileCompleteCallback;
								final String gfcs = mCurrentTask.out_string;
								if(gfc != null) // check for null
									mCurrentTask.callbackHandler.post(new Runnable() {
										@Override
										public void run() {
											try {
												gfc.onComplete(gfcs);
											}
											catch(NullPointerException e) {
											}
										}
									});
							}
							break;

					}
				} catch (Exception e) {
					//TODO tell caller
				}

				// done with this Task, remove from queue
				mTaskQueue.poll();

			}


			if(mDoLog) Log.d(TAG, "Saying Goodbye");
		}


		private void printCookies() {
			List<Cookie> cookies = getCookies();
			if (cookies.isEmpty())
				if(mDoLog) Log.d(TAG, "No Cookies");
			else
				for (Cookie c : cookies)
					if(mDoLog) Log.d(TAG, "Cookie: " + c.toString());
		}

		public final ConcurrentLinkedQueue<Task> getQueue() {
			return mTaskQueue;
		}

		public void addTask(Task t) {
			mTaskQueue.add(t);
			synchronized (mTaskQueue) {
				mTaskQueue.notify();
			}
		}




		private String getString(String url)
		{
			status = SC_OK;

			if(mDoLog) Log.i(TAG, "getString on " +url);

			HttpGet httpGet = new HttpGet(url);

			try {
				HttpResponse response = mHttpClient.execute(httpGet);

				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					// we assume that the response body contains the error message
					HttpEntity entity = response.getEntity();
					if(entity != null) {
						ByteArrayOutputStream ostream = new ByteArrayOutputStream();
						response.getEntity().writeTo(ostream);
						if(mDoLog) Log.e(TAG, "getString Error: " + ostream.toString());
					}
					else
						if(mDoLog) Log.e(TAG, "getString Error: Server did not give reason");

					return null;
				}

				if(mDoLog) Log.i(TAG, "getString Success for query " + url);

				HttpEntity entity = response.getEntity();
				if (entity != null) {

					InputStream instream = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
					StringBuilder sb = new StringBuilder();

					String line = null;

					while ((line = reader.readLine()) != null)
						sb.append(line + '\n');

					String result=sb.toString();

					if(mDoLog) Log.i(TAG,result);

					instream.close();

					return result;
				}
			}
			catch (Throwable e){
				if(mDoLog) Log.e(TAG, "getString error for query " + url, e);
				status = SC_ERR;
			}finally{
				httpGet.abort();
				mHttpClient.getConnectionManager().closeIdleConnections(1, TimeUnit.MILLISECONDS);
			}

			return null;
		}


		private byte[] getRawData(String url) {

			status = SC_OK;

			if(mDoLog) Log.i(TAG, "getRawData on " +url);


			HttpGet httpGet = new HttpGet(url);

			try {
				HttpResponse response = mHttpClient.execute(httpGet);

				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					// we assume that the response body contains the error message
					HttpEntity entity = response.getEntity();
					if(entity != null) {
						ByteArrayOutputStream ostream = new ByteArrayOutputStream();
						response.getEntity().writeTo(ostream);
						if(mDoLog) Log.e(TAG, "getRawData Error: " + ostream.toString());
					}
					else
						if(mDoLog) Log.e(TAG, "getRawData Error: Server did not give reason");

					return null;
				}

				HttpEntity entity = response.getEntity();
				if (entity != null) {

					InputStream in = entity.getContent();

					// Now that the InputStream is open, get the content length
					long contentLength = entity.getContentLength();

					long bytesRead = 0;

					// To avoid having to resize the array over and over and over as
					// bytes are written to the array, provide an accurate estimate of
					// the ultimate size of the byte array
					ByteArrayOutputStream out;
					if (contentLength != -1) {
						out = new ByteArrayOutputStream((int)contentLength);
					} else {
						out = new ByteArrayOutputStream(16384); // Pick some appropriate size
					}

					byte[] buf = new byte[512];
					while (true) {
						int len = in.read(buf);
						if (len == -1) {
							break;
						}
						out.write(buf, 0, len);
						bytesRead += len;
						if(isInterrupted()) // stop reading if thread got a pending interrupt
							break;
					}
					in.close();
					out.close();

					if(mDoLog) Log.i(TAG, "getRawData Success for query '" +url + "' read " + bytesRead + " of " + contentLength);

					return out.toByteArray();
				}
			}
			catch (Throwable e){
				if(mDoLog) Log.e(TAG, "getRawData error for query " + url, e);
				status = SC_ERR;
			}finally{
				httpGet.abort();
				mHttpClient.getConnectionManager().closeIdleConnections(1, TimeUnit.MILLISECONDS);
			}

			return null;

		}


		private String getFile(String url, String filename, final RESTfulInterface.OnGetFileProgressListener progressCallback) {

			status = SC_OK;

			if(mDoLog) Log.i(TAG, "getFile on " +url);

			HttpGet httpGet = new HttpGet(url);

			try {
				HttpResponse response = mHttpClient.execute(httpGet);

				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					// we assume that the response body contains the error message
					HttpEntity entity = response.getEntity();
					if(entity != null) {
						ByteArrayOutputStream ostream = new ByteArrayOutputStream();
						response.getEntity().writeTo(ostream);
						if(mDoLog) Log.e(TAG, "getFile Error: " + ostream.toString());
					}
					else
					if(mDoLog) Log.e(TAG, "getFile Error: Server did not give reason");

					return null;
				}

				HttpEntity entity = response.getEntity();
				if (entity != null) {

					InputStream in = entity.getContent();

					// Now that the InputStream is open, get the content length
					long contentLength = entity.getContentLength();

					long bytesRead = 0;

					FileOutputStream out = new FileOutputStream(filename);

					byte[] buf = new byte[8192];
					while (true) {
						int len = in.read(buf);
						if (len == -1) {
							break;
						}
						out.write(buf, 0, len);
						bytesRead += len;

						final long num = bytesRead;

						synchronized (RESTfulClient.this) { // do not interfere with cancelAll()
							if(progressCallback != null) // check for null
								mCurrentTask.callbackHandler.post(new Runnable() {
									@Override
									public void run() {
										progressCallback.onProgress(num);
									}
								});
						}

						if(isInterrupted()) // stop reading if thread got a pending interrupt
							break;
					}
					in.close();
					out.close();

					if(mDoLog) Log.i(TAG, "getFile Success for query '" +url + "' read " + bytesRead + " of " + contentLength);

					return filename;
				}
			}
			catch (Throwable e){
				if(mDoLog) Log.e(TAG, "getFile error for query " + url, e);
				status = SC_ERR;
			}finally{
				httpGet.abort();
				mHttpClient.getConnectionManager().closeIdleConnections(1, TimeUnit.MILLISECONDS);
			}

			return null;

		}



		private JSONObject getJSON(String url)
		{
			status = SC_OK;

			HttpGet httpGet = new HttpGet(url);

			try {
				HttpResponse response = mHttpClient.execute(httpGet);

				if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
					// we assume that the response body contains the error message
					HttpEntity entity = response.getEntity();
					if(entity != null) {
						ByteArrayOutputStream ostream = new ByteArrayOutputStream();
						response.getEntity().writeTo(ostream);
						if(mDoLog) Log.e(TAG, "getJSON Error: " + ostream.toString());
					}
					else
						if(mDoLog) Log.e(TAG, "getJSON Error: Server did not give reason");

					return null;
				}

				if(mDoLog) Log.i(TAG, "getJSON Success for query " + url);

				HttpEntity entity = response.getEntity();
				if (entity != null) {

					InputStream instream = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
					StringBuilder sb = new StringBuilder();

					String line = null;

					while ((line = reader.readLine()) != null)
						sb.append(line + "\n");

					String result=sb.toString();

					if(mDoLog) Log.i(TAG,result);

					instream.close();

					return new JSONObject(result);
				}
			}
			catch (Throwable e){
				if(mDoLog) Log.e(TAG, "getJSON error for query " + url, e);
				status = SC_ERR;
			}finally{
				httpGet.abort();
				mHttpClient.getConnectionManager().closeIdleConnections(1, TimeUnit.MILLISECONDS);
			}

			return null;
		}


		private String postJSON(String url, JSONObject data)
		{
			status = SC_OK;
			HttpPost httpPost = new HttpPost(url);

			StringEntity se = null;
			try {
				se = new StringEntity(data.toString());
			} catch (UnsupportedEncodingException e1) {
				if(mDoLog) Log.e(TAG, "postJSON error to " + url, e1);
				status = SC_ERR;
			}
			httpPost.setEntity(se);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");

			try {
				HttpResponse response= mHttpClient.execute(httpPost);

				if(mDoLog) Log.i(TAG, "postJSON to " + url + " , code: " + response.getStatusLine().getStatusCode());

				// print response body in any case
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();
				response.getEntity().writeTo(ostream);
				String answer  =  ostream.toString();

				ostream.close();

				if(mDoLog) Log.i(TAG, "postJSON to:" + url + " , response: " + answer);

				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
					return answer;
			}
			catch (Throwable e) {
				status = SC_ERR;
				if(mDoLog) Log.e(TAG, "postJSON error to " + url, e);
			}
			finally {
				httpPost.abort();
				mHttpClient.getConnectionManager().closeIdleConnections(1, TimeUnit.MILLISECONDS);
			}

			return null;
		}


		private String postMultipart(String url, InputStream[] inStreams, String[] mimeTypes, String[] filenames, final RESTfulInterface.OnPostMultipartProgressListener progressCallback) {
			status = SC_OK;
			HttpPost httpPost = new HttpPost(url);


			CountingMultipartEntity multipartEntity = new CountingMultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE,
					new CountingMultipartEntity.ProgressListener() {
						@Override
						public void transferred(final long num) {

							synchronized (RESTfulClient.this) { // do not interfere with cancelAll()
								if(progressCallback != null) // check for null
									mCurrentTask.callbackHandler.post(new Runnable() {
										@Override
										public void run() {
											progressCallback.onProgress(num);
										}
									});
							}
						}
					});


			for(int i=0; i<inStreams.length; ++i) {
				InputStream is = inStreams[i];
				// this assumes that all the arrays are of the same size, otherwise exception but WTF
				String mimeType = mimeTypes[i];
				String filename = filenames[i];
				multipartEntity.addPart("RESTfulClientData" + i, new InputStreamBody(is, mimeType, filename));
			}


			httpPost.setEntity(multipartEntity);


			try {
				HttpResponse response= mHttpClient.execute(httpPost);

				if(mDoLog) Log.i(TAG, "postMultipart to " + url + " , code: " + response.getStatusLine().getStatusCode());

				// print response body in any case
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();
				response.getEntity().writeTo(ostream);
				if(mDoLog) Log.i(TAG, "postMultipart to:" + url + " , response: " + ostream.toString());

				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
					return ostream.toString();
			}
			catch (Throwable e) {
				status = SC_ERR;
				if(mDoLog) Log.e(TAG, "postMultipart error to " + url, e);
			}
			finally {
				httpPost.abort();
				mHttpClient.getConnectionManager().closeIdleConnections(1, TimeUnit.MILLISECONDS);
			}

			return null;
		}


	} // end workerthread

	private org.apache.http.conn.ssl.SSLSocketFactory createAdditionalCertsSSLSocketFactory(Context ctx, int res, String pass) {
	    try {
	        final KeyStore ks = KeyStore.getInstance("BKS");

	        // a bks file in res/raw
	        final InputStream in = ctx.getResources().openRawResource(res);
	        try {
	            // don't forget to put the password used above in strings.xml/mystore_password
	            ks.load(in, pass.toCharArray());
	        } finally {
	            in.close();
	        }

	        return new AdditionalKeyStoresSSLSocketFactory(ks);

	    } catch( Exception e ) {
	        throw new RuntimeException(e);
	    }
	}

}
