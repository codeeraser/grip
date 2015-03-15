package de.metacode.grip

import de.metacode.grip.core.Bootstrap
import de.metacode.grip.core.CoreProcessor
import de.metacode.grip.env.EnvProcessor
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * Created by mloesch on 02.03.15.
 */

@Grapes([
        @Grab(group = 'commons-cli', module = 'commons-cli', version = '1.2'),
        @Grab('org.reflections:reflections:0.9.9-RC1'),
        @Grab(group = 'org.slf4j', module = 'slf4j-api', version = '1.7.5'),
        @Grab(group = 'org.eclipse.jgit', module = 'org.eclipse.jgit', version = '3.7.0.201502260915-r'),
        @Grab(group = 'javax.mail', module = 'mail', version = '1.4'),
        @Grab(group = 'org.apache.poi', module = 'poi', version = '3.11')
//        @Grab(group = 'ch.qos.logback', module = 'logback-core', version = '1.0.13'),
//        @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.0.13')
])

def bs = new Bootstrap()
bs.run()

/// Cli /////////////////////////////////////////////////////////////////////////////////////////////
def cli = new CliBuilder(usage: 'groovy Grip.groovy -f[h] script')
cli.f(longOpt: 'file', 'file that contains a grip script', type: String, args: 1, required: true)
cli.h(longOpt: 'help', 'usage information', required: false)

OptionAccessor opt = cli.parse(args)
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
cc.addCompilationCustomizers(importCustomizer)
def sh = new GroovyShell(cc)

/// init script /////////////////////////////////////////////////////////////////////////////////////
def env = new EnvProcessor(binding)
def home = System.getProperty("user.home")
def initScript = new File("""$home/.grip/env.grip""")
if (initScript.exists()) {
    def init = sh.parse(initScript)
    init.setDelegate(env)
    init.run()
}

/// grip script /////////////////////////////////////////////////////////////////////////////////////
def scriptFile = new File(opt.f as String)
def script = scriptFile.text
if (!opt.arguments().isEmpty()) {
    def input = opt.arguments().join(' ')
    script += """\n$input"""
}
def grip = sh.parse(script)
def core = new CoreProcessor(binding)
grip.setDelegate(core)
grip.run() //run with CoreProcessor to do the actual work