package org.example.utils

import org.example.Counter
import org.example.Property

fun <T> Property<T>.assign(newValue: T): Unit {
    this.set(newValue)
}

fun Counter.assign(newValue: Int): Unit {
    this.counter = newValue
}
