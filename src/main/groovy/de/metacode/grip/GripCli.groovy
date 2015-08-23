package de.metacode.grip

import de.metacode.grip.core.CoreProcessor
import de.metacode.grip.core.InitProcessor
import org.slf4j.LoggerFactory

/**
 * Created by mloesch on 02.03.15.
 */

def log = LoggerFactory.getLogger(GripCli.class)

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

def context = [name: 'gripCli']
/// init script /////////////////////////////////////////////////////////////////////////////////////
def home = System.getProperty("user.home")
def initScript = new File("""$home/.grip/init.grip""")
if (initScript.exists()) {
    InitProcessor.run(initScript, context)
}

/// grip script /////////////////////////////////////////////////////////////////////////////////////
def scriptFile = new File(opt.f as String)
def script = scriptFile.text
if (!opt.arguments().isEmpty()) {
    def input = opt.arguments().join(' ')
    script += """\n$input"""
}
CoreProcessor.run(script, context)