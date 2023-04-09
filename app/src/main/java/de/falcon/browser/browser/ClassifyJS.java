//package de.falcon.browser.view;
//
//import android.content.Context;
//import android.util.Log;
//import android.util.Pair;
//
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import ai.onnxruntime.NodeInfo;
//import ai.onnxruntime.OnnxTensor;
//import ai.onnxruntime.OrtEnvironment;
//import ai.onnxruntime.OrtException;
//import ai.onnxruntime.OrtSession;
//
//import ai.onnxruntime.OrtSession.SessionOptions;
//import ai.onnxruntime.OrtSession.Result;
//
//import java.io.BufferedInputStream;
//import java.io.ByteArrayOutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import de.baumann.browser.R;
//
//public class ClassifyJS {
//
//    // The ORT environment used to create the sessions for the classification models.
//    private OrtEnvironment env;
//    private OrtSession sessionClassification;
//    private OrtSession sessionClassificationTfidf;
//
//    // A set of keywords used to extract features from scripts for classification.
//    private Set<String> classificationKws;
//    // A vector of features used to classify scripts.
//    private List<String> classificationFeatures;
//
//    private static final String CLASSIFICATION_FEATURES_JSON = "classification_features";
//
//    // A map containing the names of the categories that scripts can be classified into.
//    private static final Map<Integer, String> CLASSIFICATION_LABELS = new HashMap<Integer, String>() {{
//        put(0, "marketing");
//        put(1, "cdn");
//        put(2, "tag-manager");
//        put(3, "video");
//        put(4, "customer-success");
//        put(5, "utility");
//        put(6, "ads");
//        put(7, "analytics");
//        put(8, "hosting");
//        put(9, "content");
//        put(10, "social");
//        put(11, "other");
//    }};
//
//
//    public ClassifyJS(Context context) throws IOException, OrtException {
//        Log.i("Session", "Session Init");
//        env = OrtEnvironment.getEnvironment();
//
//        InputStream classificationInputStream = context.getResources().openRawResource(R.raw.classification);
//        InputStream classificationTfidfInputStream = context.getResources().openRawResource(R.raw.classification_tfidf);
//
//        byte[] classificationBytes = new byte[0];
//        byte[] classificationTfidfBytes = new byte[0];
//
//        try {
//            classificationBytes = new byte[classificationInputStream.available()];
//            classificationInputStream.read(classificationBytes);
//            classificationInputStream.close();
//            sessionClassification = env.createSession(classificationBytes);
//
//            classificationTfidfBytes = new byte[classificationTfidfInputStream.available()];
//            classificationTfidfInputStream.read(classificationTfidfBytes);
//            classificationTfidfInputStream.close();
//            sessionClassificationTfidf = env.createSession(classificationTfidfBytes);
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (OrtException e) {
//            throw new RuntimeException(e);
//        }
//        for (NodeInfo i : sessionClassification.getInputInfo().values()) {
//            Log.i("Dev", "====> " + i.toString());
//        }
//
//        classificationFeatures = loadJsonFile(context, CLASSIFICATION_FEATURES_JSON).get("features");
//        classificationKws = getKwsFromFeatures(classificationFeatures);
//    }
//
//
//    private Map<String, List<String>> loadJsonFile(Context context, String jsonFilename) throws IOException {
//        InputStream inputStream = context.getResources().openRawResource(
//                context.getResources().getIdentifier(jsonFilename, "raw", context.getPackageName()));
//        byte[] buffer = new byte[inputStream.available()];
//        inputStream.read(buffer);
//        inputStream.close();
//        String json = new String(buffer, "UTF-8");
//        Gson gson = new Gson();
//        TypeToken<Map<String, List<String>>> token = new TypeToken<Map<String, List<String>>>() {
//        };
//
//
//        return gson.fromJson(json, token.getType());
//
//    }
//
//
//    private Set<String> getKwsFromFeatures(List<String> features) {
//        Set<String> classification_kws = new HashSet<>();
//
//        // Iterate through each input string
//        for (String element : features) {
//            // Split the input string by the "|" character
//            String feature = element;
//            String delimiter = "\\|";
//            String token;
//
//            while (feature.indexOf(delimiter) != -1) {
//                // Extract the individual keyword from the input string
//                int pos = feature.indexOf(delimiter);
//                token = feature.substring(0, pos);
//                // Remove any spaces from the keyword
//                token = token.replaceAll(" ", "");
//
//                // Add the keyword to the classification_kws set
//                classification_kws.add(token);
//
//                // Remove the keyword from the input string
//                feature = feature.substring(pos + delimiter.length());
//            }
//
//            // Remove any remaining spaces from the input string
//            feature = feature.replaceAll(" ", "");
//
//            // Add the remaining keyword to the classification_kws set
//            classification_kws.add(feature);
//        }
//
//        return classification_kws;
//    }
//
//    public Map<Integer, Float> predict(String url) throws OrtException {
//
//        // use the url to download js
//        String result = null;
//        try {
//            HttpURLConnection connection = (HttpURLConnection) new URL(urlOrJsContent).openConnection();
//            connection.setRequestMethod("GET");
//            connection.connect();
//            int responseCode = connection.getResponseCode();
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                InputStream inputStream = connection.getInputStream();
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//                StringBuilder stringBuilder = new StringBuilder();
//                String line;
//                while ((line = bufferedReader.readLine()) != null) {
//                    stringBuilder.append(line).append("\n");
//                }
//                result = stringBuilder.toString();
//                bufferedReader.close();
//            }
//            connection.disconnect();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        Strring message = result;
//
//
//        Map<Integer, Float> result = new HashMap<>();
//
//        String reducedScript = getScriptsClassificationFeatures(message, classificationKws, classificationFeatures);
//        String reducedScriptCopy = reducedScript;
//        System.out.println("reducedScript: " + sessionClassificationTfidf.getOutputInfo());
//        System.out.println("reducedScript: " + sessionClassification.getOutputInfo());
//
//
//        // Create an input tensor from the reducedScriptCopy
//        OnnxTensor inputTensor = OnnxTensor.createTensor(env, reducedScriptCopy);
//        // Create an output tensor
//        OnnxTensor outputTensor = OnnxTensor.createTensor(env, new long[]{1, 2});
//
//        // Run the classification model
//
//        //        float[] outputValues = outputTensor.getFloatBuffer().array();
//        //        for (int i = 0; i < outputValues.length; i++) {
//        //            result.put(i, outputValues[i]);
//        //        }
//
//        return result;
//
//    }
//
//    public static String getScriptsClassificationFeatures(String data, Set<String> classificationKws, List<String> classificationFeatures) {
//        List<String> features = getScriptsFeatures(data, classificationKws, classificationFeatures);
//        StringBuilder resultantFeatures = new StringBuilder();
//        for (String ft : features) {
//            resultantFeatures.append(ft).append(" ");
//        }
//        return resultantFeatures.toString();
//    }
//
//    public static List<String> getScriptsFeatures(String data, Set<String> kws, List<String> features) {
//        List<String> resultantFeatures = new ArrayList<>();
//        List<String> scriptsKws = new ArrayList<>();
//
//        for (String kw : kws) {
//            String kwNoSpaces = kw.replace(" ", ""); // remove spaces from kw
//            String kw1 = "." + kwNoSpaces + "(";
//
//            int pos = data.indexOf(kw1); // Find the first occurrence of kw1 in data
//
//            int count = 0;
//            while (pos != -1) { // Count the number of occurrences of kw1 in data
//                count++;
//                pos = data.indexOf(kw1, pos + 1);
//            }
//
//            for (int i = 0; i < count; i++) { // Add kw to scripts_kws for each occurrence of kw1 in data
//                scriptsKws.add(kwNoSpaces);
//            }
//        }
//
//        for (String ft : features) {
//            ft = ft.replace(" ", ""); // remove spaces from ft
//
//            if (!ft.contains("|")) {
//                int count = 0;
//                for (String kw : scriptsKws) {
//                    if (kw.equals(ft)) {
//                        count++;
//                    }
//                }
//                for (int i = 0; i < count; i++) {
//                    resultantFeatures.add(ft);
//                }
//            } else {
//                List<String> singularKws = new ArrayList<>();
//                String delimiter = "\\|";
//                String[] tokens = ft.split(delimiter);
//
//                for (String token : tokens) {
//                    singularKws.add(token);
//                }
//
//                int count = 0;
//                for (String kw : singularKws) {
//                    if (scriptsKws.contains(kw)) {
//                        count++;
//                    }
//                }
//                if (count == singularKws.size()) {
//                    resultantFeatures.add(ft);
//                }
//            }
//        }
//
//        return resultantFeatures;
//    }
//
//
//    private int argmax(float[] array) {
//        int index = 0;
//        float max = array[index];
//        for (int i = 1; i < array.length; i++) {
//            if (array[i] > max) {
//                max = array[i];
//                index = i;
//            }
//        }
//        return index;
//    }
//}