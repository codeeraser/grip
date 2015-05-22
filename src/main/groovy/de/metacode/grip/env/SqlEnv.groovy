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
    boolean autocommit

    Sql cache = null;

    @Override
    def createEnv() {
        if (cache != null && !cache.connection.isClosed()) {
            return cache;
        }
        def sql = Sql.newInstance(url, user, pwd, driver)
        if (autocommit) {
            sql.connection.setAutoCommit()
        }
        cache = sql
        return sql
    }
}