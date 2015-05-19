package de.metacode.grip.core

import de.metacode.grip.core.ast.HighlanderCustomizer
import de.metacode.grip.env.Env
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by mloesch on 19.05.15.
 */

@Slf4j
class InitProcessor {
    static final String ENV = "env"

    def binding;

    InitProcessor(binding) {
        this.binding = binding
    }

    def env(Closure c) {
        c()
    }

    def add(String name, Env env) {
        log.info("init env $name")
        if (!this.binding.hasProperty(ENV)) {
            this.binding.setProperty(ENV, [:])
        }
        Map envs = this.binding.getProperty(ENV) as Map
        envs.put(name.toLowerCase(), env)
    }

    static void run(File gripScript, Binding binding) {
        def cc = new CompilerConfiguration()
        cc.addCompilationCustomizers new HighlanderCustomizer("env")
        cc.scriptBaseClass = DelegatingScript.class.name
        def sh = new GroovyShell(cc)

        def script = gripScript.text
        def env = new InitProcessor(binding)

        def grip = sh.parse(script)
        grip.setDelegate(env)
        grip.run()
    }


}
