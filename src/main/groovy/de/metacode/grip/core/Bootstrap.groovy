package de.metacode.grip.core

import groovy.grape.Grape

/**
 * Created by mloesch on 02.03.15.
 */
class Bootstrap {
    void run() {
        loadDependencies()
    }

    //this is how dependencies can be set programmatically
    void loadDependencies() {
        def classLoader = this.class.classLoader.rootLoader
        Map[] grapez = [
                [group : 'mysql', module : 'mysql-connector-java', version : '5.1.6'],
                [group : 'sapdbc', module : 'sapdbc', version : '7.4']
        ]
        Grape.grab(classLoader: classLoader, grapez)
    }
}