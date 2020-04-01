# NL Pre-processer

A Java runnable tool to apply a Natural Language lexical pipeline to requirements textual data.

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

The *output.csv* file contains the bag-of-words output in the *req1* column.

## License

Free use of this software is granted under the terms of the [EPL version 2 (EPL2.0)](https://www.eclipse.org/legal/epl-2.0/)
