package de.metacode.grip.core

import de.metacode.grip.core.ast.MoveToTopCustomizer
import de.metacode.grip.core.ast.RemoveCustomizer
import de.metacode.grip.env.Env
import de.metacode.grip.renderer.excel.SimpleExcel
import de.metacode.grip.util.AttachmentProvider
import de.metacode.grip.util.Mail
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.LoggerFactory

/**
 * Created by mloesch on 14.03.15.
 */

class CoreProcessor extends InitProcessor {

    def log

    CoreProcessor(Map context) {
        super(context)
        this.log = LoggerFactory.getLogger(context.get('name'))
    }

    def methodMissing(String name, args) {
        log.info("methodMissing calls for $name")
        log.info(this.context.toMapString())
        Map envs = this.context.get(ENV) as Map<String, Env>
        if (!envs.containsKey(name)) {
            return;
        }
        if (this.context.containsKey(name)) {
            c(this.context.get(name))
            return;
        }
        Env env = envs.get(name)
        if (!args || args.length == 0 || (!(args[0] instanceof Closure))) {
            throw new IllegalArgumentException("$name needs a closure as argument")
        }
        Closure c = args[0] as Closure
        c(env.createEnv())
    }

    def propertyMissing(String name) {
        Map envs = this.context.get(ENV) as Map<String, Env>
        if (envs.containsKey(name)) {
            Env env = envs.get(name)
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