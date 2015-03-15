package de.metacode.grip.env

/**
 * Created by mloesch on 14.03.15.
 */
class EnvProcessor {

    Binding binding

    EnvProcessor(Binding binding) {
        this.binding = binding
    }

    void env(String name, Env env) {
        if (!this.binding.hasProperty("env")) {
            this.binding.setProperty("env", [:])
        }
        Map envs = this.binding.getProperty("env") as Map
        envs.put(name.toLowerCase(), env)
    }

    def methodMissing(String name, args) {
    }
}
