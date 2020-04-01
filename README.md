# NL Pre-processer

A Java runnable tool to apply a Natural Language lexical pipeline to requirements textual data.

It applies the following NL pipeline:

- Noisy text cleaning
- Stop word removal + excluded word removal
- Standarization (treure caracters extranys)
- Tokenization
- Stemming (KStemmer)
- TF-IDF discrimination

Notice that the processing of individual requirements can lead to uncertain results given that TF-IDF is based on the overall document corpus.

## How to build

Build the JAR using the following command:

```
mvn clean install package
```

## How to run

Run the tool using the following command:

```
java -jar preprocesser-1.0.0.jar input.csv output.csv
```

Where *input.csv* is a CSV format file including at least the following columns:

- *req1_id*: the ID of the first requirement
- *req1*: the textual information of the first requirement

The *output.csv* file contains the bag-of-words output in the *req1* column (auxiliary columns are included).

## License

Free use of this software is granted under the terms of the [EPL version 2 (EPL2.0)](https://www.eclipse.org/legal/epl-2.0/)
