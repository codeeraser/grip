Groovy Information Processing
======

The goal of *Grip* is to support you handling data and datasources you frequently need to work with.

When you process some data you have to do three steps typically:

1. Access the datasource and fetch data
2. Do something with it (formatting, calculations, ...)
3. Render/Process your results (to readable text, SQL, CSV, ...)

The first and the third step often turns out to be a very repetitive work.
*Grip* helps you to build reusable code by introducing the concepts of *Envs* (reading data) and *Renderers* (writing data).
Additionally, *Grip* comes with Quartz support to schedule your scripts.

To cut a long story short, this is a simple grip-script reading data from a database and writing the data to a csv-file:

```
init {
    grab group: 'mysql', module: 'mysql-connector-java', version: '5.1.6'
    env "mysql", sql(
            url: "jdbc:mysql://localhost/sampledb",
            driver: "com.mysql.jdbc.Driver",
            user: "testuser",
            pwd: "testpwd")
}

def csv = newCsv ','
mysql.query "select name, email from users", { rs -> csv.writer.writeAll rs, false}
csv.toFile "~/tmp/test.csv"
```

The `init` part sets up the *Env* representing a mysql database.
You use it by just calling the name you defined (`mysql.query ...`).
`newCsv` instantiates a Csv *Renderer* giving you a com.opencsv.CSVWriter to work with.
You actually define *Envs* in your home-dir (~/.grip/init.grip), so this script contains 3 lines of code in the end.

In this early state, 3 *Renderer* are available:
* Csv (OpenCsv)
* SimpleExcel (POI)
* SysOut (Console)

Only one *Env* exists with SqlEnv to access Databases via JDBC.