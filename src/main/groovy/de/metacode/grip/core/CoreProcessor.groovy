package de.metacode.grip.core

import de.metacode.grip.core.ast.MoveToTopCustomizer
import de.metacode.grip.core.ast.RemoveCustomizer
import de.metacode.grip.env.Env
import groovy.util.logging.Slf4j
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Created by mloesch on 14.03.15.
 */

@Slf4j
class CoreProcessor extends InitProcessor {

    CoreProcessor(binding) {
        super(binding)
    }

    def methodMissing(String name, args) {
        log.info("methodMissing calls for $name")
        log.info(this.binding.properties.toMapString())
        Map envs = this.binding.getProperty(ENV) as Map<String, Env>
        if (envs.containsKey(name)) {
            Env env = envs.get(name)
            if (!args || args.length == 0 || (!(args[0] instanceof Closure))) {
                throw new IllegalArgumentException("$name needs a closure as argument")
            }
            Closure c = args[0] as Closure
            c(env.createEnv())
        }
    }

    def propertyMissing(String name) {
        Map envs = this.binding.getProperty(ENV) as Map<String, Env>
        if (envs.containsKey(name)) {
            Env env = envs.get(name)
            return env.createEnv()
        }
    }

    static void run(File gripScript, Binding binding) {
        run(gripScript.text, binding)
    }

    static void run(String gripScript, Binding binding) {
        def cc = new CompilerConfiguration()
        cc.addCompilationCustomizers new MoveToTopCustomizer("init")
        cc.addCompilationCustomizers new RemoveCustomizer("schedule")
        cc.scriptBaseClass = DelegatingScript.class.name
        def sh = new GroovyShell(cc)

        def core = new CoreProcessor(binding)

        def grip = sh.parse(gripScript)
        grip.setDelegate(core)
        grip.run()
    }

}