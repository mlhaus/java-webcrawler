# java-webcrawler

1. Make sure [Java](https://www.oracle.com/java/technologies/downloads/) and [Maven](https://maven.apache.org/download.cgi) are installed.
2. [Clone this project](https://github.com/mlhaus/java-webcrawler). 
3. Open the Terminal and navigate to the project folder. 
4. Enter this command to run the unit tests and compile the project
    ```
    mvn package
    ```
5. Enter this command to run the parallel web crawler.
   - The parallel web crawler is a threaded program that performs multiple tasks simultaneously.
    ```
    java -classpath target/webcrawler-1.0.jar \
        com.webcrawler.main.WebCrawlerMain \
        src/main/config/sample_config_parallel.json
    ```
6. Enter this command to run the sequential web crawler.
    - The parallel web crawler is not a threaded program. It crawls one page at a time, following a step-by-step approach.
    ```
    java -classpath target/webcrawler-1.0.jar \
        com.webcrawler.main.WebCrawlerMain \
        src/main/config/sample_config_sequential.json
    ```