package de.metacode.grip

import de.metacode.grip.core.Bootstrap
import de.metacode.grip.core.CoreProcessor
import de.metacode.grip.core.ast.MoveToTopCustomizer
import de.metacode.grip.core.ast.RemoveCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.slf4j.LoggerFactory

/**
 * Created by mloesch on 02.03.15.
 */

@Grapes([
        @Grab(group = 'commons-cli', module = 'commons-cli', version = '1.2'),
        @Grab('org.reflections:reflections:0.9.9-RC1'),
        @Grab(group = 'org.eclipse.jgit', module = 'org.eclipse.jgit', version = '3.7.0.201502260915-r'),
        @Grab(group = 'org.slf4j', module = 'slf4j-api', version = '1.7.2'),
        @Grab(group = 'org.slf4j', module = 'slf4j-simple', version = '1.7.2'),
        @GrabExclude(group = 'xml-apis', module = 'xml-apis'),
//plugin stuff
        @Grab(group = 'hsqldb', module = 'hsqldb', version = '1.8.0.10'),
        @Grab(group='javax.mail', module='javax.mail-api', version='1.5.1'),
        @Grab(group = 'org.apache.poi', module = 'poi', version = '3.11'),
        @Grab(group='org.quartz-scheduler', module='quartz', version='2.2.1')
])

def log = LoggerFactory.getLogger(Grip.class)

def bs = new Bootstrap()
bs.run()

/// Cli /////////////////////////////////////////////////////////////////////////////////////////////
def cli = new CliBuilder(usage: 'groovy Grip.groovy -f[h] script')
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
def importCustomizer = new ImportCustomizer()
importCustomizer.addStarImports("de.metacode.grip.env")
cc.addCompilationCustomizers new MoveToTopCustomizer("env")
cc.addCompilationCustomizers new RemoveCustomizer("schedule")

//cc.addCompilationCustomizers(importCustomizer)
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