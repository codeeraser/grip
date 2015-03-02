package de.metacode.grip

import de.metacode.grip.core.Bootstrap
import groovy.sql.Sql

/**
 * Created by mloesch on 02.03.15.
 */

@Grapes([
        @Grab(group = 'commons-cli', module = 'commons-cli', version = '1.2'),
        @Grab('org.reflections:reflections:0.9.9-RC1'),
        @Grab(group = 'org.slf4j', module = 'slf4j-api', version = '1.7.5'),
        @Grab(group = 'org.eclipse.jgit', module = 'org.eclipse.jgit', version = '3.7.0.201502260915-r'),
//        @Grab(group = 'ch.qos.logback', module = 'logback-core', version = '1.0.13'),
//        @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.0.13')
])

def bs = new Bootstrap()
bs.run()

def sqlInst = Sql.newInstance("jdbc:mysql://localhost/gwt", "root", "", "com.mysql.jdbc.Driver")
sqlInst.eachRow("select * from users", { row ->
    println row.email
})