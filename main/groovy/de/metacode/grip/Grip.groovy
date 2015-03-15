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
        @Grab(group='javax.mail', module='mail', version='1.4'),
        @Grab(group='org.apache.poi', module='poi', version='3.11')
//        @Grab(group = 'ch.qos.logback', module = 'logback-core', version = '1.0.13'),
//        @Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.0.13')
])

def bs = new Bootstrap()
bs.run()

// configure the base script class
def cc = new CompilerConfiguration()
cc.scriptBaseClass = DelegatingScript.class.name
def importCustomizer = new ImportCustomizer()
importCustomizer.addStarImports("de.metacode.grip.env")
cc.addCompilationCustomizers(importCustomizer)

def script = new File("../scripts/test.grip").text

def sh = new GroovyShell(cc)
def s = sh.parse(script)

//configuration
def env = new EnvProcessor(binding: binding)
s.setDelegate(env)
s.run()

//run again with CoreDSL
def core = new CoreProcessor(binding: binding)
s.setDelegate(core)
s.run()