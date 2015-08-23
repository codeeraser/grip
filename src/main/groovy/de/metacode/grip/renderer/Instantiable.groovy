package de.metacode.grip.renderer

/**
 * Created by mloesch on 17.08.15.
 */

/**
 * Marker Interface to add instantiation methods to the {@link de.metacode.grip.core.CoreProcessor}.
 * Two methods will be added, named by the template new<ClassName> and new<ClassName>With.
 * new<ClassName> doesn't expect parameters. new<ClassName>With expects a Map (to call the default constructor).
 */
interface Instantiable {
}