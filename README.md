# Test - InfluxDB - Data Ingestion

Tester of the InfluxDB ability of ingesting time series data

## Repository Structure
-   `data/`, containing the printers parsed logs files in the format of CSV files;
-   `logs/`, containing the log information of all the tests done;
-   `resources/`, containing the InfluxDB driver, the database credentials file and the logger properties;
-   `src/`, containing the java source files;
-   `target/`, containing the generated .class files after compiling the java code.

## Requirements
-   InfluxDB JDBC Driver (2.8)

## Installation and running the project
-   Create the folder `data`;
    -   Inside the folder, copy-paste the printers parsed log files, whose timestamp is defined in nanoseconds;
-   Inside the folder `resources`,
    -   Create a file called `server_influxdb_credentials.txt`, containing the username (first line) and the password (second line) to access the server InfluxDB database;
-   TODO
