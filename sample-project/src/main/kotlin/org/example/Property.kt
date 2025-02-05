package org.example

@CustomAnnotation
class Property<T> {
    var value: T? = null
    fun set(newValue: T) {
        value = newValue
    }
}