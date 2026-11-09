# CSV Analytics Dashboard (Spring Boot + MySQL)
A 50,000-row CSV uploads and charts in about 3.1 s (average of 3 runs), using batched JDBC inserts (batch size 1,000, `rewriteBatchedStatements=true`).
![Dashboard screenshot](docs/dashboard.png)


## Key finding
Upload a CSV and get descriptive statistics and charts back. The app saves rows with batched JDBC inserts (batch size 1,000, `rewriteBatchedStatements=true`), and a built-in timing panel shows where the time goes. A 50,000-row file uploads and charts in about 3.1 s (average of 3 runs).

## Features
- CSV upload and storage in MySQL via Spring Data JPA
- Histogram endpoint and Chart.js charts
- Timing panel measuring upload-to-chart time

## Tech stack
Java 21, Spring Boot (Spring Web, Spring Data JPA), Maven, MySQL, Chart.js

## Performance results

Measured locally on an Intel Core i5, 16 GB RAM, with MySQL running on a 512 GB SSD. Each time is the average of 3 runs.

| Rows   | Upload + save (s) | Load + draw charts (s) | Total (s) |
|--------|-------------------|------------------------|-----------|
| 1,000  | 0.12              | 0.09                   | 0.21      |
| 10,000 | 0.41              | 0.29                   | 0.70      |
| 50,000 | 1.98              | 1.16                   | 3.14      |

## How it works
1. CSV is uploaded through the dashboard
2. Rows are saved to MySQL through JPA
3. Statistics are computed and sent to Chart.js
4. The timing panel records each stage

## Run locally
1. Install JDK 21, Maven, and MySQL
2. Create a database: `CREATE DATABASE [your_db_name];`
3. Copy `application.properties.example` to `application.properties` and
   set your own MySQL URL, username, and password
4. `mvn spring-boot:run`
5. Open `http://localhost:8080/chart.html`

## Limitations
- Timings come from one local machine, not a controlled benchmark

## Roadmap
- [ ] Batch inserts (the proposed fix for the 94% bottleneck)
- [ ] Box plot and scatter plot
- [ ] More statistics: skewness, confidence interval, correlation
- [ ] CSV export
