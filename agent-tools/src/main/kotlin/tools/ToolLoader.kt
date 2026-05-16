package org.jbareaud.agent.tools

import io.github.classgraph.ClassGraph

fun load(): List<Class<*>> {
    val pkg = ToolRegistry::class.java.`package` .name
    val routeAnnotation = Toolable::class.java.name
    val results = mutableListOf<Class<*>>()
    ClassGraph()
        .verbose()
        .enableAllInfo()
        .acceptPackages(pkg)
        .scan().use { scanResult ->
            for (routeClassInfo in scanResult.getClassesWithAnnotation(routeAnnotation)) {
                val clazz = Class.forName(routeClassInfo.name)
                results.add(clazz)
            }
        }
    return results
}