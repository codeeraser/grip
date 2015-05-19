package de.metacode.grip

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

def binding = new Binding()

def home = System.getProperty("user.home")
loadInitScript(new File("""$home/.grip/init.grip"""), log, binding)

def scriptDir = new File('../../../../../test/groovy/de/metacode/grip')
loadInitScript(new File(scriptDir.absolutePath, "init.grip"), log, binding)

Quartz.instance.start()
new SocketActor().start()

scriptDir.eachFileMatch(FileType.FILES, ~/.*grip$/) { File file ->
    if (!file.name.endsWith("init.grip")) {
        JobProcessor.run(file, binding)
    }
}


def loadInitScript(File init, Logger log, Binding binding) {
    if (init.exists()) {
        log.info("Loading $init.absoluteFile")
        InitProcessor.run(init, binding)
    }
}