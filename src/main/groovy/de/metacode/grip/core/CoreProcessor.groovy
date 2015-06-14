package de.metacode.grip.core

import de.metacode.grip.core.ast.MoveToTopCustomizer
import de.metacode.grip.core.ast.RemoveCustomizer
import de.metacode.grip.env.Env
import de.metacode.grip.renderer.excel.SimpleExcel
import de.metacode.grip.util.AttachmentProvider
import de.metacode.grip.util.Mail
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

/**
 * Created by mloesch on 14.03.15.
 */

class CoreProcessor extends InitProcessor {

    Logger log

    CoreProcessor(Map context) {
        super(context)
        this.log = LoggerFactory.getLogger(context['name'] as String)
    }

    String getName() {
        this.context['name']
    }

    //TODO move logger methods to a delegate ///////////////
    def info(String text) {
        MDC.put("loggerFileName", name)
        this.log.info(text)
        MDC.remove("loggerFileName")
    }

    def debug(String text) {
        MDC.put("loggerFileName", name)
        this.log.debug(text)
        MDC.remove("loggerFileName")
    }

    def warn(String text) {
        MDC.put("loggerFileName", name)
        this.log.warn(text)
        MDC.remove("loggerFileName")
    }

    def trace(String text) {
        MDC.put("loggerFileName", name)
        this.log.trace(text)
        MDC.remove("loggerFileName")
    }
    //TODO end /////////////////////////////////////////////

    def methodMissing(String name, args) {
        log.info("methodMissing calls for $name")
        log.info(this.context.toMapString())
        Map envs = this.context[ENV] as Map<String, Env>
        if (!envs.containsKey(name)) {
            return;
        }
        if (this.context.containsKey(name)) {
            c(this.context[name])
            return;
        }
        Env env = envs[name]
        if (!args || args.length == 0 || (!(args[0] instanceof Closure))) {
            throw new IllegalArgumentException("$name needs a closure as argument")
        }
        Closure c = args[0] as Closure
        c(env.createEnv())
    }

    def propertyMissing(String name) {
        Map envs = this.context[ENV] as Map<String, Env>
        if (envs.containsKey(name)) {
            Env env = envs[name]
            return env.createEnv()
        }
    }

    def newSimpleXls() {
        new SimpleExcel()
    }

    def sendmail(params, AttachmentProvider attachmentProvider, String filename) {
        Mail.send(params, attachmentProvider, filename)
    }

    static void run(File gripScript, Map context) {
        run(gripScript.text, context)
    }

    static void run(String gripScript, Map context) {
        def cc = new CompilerConfiguration()
        cc.addCompilationCustomizers new MoveToTopCustomizer("init")
        cc.addCompilationCustomizers new RemoveCustomizer("schedule")
        cc.scriptBaseClass = DelegatingScript.class.name
        def sh = new GroovyShell(cc)

        def core = new CoreProcessor(context)

        def grip = sh.parse(gripScript)
        grip.setDelegate(core)
        grip.run()
    }
}