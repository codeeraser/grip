package de.metacode.grip

import de.metacode.grip.core.JobProcessor
import de.metacode.grip.core.Quartz
import de.metacode.grip.core.SocketActor
import de.metacode.grip.core.ast.HighlanderCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.hsqldb.Server

/**
 * Created by mloesch on 06.05.15.
 */

/// hsql server /////////////////////////////////////////////////////////////////////////////////////
println "starting server"
Server.main()
println "after starting server"

/// quartz scheduler ////////////////////////////////////////////////////////////////////////////////
Quartz.instance.start()
//// interpred job //////////////////////////////////////////////////////////////////////////////////
/// compiler configuration ////////////////////////////////////////////////////////////////////////////
def cc = new CompilerConfiguration()
cc.addCompilationCustomizers new HighlanderCustomizer("schedule")
cc.scriptBaseClass = DelegatingScript.class.name
def sh = new GroovyShell(cc)

def binding = new Binding()
//println new File('./../../../../').absolutePath


def scriptFile = new File('../../../../../test/de/metacode/grip/hsqlTest.grip')
def script = scriptFile.text
def job = new JobProcessor(binding, script)

/// grip script /////////////////////////////////////////////////////////////////////////////////////

def grip = sh.parse(script)
grip.setDelegate(job)
grip.run() //run with JobProcessor to config the job


new SocketActor().start()

