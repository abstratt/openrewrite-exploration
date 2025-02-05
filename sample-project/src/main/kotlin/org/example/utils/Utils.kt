package org.example.utils

import org.example.Property

fun <T> Property<T>.assign(newValue: T): Unit {
    println("Assign function called")
    this.set(newValue)
}
