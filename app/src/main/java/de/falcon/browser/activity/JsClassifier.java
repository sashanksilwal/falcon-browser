package de.falcon.browser.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import de.falcon.browser.view.JSModel;

public class JsClassifier {
    private final Context context;

    public JsClassifier(Context context) {
        this.context = context.getApplicationContext();

    }
    private static final String TAG = "JsClassifier";

    public void downloadAndLogJs(String url) {
        new DownloadJsTask(this.context).execute(url);
    }

    private class DownloadJsTask extends AsyncTask<String, Void, String> {

        private Context context;

        public DownloadJsTask(Context context) {
            this.context = context;
        }
        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            Log.i(TAG, url);
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
        protected void onPostExecute(String jsContent) {

            if (jsContent != null) {
                Log.i(TAG, "Downloaded JavaScript content: " + jsContent);
//                JSModel model = null;
//                try {
//                    JSModel jsModel = new JSModel(this.context.getApplicationContext());
//
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                Map<String, String> results = model.classify(jsContent);
//                String classificationLabel = results.get("classification");
//                String clusteringLabel = results.get("clustering");
//                Log.i(TAG, "Downloaded JavaScript content: " + classificationLabel+clusteringLabel);
            } else {
                Log.e(TAG, "Failed to download JavaScript content.");
            }
        }
    }
}


