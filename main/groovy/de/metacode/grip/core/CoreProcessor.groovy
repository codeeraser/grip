package de.metacode.grip.core

import de.metacode.grip.env.Env

/**
 * Created by mloesch on 14.03.15.
 */
class CoreProcessor {

    Binding binding

    def methodMissing(String name, args) {
        if (name.startsWith("use")) {
            Map envs = this.binding.getProperty("env") as Map<String, Env>
            def envName = name.substring(3).toLowerCase()
            if (!envs.containsKey(envName)) {
                throw new IllegalStateException("no env with name $envName available!")
            }
            Env env = envs.get(envName)
            if (!args || args.length == 0 || (!(args[0] instanceof Closure))) {
                throw new IllegalArgumentException("$name needs a closure as argument")
            }
            Closure c = args[0] as Closure
            c(env.createEnv())
        }
    }
}
