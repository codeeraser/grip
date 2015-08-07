package de.metacode.grip.env

import groovy.sql.Sql
import groovy.util.logging.Slf4j

/**
 * Created by mloesch on 14.03.15.
 */

@Slf4j
class SqlEnv implements Env {
    String url
    String driver
    String user
    String pwd
    boolean autocommit

    private Sql cache = null;
    private final Object mutex = new Object()

    @Override
    def Sql createEnv() {
        synchronized (this.mutex) {
            if (cache != null && !cache.connection.closed) {
                log.trace("return cached Sql instance")
                return cache
            }
            cache = Sql.newInstance(url, user, pwd, driver)
            if (autocommit) {
                log.debug("activate autocommit")
                cache.connection.autoCommit = true
            }
            return cache
        }
    }

    @Override
    def destroy() {
        synchronized (this.mutex) {
            if (cache != null) {
                log.debug("closing connection")
                cache.close()
            }
        }
    }
}