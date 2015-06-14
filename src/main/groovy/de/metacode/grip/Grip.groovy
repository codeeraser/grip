package de.metacode.grip

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import de.metacode.grip.core.InitProcessor
import de.metacode.grip.core.JobProcessor
import de.metacode.grip.core.Quartz
import de.metacode.grip.core.SocketActor
import groovy.io.FileType
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by mloesch on 06.05.15.
 */

def log = LoggerFactory.getLogger(Grip.class)

// assume SLF4J is bound to logback in the current environment
LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
JoranConfigurator configurator = new JoranConfigurator();
configurator.setContext(lc);
StatusPrinter.print(lc);

def context = [:]

def home = System.getProperty("user.home")
loadInitScript(new File("""$home/.grip/init.grip"""), log, context)

def workdir = null

def props = new File("""$home/.grip/grip.properties""")
if (props.exists()) {
    def config = new ConfigSlurper().parse(props.toURI().toURL())
    if (config.containsKey("workdir")) {
        workdir = config.workdir
    }
}
if (System.getProperty("workdir") != null) {
    workdir = System.getProperty("workdir")
}
if (!workdir) {
    throw new IllegalStateException("no workdir specified!")
}

def scriptDir = new File(workdir)
loadInitScript(new File(scriptDir.absolutePath, "init.grip"), log, context)

Quartz.instance.start()
new SocketActor().start()

scriptDir.eachFileMatch(FileType.FILES, ~/.*grip$/) { File file ->
    if (!file.name.endsWith("init.grip")) {
        JobProcessor.run(file, context)
    }
}

def loadInitScript(File init, Logger log, Map context) {
    if (init.exists()) {
        log.info("Processing init script $init.absoluteFile")
        InitProcessor.run(init, context)
    }
}