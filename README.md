# Test - InfluxDB - Data Ingestion

Tester of the InfluxDB ability of ingesting time series data

## Repository Structure
-   `data/`, containing the printers parsed logs files in the format of CSV files;
-   `logs/`, containing the log information of all the tests done;
-   `resources/`, containing the InfluxDB driver, the database credentials file and the logger properties;
-   `scripts/`, containing the bash code to switch the index type and empty the memory;
-   `src/main/java`, containing the java source files;
-   `target/`, containing the generated .class files after compiling the java code.

## Requirements
The repository is a Maven project. Therefore, the dependency that will automatically be downloaded is:
-   InfluxDB JDBC Driver (2.8)

## Installation and running the project
-   Create the folder `data`;
    -   Inside the folder, copy-paste the printers parsed log files, whose timestamp is defined in nanoseconds;
-   Inside the folder `resources`,
    -   Create a file called `server_influxdb_credentials.txt`, containing the username (first line) and the password (second line) to access the server InfluxDB database;
-   Run the project
    -   Open IntelliJ IDEA
    -   Compile the maven project
    -   Execute the main method in the `src/main/java/Main.java` file
    -   If needed, switch the indexes of the databases (see next paragraph).

## Switch index
If you want to change the index:
-   Execute `bash index/switch_index_to.sh inmem` to set the \"inmem\" index
-   Execute `bash index/switch_index_to.sh tsi1` to set the \"tsi1\" index
