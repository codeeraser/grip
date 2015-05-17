package de.metacode.grip.env

import groovy.sql.Sql

/**
 * Created by mloesch on 14.03.15.
 */
class SqlEnv implements Env {
    String url
    String driver
    String user
    String pwd

    @Override
    def createEnv() {
        return Sql.newInstance(url, user, pwd, driver)
    }
}