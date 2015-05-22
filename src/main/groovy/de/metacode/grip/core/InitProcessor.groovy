package de.metacode.grip.core

import de.metacode.grip.core.ast.HighlanderCustomizer
import de.metacode.grip.env.Env
import de.metacode.grip.env.SqlEnv
import groovy.grape.Grape
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.LoggerFactory

/**
 * Created by mloesch on 19.05.15.
 */

@Slf4j
class InitProcessor {
    static final String ENV = "env"

    Map context

    InitProcessor(Map context) {
        this.context = context
    }

    static def sql(Map map) {
        new SqlEnv(map)
    }


    static def init(Closure c) {
        c()
    }

    def env(String name, Env env) {
        log.info("init env $name")
        if (!this.context.containsKey(ENV)) {
            this.context.put(ENV, [:])
        }
        Map envs = this.context.get(ENV) as Map
        envs.put(name.toLowerCase(), env)
    }

    def grab(Map dependencies) {
        Grape.grab(classLoader: this.class.classLoader.rootLoader, dependencies)
    }

    static void run(File gripScript, Map context) {
        def cc = new CompilerConfiguration()
        cc.addCompilationCustomizers new HighlanderCustomizer("init")
        cc.scriptBaseClass = DelegatingScript.class.name
        def sh = new GroovyShell(cc)

        def script = gripScript.text
        def env = new InitProcessor(context)

        def grip = sh.parse(script)
        grip.setDelegate(env)
        grip.run()
    }
}