package de.metacode.grip

import de.metacode.grip.core.Bootstrap
import de.metacode.grip.core.CoreProcessor
import de.metacode.grip.core.ast.MoveToTopCustomizer
import de.metacode.grip.core.ast.RemoveCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.LoggerFactory

/**
 * Created by mloesch on 02.03.15.
 */

def log = LoggerFactory.getLogger(GripCli.class)

def bs = new Bootstrap()
bs.run()

/// Cli /////////////////////////////////////////////////////////////////////////////////////////////
def cli = new CliBuilder(usage: 'groovy GripCli.groovyovy -f[h] script')
cli.f(longOpt: 'file', 'file that contains a grip script', type: String, args: 1, required: true)
cli.h(longOpt: 'help', 'usage information', required: false)

def opt = cli.parse(args)
if (!opt) {
    return
}
if (opt.h || !opt.getInner().getOptions()) {
    cli.usage()
    return
}

/// compiler configuration //////////////////////////////////////////////////////////////////////////
def cc = new CompilerConfiguration()
cc.scriptBaseClass = DelegatingScript.class.name
cc.addCompilationCustomizers new MoveToTopCustomizer("env")
cc.addCompilationCustomizers new RemoveCustomizer("schedule")
def sh = new GroovyShell(cc)

//This is how create-methods for plugin environments could be injected
/*
CoreProcessor.metaClass.createSql = { Closure c ->
    def sql = new SqlEnv()
    c.delegate = sql
    c.resolveStrategy = Closure.DELEGATE_ONLY
    c()
    sql
}
*/

def core = new CoreProcessor(this.binding)


log.info(this.binding.properties.toMapString())
/// init script /////////////////////////////////////////////////////////////////////////////////////
def home = System.getProperty("user.home")
def initScript = new File("""$home/.grip/env.grip""")
if (initScript.exists()) {
    log.info("RUN ENV: \n$initScript.text ")
    def init = sh.parse(initScript.text)
    init.setDelegate(core)
    init.run()
}

log.info(this.binding.properties.toMapString())

/// grip script /////////////////////////////////////////////////////////////////////////////////////
def scriptFile = new File(opt.f as String)
def script = scriptFile.text
if (!opt.arguments().isEmpty()) {
    def input = opt.arguments().join(' ')
    script += """\n$input"""
}
def grip = sh.parse(script)
grip.setDelegate(core)
grip.run() //run with CoreProcessor to do the actual work

log.info(this.binding.properties.toMapString())