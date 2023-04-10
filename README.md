## This is a repo clone of [FOSS Browser](https://github.com/scoute-dich/browser/)
 
# ClassifyJS

ClassifyJS is an Android Java class that provides methods for classifying JavaScript code into different categories using machine learning models. It uses the ONNX Runtime library for inference with pre-trained models.

## Dependencies

The following libraries are required to use ClassifyJS:

ai.onnxruntime: This is the Java library for ONNX Runtime, which provides support for running ONNX models on Android.
com.google.gson: This is a popular Java library for working with JSON data.

## Usage

To use ClassifyJS in your Android project, follow these steps:

Add the ai.onnxruntime and com.google.gson dependencies to your project.
Create an instance of ClassifyJS by passing a Context object to its constructor, like this:
```java
Context context = ...; // Obtain a Context object from your Android application
ClassifyJS classifyJS = new ClassifyJS(context);
```

Call the `predict` method on the `classifyJS` object, passing a URL of a JavaScript file as an argument, to classify the JavaScript code into different categories. The `predict` method returns a `Map<Integer, Float>` object, where the keys are the category labels and the values are the predicted probabilities for each category.


```java
String url = "https://example.com/js/script.js"; // URL of the JavaScript file to be classified
Map<Integer, Float> predictions = classifyJS.predict(url);

// Access the predicted probabilities for each category
for (Map.Entry<Integer, Float> entry : predictions.entrySet()) {
    int label = entry.getKey();
    String category = CLASSIFICATION_LABELS.get(label);
    float probability = entry.getValue();
    Log.d("Prediction", "Category: " + category + ", Probability: " + probability);
}
```
Use the predicted category labels and probabilities to take further action in your application based on the classification results.

## Model Files

ClassifyJS uses two pre-trained ONNX models for classification: 

`classification` and `classification_tfidf`. These models are loaded from the `R.raw` resources in the Android project, and the model files should be placed in the `res/raw` directory of your Android project. The model files should be in the ONNX format and should have the following names:

* classification: This model is used for classification based on keyword features extracted from the JavaScript code.

* classification_tfidf: This model is used for classification based on term frequency-inverse document frequency (TF-IDF) features extracted from the JavaScript code.