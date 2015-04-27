package de.metacode.grip.core

import de.metacode.grip.env.Env
import de.metacode.grip.env.SqlEnv
import groovy.sql.Sql

/**
 * Created by mloesch on 14.03.15.
 */
class SqlProcessor {

    Sql sql
    Binding binding

    SqlProcessor(Binding binding) {
        this.binding = binding
        Env property = this.binding.getProperty("env") as Env
        if (!property instanceof SqlEnv) {
            throw new IllegalStateException("env must e Sql")
        }
        this.sql = property.createEnv() as Sql
    }
}