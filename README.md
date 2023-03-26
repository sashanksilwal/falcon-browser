## This is a repo clone of [FOSS Browser](https://github.com/scoute-dich/browser/)
 
## loadUrlAndDownloadJs
The loadUrlAndDownloadJs class is responsible for loading a given URL and downloading all the JavaScript files linked to that page.

### How to use

1. Create an instance of loadUrlAndDownloadJs class.
2. Call the loadUrl method with the URL you want to load as the parameter.
3. The class will download the HTML content from the given URL, extract all the JavaScript links and download each of them using the DownloadHtmlTask class.
4. After downloading all the JavaScript files, the class will load the HTML content in the web view.
Dependencies
5. The loadUrlAndDownloadJs class depends on the DownloadHtmlTask class, which is responsible for downloading HTML content and extracting JavaScript links.

### Class methods
*loadUrl(String url)*

This method loads the given URL in the web view, downloads all the JavaScript files linked to that page and then loads the HTML content in the web view.

## DownloadHtmlTask class
The DownloadHtmlTask class is responsible for downloading HTML content from a given URL and extracting JavaScript links from it.

### How to use

1. Create an instance of DownloadHtmlTask class.
2. Call the execute method with the URL you want to download as the parameter.
3. The class will download the HTML content from the given URL and extract all the JavaScript links.

### Class methods

*execute(String url)*

This method downloads the HTML content from the given URL and returns it as a string.

*getJsLinks(String htmlContent)*

This method takes HTML content as input and returns a set of all the JavaScript links present in that HTML content.

## JsClassifier class

The JsClassifier class is responsible for downloading JavaScript content from a given URL and classifying it using a JSModel.

### How to use

1. Create an instance of JsClassifier class, passing a `Context` object to the constructor.
2. Call the `downloadAndLogJs` method with the URL you want to download as the parameter.
3. The class will download the JavaScript content from the given URL and log it.

### Class methods

- `downloadAndLogJs(String url)`

This method downloads the JavaScript content from the given URL and logs it.

- `DownloadJsTask`

This inner class is responsible for downloading the JavaScript content in the background using an `AsyncTask`. Once the download is complete, it logs the downloaded content.

- `onPostExecute(String jsContent)`

This method is called when the download is complete. It takes the downloaded JavaScript content as input and uses a `JSModel` to classify it. The classification results are then logged.


## DownloadJsTask class
The DownloadJsTask class is responsible for downloading JavaScript content from a given URL.

### How to use
1. Create an instance of DownloadJsTask class with a Context object.
2. Call the execute method with the URL you want to download as the parameter.
3. The class will download the JavaScript content from the given URL and return it as a string.
4. The downloaded content can then be passed to a JSModel for classification.

### Class methods
- `DownloadJsTask(Context context)`

This constructor initializes the DownloadJsTask object with a Context object.

- `doInBackground(String... urls)`

This method downloads the JavaScript content from the given URL and returns it as a string.

- `onPostExecute(String jsContent)`

This method logs the downloaded JavaScript content. It can be modified to perform classification using a JSModel.

