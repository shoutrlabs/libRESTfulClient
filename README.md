# libRESTfulClient - A Lightweight Async REST Client Library for Android

This Java library for Android enables calling code to issue asynchronous REST
calls and get notified about the results via callbacks. It is designed to be
used from an app's main UI thread, all network communication happens on a
separate worker thread.

To illustrate its genereal use, here is how to get a JSON object from a remote:

```java
Handler mainThreadHandler = new Handler(Looper.getMainLooper()); // callbacks will be executed on main thread this way
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

## Functionality

Here's what one can do with libRESTfulClient in a rough overview:

* Downloading files with progress updates
* Downloading and uploading JSON
* Downloading raw binary data
* Obtain entity sizes without downloading them
* Downloading strings
* Uploading all kinds of data as Multipart data
* Adding cookies to the calls
 
... all in an async callback-based fashion.

## Using it in one's app

For the time being, libRESTfulClient code is simply built with the app code,
so just clone the repository into your source tree (or add it as a submodule)
and add libRESTfulClient as a dependency to your app module's build.gradle like
this:

```gradle
dependencies {
    ...
    implementation project(':libRESTfulClient')
}

```

## License

LibRESTfulClient is made available under [a 2-clause BSD license](LICENSE).


