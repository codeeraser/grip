package de.metacode.grip.core.classloaderhack

import org.reflections.util.ClasspathHelper

/**
 * Created by mloesch on 03.02.17.
 */

@Singleton(strict = false)
class TransferClassLoader extends GroovyClassLoader {

    static {
        ReflectionsHelper.registerUrlTypes()
    }

    def transferJarsToSystemloader() {
        def urls = ClasspathHelper.forClassLoader(this).toArray() as URL[]
        def cph = new ClassPathHacker()
        urls.each { URL url ->
            if (url.file.endsWith('jar')) {
                cph.addFile(url.file)
            }
        }
    }
}