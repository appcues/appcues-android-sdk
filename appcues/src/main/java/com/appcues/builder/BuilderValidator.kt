package com.appcues.builder

internal interface BuilderValidator<T> {

    /**
     * validate and handle value to fit requirements for the builder
     */
    fun validate(value: T): T
}
