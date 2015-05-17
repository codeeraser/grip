package de.metacode.grip.core

import de.metacode.grip.env.Env
import de.metacode.grip.env.SqlEnv
import groovy.util.logging.Slf4j

/**
 * Created by mloesch on 14.03.15.
 */
@Slf4j
class CoreProcessor {
    static final String ENV = "env"
    final Binding binding

    CoreProcessor(Binding binding) {
        this.binding = binding
    }

    def createSql = { Closure c ->
        def sql = new SqlEnv()
        c.delegate = sql
        c.resolveStrategy = DELEGATE_ONLY
        c()
        sql
    }

    def env(String name, Env env) {
        log.info("HEY HEY $name!")
        if (!this.binding.hasProperty(ENV)) {
            this.binding.setProperty(ENV, [:])
            log.info("setting property env")
        }
        Map envs = this.binding.getProperty(ENV) as Map
        envs.put(name.toLowerCase(), env)
    }

    def methodMissing(String name, args) {
        log.info("methodMissing calls for $name")
        if (name.startsWith("use")) {
            log.info(this.binding.properties.toMapString())
            Map envs = this.binding.getProperty(ENV) as Map<String, Env>
            def envName = name.substring(3).toLowerCase()
            if (!envs.containsKey(envName)) {
                throw new IllegalStateException("no env with name $envName available!")
            }
            Env env = envs.get(envName)
            if (!args || args.length == 0 || (!(args[0] instanceof Closure))) {
                throw new IllegalArgumentException("$name needs a closure as argument")
            }
            Closure c = args[0] as Closure
            c(env.createEnv())
        }
    }
}