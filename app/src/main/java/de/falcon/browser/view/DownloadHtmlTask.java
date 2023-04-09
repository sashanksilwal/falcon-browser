package de.falcon.browser.view;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class DownloadHtmlTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "DownloadHTML";
    @Override
    protected String doInBackground(String... urls) {
        String url = urls[0];
        String result = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                result = stringBuilder.toString();
                bufferedReader.close();
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String htmlContent) {
        if (htmlContent != null) {
            Log.i(TAG, "Downloaded HTML content: " + htmlContent);
        } else {
            Log.e(TAG, "Failed to download HTML content.");
        }
    }

    public Set<String> getJsLinks(String htmlContent, String baseUrl) {
        Set<String> jsLinks = new HashSet<>();
        Document doc = Jsoup.parse(htmlContent, baseUrl);
        Elements scripts = doc.getElementsByTag("script");
        for (Element script : scripts) {
            String src = script.attr("src");
            if (!src.isEmpty() && src.endsWith(".js")) {
                if (src.startsWith("http") || src.startsWith("https")) {
                    // The link is already a direct link, add it to the set
                    jsLinks.add(src);
                } else {
                    // The link is relative, make it a complete path and add it to the set
                    URL url;
                    try {
                        url = new URL(baseUrl);
                        URL absoluteUrl = new URL(url, src);
                        jsLinks.add(absoluteUrl.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return jsLinks;
    }
}

