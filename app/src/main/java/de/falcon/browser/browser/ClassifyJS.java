package de.falcon.browser.browser;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import ai.onnxruntime.OrtSession.Result;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;


import ai.onnxruntime.ValueInfo;
import de.baumann.browser.R;

public class ClassifyJS {

    // The ORT environment used to create the sessions for the classification models.
    private OrtEnvironment env;
    private OrtSession sessionClassification;
    private OrtSession sessionClassificationTfidf;

    String message = null;
    private static final String CACHE_FILE = "blocks.txt";
    private static final Set<String> blocks = new HashSet<>();

    private static final Locale locale = Locale.getDefault();


    // A set of keywords used to extract features from scripts for classification.
    private Set<String> classificationKws;
    // A vector of features used to classify scripts.
    private List<String> classificationFeatures;

    private static final String CLASSIFICATION_FEATURES_JSON = "classification_features";

    // A map containing the names of the categories that scripts can be classified into.
    private static final Map<Integer, String> CLASSIFICATION_LABELS = new HashMap<Integer, String>() {{
        put(0, "marketing");
        put(1, "cdn");
        put(2, "tag-manager");
        put(3, "video");
        put(4, "customer-success");
        put(5, "utility");
        put(6, "ads");
        put(7, "analytics");
        put(8, "hosting");
        put(9, "content");
        put(10, "social");
        put(11, "other");
    }};


    public ClassifyJS(Context context )   {
        Log.i("Session", "Session Init");
        // Create an OrtEnvironment instance
        env = OrtEnvironment.getEnvironment();

        // Load the classification and classification_tfidf model files from raw resources
        InputStream classificationInputStream = context.getResources().openRawResource(R.raw.classification);
        InputStream classificationTfidfInputStream = context.getResources().openRawResource(R.raw.classification_tfidf);

        byte[] classificationBytes = new byte[0];
        byte[] classificationTfidfBytes = new byte[0];

        try {
            // Read the classification model file into a byte array and create an OrtSession instance
            classificationBytes = new byte[classificationInputStream.available()];
            classificationInputStream.read(classificationBytes);
            classificationInputStream.close();
            sessionClassification = env.createSession(classificationBytes);

            // Read the classification_tfidf model file into a byte array and create an OrtSession instance
            classificationTfidfBytes = new byte[classificationTfidfInputStream.available()];
            classificationTfidfInputStream.read(classificationTfidfBytes);
            classificationTfidfInputStream.close();
            sessionClassificationTfidf = env.createSession(classificationTfidfBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
        try 
        {
            // Get input information for the classification model and print it to log
            // for (NodeInfo i : sessionClassification.getInputInfo().values()) {
            //     Log.i("Dev", "====> " + i.toString());
            // }

            // Load the features from a JSON file and store them in classificationFeatures variable
            classificationFeatures = loadJsonFile(context, CLASSIFICATION_FEATURES_JSON).get("features");
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }

        // Get the keywords from the features and store them in classificationKws variable
        classificationKws = getKwsFromFeatures(classificationFeatures);

        File file = new File(context.getDir("filesdir", Context.MODE_PRIVATE) + "/" + CACHE_FILE);
        if (!file.exists()) {
            //copy blocks.txt from assets if not available
            Log.d("Blocks file", "does not exist");
            try {
                AssetManager manager = context.getAssets();
                copyFile(manager.open(CACHE_FILE), new FileOutputStream(file));
                //downlaodClassificatons(context);  //try to update blocks.txt from internet
            } catch (IOException e) {
                Log.e("browser", "Failed to copy asset file", e);
            }
        }
        if (blocks.isEmpty()) {
            loadBlockClassifications(context);
        }
    }

    private static void loadBlockClassifications(final Context context) {
        Thread thread = new Thread(() -> {
            try {
                File file = new File(context.getDir("filesdir", Context.MODE_PRIVATE) + "/" + CACHE_FILE);
                FileReader in = new FileReader(file);
                BufferedReader reader = new BufferedReader(in);
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#")) continue;
                    blocks.add(line.toLowerCase(locale));
                }
                in.close();
            } catch (IOException i) {
                Log.w("browser", "Error loading adBlockHosts", i);
            }
        });
        thread.start();
    }


    // Load a JSON file from raw resources and parse it into a Map<String, List<String>> object
    private Map<String, List<String>> loadJsonFile(Context context, String jsonFilename) throws IOException {
        // Open the JSON file from raw resources
        InputStream inputStream = context.getResources().openRawResource(
                context.getResources().getIdentifier(jsonFilename, "raw", context.getPackageName()));
        
        // Read the contents of the file into a byte array
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        inputStream.close();
        
        // Convert the byte array into a UTF-8 encoded string
        String json = new String(buffer, "UTF-8");
        
        // Create a Gson instance to parse the JSON string
        Gson gson = new Gson();
        
        // Define a TypeToken to specify the target type of the JSON parsing
        TypeToken<Map<String, List<String>>> token = new TypeToken<Map<String, List<String>>>() {};
        
        // Parse the JSON string into a Map<String, List<String>> object using Gson
        return gson.fromJson(json, token.getType());
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    private Set<String> getKwsFromFeatures(List<String> features) {
        Set<String> classification_kws = new HashSet<>();

        // Iterate through each input string
        for (String element : features) {
            // Split the input string by the "|" character
            String feature = element;
            String delimiter = "\\|";
            String token;

            while (feature.indexOf(delimiter) != -1) {
                // Extract the individual keyword from the input string
                int pos = feature.indexOf(delimiter);
                token = feature.substring(0, pos);
                // Remove any spaces from the keyword
                token = token.replaceAll(" ", "");

                // Add the keyword to the classification_kws set
                classification_kws.add(token);

                // Remove the keyword from the input string
                feature = feature.substring(pos + delimiter.length());
            }

            // Remove any remaining spaces from the input string
            feature = feature.replaceAll(" ", "");

            // Add the remaining keyword to the classification_kws set
            classification_kws.add(feature);
        }

        return classification_kws;
    }

    public Map<Integer, Float> predict(String url) throws OrtException {

        // Use the URL to download JavaScript code
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
                message = stringBuilder.toString();
                bufferedReader.close();
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Log the downloaded JavaScript code
        Log.i("JS-Link", url);
        Log.i("JS-Content", message);

        // write the downloaded JavaScript code to a file
        

        

        // Create a map to store the prediction results
        Map<Integer, Float> result = new HashMap<>();

        // Get the reduced script for classification
        String reducedScript = getScriptsClassificationFeatures(message, classificationKws, classificationFeatures);
        String reducedScriptCopy = reducedScript;

        // Log information about the classification models
        Log.i("Classification", sessionClassification.getOutputInfo().toString());
        Log.i("ClassificationTfidf", sessionClassificationTfidf.getOutputInfo().toString());

        // Create an array to hold the input data for the first classification model
        String[] floatInputArray = new String[1];

        // Create an input tensor from the reducedScriptCopy for the first classification model
        long[] inputShape_tfidf = new long[]{1, 1};
        floatInputArray[0] = reducedScriptCopy;
        OnnxTensor inputTensor = OnnxTensor.createTensor(env, floatInputArray, inputShape_tfidf);

        // Run the first classification model and get the output tensor
        Result output_tensor = sessionClassificationTfidf.run(Collections.singletonMap("float_input", inputTensor));

        // Create an array to hold the input data for the second classification model
        long[] inputShape = new long[]{1, 499};
        float[] input_tensor_values = new float[499];

        // Get the float values from the output tensor of the first classification model
        OnnxTensor outputTensor = (OnnxTensor) output_tensor.get(0);
        float[] floatArr = outputTensor.getFloatBuffer().array();

        // Copy non-zero float values to the input tensor array for the second classification model
        for (int i = 0; i < 499; i++) {
            if (floatArr[i] != 0) {
                input_tensor_values[i] = floatArr[i];
                // Log the non-zero float values
                // Log.i("ClassificationTfidf", i + ": " + floatArr[i]);
            }
        }

//        try {
//            // create the file in the Download folder
//            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "js.txt");
//            FileWriter writer = new FileWriter(file);
//            // write the value of floatInputArray to the file
//
//            writer.append(message);
//            writer.append(floatArr.toString());
//            writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // Create an input tensor from the input_tensor_values for the second classification model
        OnnxTensor inputTensor2 = OnnxTensor.createTensor(env, FloatBuffer.wrap(input_tensor_values), inputShape);

        // Run the second classification model and get the output tensor
        Result output_tensor2 = sessionClassification.run(Collections.singletonMap("float_input", inputTensor2));

        OnnxValue outputTensor2 = output_tensor2.get(1);
        ValueInfo mapInfo = outputTensor2.getInfo();

        Object outputValue = output_tensor2.get(1).getValue();
        String type = outputValue.getClass().getName();

        
//        // values_ort.GetTensorMutableData<float>();
//        // log the outputValue and outputTensor2
//        Log.i("OutputValue", outputValue.toString());
//        Log.i("OutputTensor2", outputTensor2.toString());
//        Log.i("Output_Tensor2", output_tensor2.toString());
//        // log type
//        Log.i("Type-", type);

//        Log.d("HighestValue",   getHighestValue(result) );

        return result;
    }


    public static String getScriptsClassificationFeatures(String data, Set<String> classificationKws, List<String> classificationFeatures) {
        List<String> features = getScriptsFeatures(data, classificationKws, classificationFeatures);
        StringBuilder resultantFeatures = new StringBuilder();
        for (String ft : features) {
            resultantFeatures.append(ft).append(" ");
        }
        return resultantFeatures.toString();
    }

    public static List<String> getScriptsFeatures(String data, Set<String> kws, List<String> features) {
        List<String> resultantFeatures = new ArrayList<>();
        List<String> scriptsKws = new ArrayList<>();

        for (String kw : kws) {
            String kwNoSpaces = kw.replace(" ", ""); // remove spaces from kw
            String kw1 = "." + kwNoSpaces + "(";

            int pos = data.indexOf(kw1); // Find the first occurrence of kw1 in data

            int count = 0;
            while (pos != -1) { // Count the number of occurrences of kw1 in data
                count++;
                pos = data.indexOf(kw1, pos + 1);
            }

            for (int i = 0; i < count; i++) { // Add kw to scripts_kws for each occurrence of kw1 in data
                scriptsKws.add(kwNoSpaces);
            }
        }

        for (String ft : features) {
            ft = ft.replace(" ", ""); // remove spaces from ft

            if (!ft.contains("|")) {
                int count = 0;
                for (String kw : scriptsKws) {
                    if (kw.equals(ft)) {
                        count++;
                    }
                }
                for (int i = 0; i < count; i++) {
                    resultantFeatures.add(ft);
                }
            } else {
                List<String> singularKws = new ArrayList<>();
                String delimiter = "\\|";
                String[] tokens = ft.split(delimiter);

                for (String token : tokens) {
                    singularKws.add(token);
                }

                int count = 0;
                for (String kw : singularKws) {
                    if (scriptsKws.contains(kw)) {
                        count++;
                    }
                }
                if (count == singularKws.size()) {
                    resultantFeatures.add(ft);
                }
            }
        }

        return resultantFeatures;
    }


    private int argmax(float[] array) {
        int index = 0;
        float max = array[index];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }

    // function that takes Map<Integer, Float> result and returns the key with the highest value
    private int getHighestValue(Map<Integer, Float> result) {
        int highestValue = 0;
        for (Map.Entry<Integer, Float> entry : result.entrySet()) {
            if (entry.getValue() > highestValue) {
                highestValue = entry.getKey();
            }
        }
        return highestValue;
    }
    //    private static void loadHosts(final Context context) {
//        Thread thread = new Thread(() -> {
//            try {
//                File file = new File(context.getDir("filesdir", Context.MODE_PRIVATE) + "/" + CACHE_FILE);
//                FileReader in = new FileReader(file);
//                BufferedReader reader = new BufferedReader(in);
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    if (line.startsWith("#")) continue;
//                    blocks.add(line.toLowerCase(locale));
//                }
//                in.close();
//            } catch (IOException i) {
//                Log.w("browser", "Error loading adBlockHosts", i);
//            }
//        });
//        thread.start();
//    }

//    public static void downlaodClassificatons(final Context context) {
//        Thread thread = new Thread(() -> {
//
//            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//            String hostURL = sp.getString("ab_hosts", "");
//
//            try {
//                URL url = new URL(hostURL);
//                Log.d("browser", "Download AdBlock hosts");
//                URLConnection connection = url.openConnection();
//                connection.setReadTimeout(5000);
//                connection.setConnectTimeout(10000);
//
//                InputStream is = connection.getInputStream();
//                BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
//
//                File tempfile = new File(context.getDir("filesdir", Context.MODE_PRIVATE) + "/temp.txt");
//
//                if (tempfile.exists()) {
//                    tempfile.delete();
//                }
//                tempfile.createNewFile();
//
//                FileOutputStream outStream = new FileOutputStream(tempfile);
//                byte[] buff = new byte[5 * 1024];
//
//                int len;
//                while ((len = inStream.read(buff)) != -1) {
//                    outStream.write(buff, 0, len);
//                }
//
//                outStream.flush();
//                outStream.close();
//                inStream.close();
//
//                //now remove leading 0.0.0.0 from file
//                FileReader in = new FileReader(tempfile);
//                BufferedReader reader = new BufferedReader(in);
//                File outfile = new File(context.getDir("filesdir", Context.MODE_PRIVATE) + "/" + CACHE_FILE);
//                FileWriter out = new FileWriter(outfile);
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    if (line.startsWith("0.0.0.0 ")) {
//                        line = line.substring(8);
//                    }
//                    out.write(line + "\n");
//                }
//                in.close();
//                out.close();
//
//                tempfile.delete();
//
//                blocks.clear();
//                loadHosts(context);  //reload hosts after update
//                Log.w("browser", "AdBlock hosts updated");
//
//            } catch (IOException i) {
//                Log.w("browser", "Error updating AdBlock hosts", i);
//            }
//        });
//        thread.start();
//    }
}