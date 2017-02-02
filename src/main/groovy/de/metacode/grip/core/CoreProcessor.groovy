package de.metacode.grip.core

import de.metacode.grip.core.ast.MoveToTopCustomizer
import de.metacode.grip.core.ast.RemoveCustomizer
import de.metacode.grip.env.Env
import de.metacode.grip.renderer.Instantiable
import org.codehaus.groovy.control.CompilerConfiguration
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.activation.DataSource

/**
 * Created by mloesch on 14.03.15.
 */

class CoreProcessor extends InitProcessor {
    final @Delegate
    Logger log
    private final Binding binding

    static {
        initInstantiables()
    }

    CoreProcessor(Map context, Binding binding) {
        super(context)
        this.binding = binding
        this.log = LoggerFactory.getLogger(context.name as String)
    }

    static void initInstantiables() {
        def newInstanceWithArgs = { Class clazz, Map args -> clazz.newInstance(args) }
        def newInstance = { Class clazz -> clazz.newInstance() }

        def classLoaders = [ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader()]
        new Reflections(
                new ConfigurationBuilder()
                        .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                        .setUrls(ClasspathHelper.forClassLoader(classLoaders as ClassLoader[]))
                        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("de.metacode")))
        ).getSubTypesOf(Instantiable.class).each { Class clazz ->
            CoreProcessor.metaClass."new${clazz.simpleName}With" = newInstanceWithArgs.curry(clazz)
            //noinspection GroovyResultOfAssignmentUsed
            CoreProcessor.metaClass."new${clazz.simpleName}" = newInstance.curry(clazz)
        }
    }

    def propertyMissing(String name) {
        if (envs.containsKey(name)) {
            Env env = envs[name]
            return env.createEnv()
        }
        this.binding[name]
    }

    def response(String text) {
        this.context['responseText'] = text
    }

    def response(DataSource ds) {
        this.context['responseDataSource'] = ds
    }

    static void run(String gripScript, Map context) {
        def cc = new CompilerConfiguration()
        cc.addCompilationCustomizers new MoveToTopCustomizer("init")
        cc.addCompilationCustomizers new RemoveCustomizer("schedule")
        cc.scriptBaseClass = DelegatingScript.class.name

        def binding = new Binding()
        def sh = new GroovyShell(binding, cc)
        def core = new CoreProcessor(context, binding)

        def grip = sh.parse(gripScript)
        grip.setDelegate(core)
        grip.run()

        getEnvs(context)?.values()*.destroy()
    }

    private Map<String, Env> getEnvs() {
        getEnvs(this.context) ?: Collections.emptyMap()
    }

    private static Map<String, Env> getEnvs(Map context) {
        context[ENV] as Map<String, Env>
    }
}