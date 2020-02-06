# libRESTfulClient

This Java library for Android enables calling code to issue asynchronous REST
calls and get notified about the results via callbacks. It is designed to be
used from an app's main UI thread, all network communication happens on a
separate worker thread.

To illustrate its genereal use, here is how to get a JSON object from a remote:

```java
Handler mainThreadHandler = new Handler(Looper.getMainLooper());
RESTfulClient restlessClient = new RESTfulClient();

restlessClient.getJSON(mainThreadHandler, "https://example.io/api/users", new RESTfulInterface.OnGetJSONCompleteListener() {
    @Override
    public void onComplete(JSONObject returned) {

        if(returned != null) {
            // do something with JSON
        } else {
            // handle error case
        }

    }
});

```