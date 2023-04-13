## This is a repo clone of [FOSS Browser](https://github.com/scoute-dich/browser/)
 
# ClassifyJS

ClassifyJS is an Android Java class that provides methods for classifying JavaScript code into different categories using machine learning models. It uses the ONNX Runtime library for inference with pre-trained models.

## Instance Variables:
- `env`: An instance of the OrtEnvironment class used to create sessions for the classification models.
- `sessionClassification`: An OrtSession instance for the classification model.
- `sessionClassificationTfidf`: An OrtSession instance for the classification_tfidf model.
- `message`: A String variable that appears to be unused.
- `CACHE_FILE`: A String constant representing the name of the cache file.
- `blocks`: A HashSet that stores blocks of script classifications.
- `locale`: A Locale object representing the default locale.
- `classificationKws`: A Set of keywords used to extract features from scripts for classification.
- `classificationFeatures`: A List of features used to classify scripts.

## Constants:
- `CLASSIFICATION_FEATURES_JSON`: A constant String representing the name of the JSON file containing classification features.
- `CLASSIFICATION_LABELS`: A constant Map containing the names of the categories that scripts can be classified into, where the keys are Integer values representing the categories and the values are String labels for the categories.

## Constructor:
The `ClassifyJS` class has a constructor that takes a `Context` object as a parameter and initializes the instance variables. It loads the classification and classification_tfidf model files from raw resources, reads the features from a JSON file, and stores them in the `classificationFeatures` variable. It also initializes the `classificationKws` variable with the keywords extracted from the features. Additionally, it loads blocks of script classifications from a cache file.

## Methods:
- `loadJsonFile(Context context, String jsonFilename)`: A private method that loads a JSON file from raw resources, parses it into a Map object, and returns it. It takes a `Context` object and a `String` representing the name of the JSON file as parameters.
- `getKwsFromFeatures(List<String> features)`: A private method that takes a list of features as input and extracts keywords from the features. It returns a Set of keywords.
- `classify(String script)`: A public method that takes a JavaScript script as input, extracts features from the script, and classifies the script into one of the predefined categories using the loaded classification model. It returns a String representing the category label.
- `classifyTfidf(String script)`: A public method that takes a JavaScript script as input, extracts features from the script, and classifies the script into one of the predefined categories using the loaded classification_tfidf model. It returns a String representing the category label.
- `classifyBlocks(String script)`: A public method that takes a JavaScript script as input and checks if the script matches any of the blocks of script classifications loaded from the cache file. It returns a String representing the category label if a match is found, or null if no match is found.
- `clearCache()`: A public method that clears the blocks cache.
- `getCategoryLabels()`: A public method that returns a Map of category labels and their corresponding category names.
- `getBlocks()`: A public method that returns a Set of blocks of script classifications loaded from the cache file.

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

