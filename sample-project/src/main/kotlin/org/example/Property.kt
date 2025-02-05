package org.example

@AssignOpAnnotation
class Property<T> {
    var value: T? = null
    fun set(newValue: T) {
        value = newValue
    }
}