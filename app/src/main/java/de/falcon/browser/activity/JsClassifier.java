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

import ai.onnxruntime.OrtException;
import de.falcon.browser.view.JSModel;

public class JsClassifier {
    private final Context context;

    public JsClassifier(Context context) {
        this.context = context.getApplicationContext();

    }
    private static final String TAG = "JsClassifier";

    public void downloadAndLogJs(String url) {
        new DownloadJsTask(this.context).execute(url, 1);
    }

    private class DownloadJsTask extends AsyncTask<Object, Void, String> {

        private Context context;

        public DownloadJsTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Object... params) {
            String result = null;
            if (params.length < 2) {
                throw new IllegalArgumentException("DownloadJsTask requires at least 2 parameters: url or jsContent and mode");
            }
            String urlOrJsContent = params[0].toString();
            int mode = (int) params[1];

            if (mode == 1) {
                // Download JavaScript content from URL
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(urlOrJsContent).openConnection();
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
            } else if (mode == 2) {
                // Use the provided JavaScript code directly
                result = urlOrJsContent;
            } else {
                throw new IllegalArgumentException("Invalid mode parameter for DownloadJsTask: " + mode);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String jsContent) {
            Map<Integer, Float> results;
            if (jsContent != null) {
                Log.i(TAG, "Downloaded JavaScript content: " + jsContent);
                JSModel jsModel = null;
                try {
                     jsModel = new JSModel(this.context.getApplicationContext());

                } catch (IOException e) {
                    Log.e(TAG, "Failed to download JavaScript content.");
                    throw new RuntimeException(e);
                } catch (OrtException e) {
                    throw new RuntimeException(e);
                }
                try {
                    results = jsModel.predict(jsContent);
                } catch (OrtException e) {
                    throw new RuntimeException(e);
                }
//                String clusteringLabel = results.get("clustering");
                Log.i(TAG, "Downloaded JavaScript content: " + results);
            } else {
                Log.e(TAG, "Failed to download JavaScript content.");
            }
        }
    }
}


