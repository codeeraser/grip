Groovy Information Processing
======

The goal of *Grip* is to support you handling data and datasources you frequently need to work with.
There are at least two ways to work with *Grip*. You can execute scripts via REST interface immediatly or you let your scripts be scheduled.
So you can query your database really fast (getting the output as text, xls or csv) and schedule jobs, e.g. to report something once a month.

When you process some data you have to do four steps typically:

1. Access the datasource and fetch data
2. Do something with it (formatting, calculations, ...)
3. Render/Process your results (to readable text, xls, ...)
4. Write the results into a local/remote file/stream or send it per mail

With the exception of the second step, this often turns out to be a very repetitive work.
*Grip* helps you to build reusable code by introducing the concepts of *Envs* (enables easy access to a data source) and *Renderers* (writing data). The *Renderers* result can be saved in a local file, sent via scp to a remote host or via e-mail.
Additionally, *Grip* comes with Quartz support to schedule your scripts. When using the REST interface, you can get the result immediately in the response.

To cut a long story short, this is a simple grip-script reading data from a database and writing the data to a csv-file:

```
schedule 'MysqlExample', 'now'

init {
    grab group: 'mysql', module: 'mysql-connector-java', version: '5.1.6'
    env "mysql", sql(
            url: "jdbc:mysql://localhost/sampledb",
            driver: "com.mysql.jdbc.Driver",
            user: "testuser",
            pwd: "testpwd")
}

def csv = newCsvWith(separator: ';')
mysql.query "select name, email from users", { rs -> csv.writeAll rs, false}
csv.toFile "~/tmp/test.csv"
```
This script is named *MysqlExample* and is scheduled *now* (which means just running once when *Grip* came up).
The `init` part sets up the *Env* representing a mysql database.
You use it by just calling the name you defined (`mysql.query ...`).
`newCsv` instantiates a Csv *Renderer* giving you a com.opencsv.CSVWriter to work with.
You actually define *Envs* in your home-dir (~/.grip/init.grip), so this script contains 3 lines of code in the end.

Structure of a *Grip* script
------
A *Grip* script contains up to three parts.

1. The regular script
2. The init part, which is code `init {}` contains
3. The scheduler part, which is the hook to setup a quartz job executing yout script

When you write the script you don't need to stick to an order.
If there's a `schedule` somewhere in your script, *Grip* creates a quartz job.
If there's an `init{}` somewhere in your script, its contained code will be executed first. Before executing any script, *Grip* is looking for *~/.grip/init.grip* to be executed first. You might have a couple of *Envs* you work with during the day. With this shortcut, you don't need to write the ```init{}``` block over and over again.

Env
------
Only one *Env* exists today (SqlEnv) to access databases via JDBC.

Renderer
------
3 *Renderer* are available:
* Csv (OpenCsv)
* SimpleExcel (POI)
* SysOut (Plain Text)

Filehandling
------
Every *Renderer* provides three methods to work with its results
 * *toFile* to write a local file
 * *toRemoteFile* to send file with scp
 * *sendMail* to send file via e-mail
 
####Some Examples
 ...TODO...


Scheduler
------
Every script can be scheduled using the `schedule` command and a cron-like definition.
To do so, put the `schedule` command somewhere in your script. E.g. to execute a script every 30 seconds:

```schedule "MyAmazingGripScript", "0/30 * * * * ?"```

Read more about the cron format: http://quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06

REST interface
------
Two resources are available
* Show scheduled scripts ```GET /jobs```
* Execute a script ```POST /exec```

####Show all known jobs (scheduled scripts)

The Joblist is plain text (should be json at some time), e.g.
```
[jobName] : SlowPingScript [groupName] : SlowPingScript - Sun Feb 05 18:56:40 CET 2017
[jobName] : PingScript [groupName] : PingScript - Sun Feb 05 18:56:36 CET 2017
```

####Running some code
Here's an example how to execute a script with curl:

```curl --data-binary "@MyGripScript.grip" localhost:5050/exec```


If you want to execute a script and getting back a reponse, you need to call the *response* methode.
Here are some examples how to execute code (with curl):

####Query your db from console
To get a very short script, you need to init your *Env* with *~/.grip/init.grip*.
If this file exists you can query your DB like this (*Env* name is *hsql*):
 
```curl -data="response newSysOut().write(hsql.executeQuery("select * from atable")).toDataSource()" localhost:5050/exec```

This is nice. But how about this:

```sout select name, address from atable```

Cool, isn't it? *sout* is just a shell-script encapsulating the code.
You can do the same with generating Excel- or Csv-Files. Find the shell scripts in *src/main/resources*.

Deploying *Grip*
------
The easiest way to deploy *Grip* is to build a shadowJar using gradle.
After cloning the repo, just execute ```gradle shadowJar``` in the grip dir. You'll find the all-jar in build/libs.

Now you can run *Grip* like this:

```java -Dworkdir="/user/home/gripscripts" -Dlog.dir="/user/home/logdir" -jar grip-0.1-all.jar```

The *workdir* parameter is mandatory. *Grip* searches this dir for *.grip files to schedule them.