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
