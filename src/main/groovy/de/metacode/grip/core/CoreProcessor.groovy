package de.metacode.grip.core

import de.metacode.grip.core.ast.MoveToTopCustomizer
import de.metacode.grip.core.ast.RemoveCustomizer
import de.metacode.grip.env.Env
import de.metacode.grip.renderer.Csv
import de.metacode.grip.renderer.SimpleExcel
import de.metacode.grip.renderer.SysOut
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by mloesch on 14.03.15.
 */

class CoreProcessor extends InitProcessor {
    final @Delegate Logger log
    private final Binding binding

    CoreProcessor(Map context, Binding binding) {
        super(context)
        this.binding = binding
        this.log = LoggerFactory.getLogger(context['name'] as String)
    }

    def methodMissing(String name, args) {
        if (!envs.containsKey(name)) {
            this.log.info("methodMissing call for $name")
            return;
        }
        if (this.context.containsKey(name)) {
            c(this.context[name])
        } else {
            Env env = envs[name]
            if (!args || args.length == 0 || (!(args[0] instanceof Closure))) {
                throw new IllegalArgumentException("$name needs a closure as argument")
            }
            Closure c = args[0] as Closure
            c(env.createEnv())
        }
    }

    def propertyMissing(String name) {
        if (envs.containsKey(name)) {
            Env env = envs[name]
            return env.createEnv()
        }
        this.binding[name]
    }

    static SimpleExcel newSimpleXls() {
        new SimpleExcel()
    }

    static Csv newCsv(String separator = ',') {
        new Csv(separator)
    }

    static SysOut newSysOut() {
        return new SysOut()
    }

    static void run(String gripScript, Map context) {
        def cc = new CompilerConfiguration()
        cc.addCompilationCustomizers new MoveToTopCustomizer("init")
        cc.addCompilationCustomizers new RemoveCustomizer("schedule")
        cc.scriptBaseClass = DelegatingScript.class.name

        def binding = new Binding()
        def sh = new GroovyShell(binding, cc)
        def core = new CoreProcessor(context, binding)

        def grip = sh.parse(gripScript)
        grip.setDelegate(core)
        grip.run()

        getEnvs(context)?.values()*.destroy()
    }

    private Map<String, Env> getEnvs() {
        getEnvs(this.context)?: Collections.emptyMap()
    }

    private static Map<String, Env> getEnvs(Map context) {
        context[ENV] as Map<String, Env>
    }
}