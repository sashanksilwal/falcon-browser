package de.falcon.browser.view;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.baumann.browser.R;

public class JSModel {

    private static final String CLASSIFICATION_TFIDF_MODEL = "classification_tfidf_model";
//    private static final String CLUSTERING_TFIDF_MODEL = "clustering_tfidf_model";
    private static final String CLASSIFICATION_MODEL = "classification_model";
//    private static final String CLUSTERING_MODEL = "clustering_model";
    private static final String CLASSIFICATION_FEATURES_JSON = "classification_features.json";
    private static final String CLUSTERING_FEATURES_JSON = "clustering_features.json";

    private final Map<String, String> classificationLabels = new HashMap<String, String>() {{
        put("0", "marketing");
        put("1", "cdn");
        put("2", "tag-manager");
        put("3", "video");
        put("4", "customer-success");
        put("5", "utility");
        put("6", "ads");
        put("7", "analytics");
        put("8", "hosting");
        put("9", "content");
        put("10", "social");
        put("11", "other");
    }};

    private final Map<String, String> clusteringLabels = new HashMap<String, String>() {{
        put("1", "noncritical");
        put("0", "critical");
    }};

    private Interpreter classificationTfidfInterpreter;
    private Interpreter clusteringTfidfInterpreter;
    private Interpreter classificationInterpreter;
    private Interpreter clusteringInterpreter;
    private List<String> classificationFeatures;
    private List<String> clusteringFeatures;
    private List<String> classificationKws;
    private List<String> clusteringKws;

    public JSModel(Context context ) throws IOException {
//        classificationTfidfInterpreter = new Interpreter(loadModelFile(context,CLASSIFICATION_TFIDF_MODEL));

//        clusteringTfidfInterpreter = new Interpreter(loadModelFile(CLUSTERING_TFIDF_MODEL));
//        classificationInterpreter = new Interpreter(loadModelFile(CLASSIFICATION_MODEL));
//        clusteringInterpreter = new Interpreter(loadModelFile(CLUSTERING_MODEL));
//        classificationFeatures = loadJsonFile(CLASSIFICATION_FEATURES_JSON).get("features");
        clusteringFeatures = loadJsonFile(context, CLUSTERING_FEATURES_JSON).get("features");
//        classificationKws = getKwsFromFeatures(classificationFeatures);
//        clusteringKws = getKwsFromFeatures(clusteringFeatures);
    }

//    private MappedByteBuffer loadModelFile(Context c, String modelFilename) throws IOException {
//        InputStream inputStream = c.getResources().openRawResource(R.raw.classification_tfidf_model);
//        StringBuilder sb = new StringBuilder();
//        byte[] buffer = new byte[1024];
//        int bytesRead;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            sb.append(new String(buffer, 0, bytesRead));
//        }
//        Log.i("loadModelFile", "Input stream contents: " + sb.toString());
//        if (inputStream == null) {
//            Log.e("loadModelFile", "Failed to open resource file: " + modelFilename);
//            throw new IOException("Failed to open resource file: " + modelFilename);
//        } else {
//            Log.i("loadModelFile", "Resource file: " + modelFilename);
//
//        }
//        if (inputStream instanceof FileInputStream) {
//            FileChannel fileChannel = ((FileInputStream) inputStream).getChannel();
//            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
//        } else {
//            byte[] modelBuffer = IOUtils.toByteArray(inputStream);
//            ByteBuffer buffer = ByteBuffer.allocateDirect(modelBuffer.length)
//                    .order(ByteOrder.nativeOrder());
//            buffer.put(modelBuffer);
//            buffer.flip();
//            return (MappedByteBuffer) buffer;
//        }
//    }



    private Map<String, List<String>> loadJsonFile(Context context, String jsonFilename) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(
                context.getResources().getIdentifier(jsonFilename, "raw", context.getPackageName()));
//        byte[] buffer = new byte[inputStream.available()];
//        inputStream.read(buffer);
//        inputStream.close();
//        String json = new String(buffer, "UTF-8");
//        Gson gson = new Gson();
//        TypeToken<Map<String, List<String>>> token = new TypeToken<Map<String, List<String>>>() {
//        };
//        Log.i("Contents of file {}: {}", jsonFilename + json);

//        return gson.fromJson(json, token.getType());
        return null;
    }


    private List<String> getKwsFromFeatures(List<String> features) {
        List<String> kws = new ArrayList<>();
        for (String feature : features) {
            String[] parts = feature.split("_");
            if (parts.length > 1) {
                String kw = parts[1].replaceAll("\\d", "");
                if (!kws.contains(kw)) {
                    kws.add(kw);
                }
            }
        }
        Collections.sort(kws);
        return kws;
    }
    public Map<String, Object> predict(String message) {
        Map<String, Object> result = new HashMap<>();
//        try {
//            // tokenize the message
//            String[] tokens = message.split("\\s+");
//            // tfidf encoding of tokens
//            byte[] classificationTfidfOutput = tfidfEncode(tokens, classificationFeatures, classificationTfidfInterpreter);
//            byte[] clusteringTfidfOutput = tfidfEncode(tokens, clusteringFeatures, clusteringTfidfInterpreter);
//
//            // classification model inference
//            float[] classificationModelOutput = new float[classificationLabels.size()];
//            classificationInterpreter.run(classificationTfidfOutput, classificationModelOutput);
//            int classificationIndex = argmax(classificationModelOutput);
//
//            // clustering model inference
//            float[] clusteringModelOutput = new float[clusteringLabels.size()];
//            clusteringInterpreter.run(clusteringTfidfOutput, clusteringModelOutput);
//            int clusteringIndex = argmax(clusteringModelOutput);
//
//            // prepare results
//            result.put("category", classificationLabels.get(String.valueOf(classificationIndex)));
//            result.put("criticality", clusteringLabels.get(String.valueOf(clusteringIndex)));
//            result.put("keywords", classificationKws);
//        } catch (Exception e) {
//            result.put("error", "Failed to predict category and criticality");
//            e.printStackTrace();
//        }
        return result;
    }

    private byte[] tfidfEncode(String[] tokens, List<String> features, Interpreter interpreter) {
        byte[] input = new byte[features.size()];
        Arrays.fill(input, (byte) 0.0f);
        for (String token : tokens) {
            if (features.contains("tf_" + token)) {
                int index = features.indexOf("tf_" + token);
                input[index] += 1.0f;
            }
        }
        byte[] output = new byte[features.size()];
        interpreter.run(ByteBuffer.wrap(input), ByteBuffer.wrap(output));
        return output;
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
}