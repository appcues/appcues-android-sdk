package com.appcues.di.definition

internal class DefinitionParams(params: List<Any?> = listOf()) {

    private val iterator = params.iterator()

    fun <T> next(): T {
        @Suppress("UNCHECKED_CAST")
        return iterator.next() as T
    }

    @Suppress("unused")
    fun <T> nextOrNull(): T? {
        @Suppress("UNCHECKED_CAST")
        return iterator.next() as T?
    }
}
