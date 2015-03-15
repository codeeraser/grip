package de.metacode.grip.env

/**
 * Created by mloesch on 14.03.15.
 */
class Sql implements Env {
    String url
    String driver
    String user
    String pwd

    @Override
    def createEnv() {
        return groovy.sql.Sql.newInstance(url, user, pwd, driver)
    }
}
