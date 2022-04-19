package com.appcues.builder

internal class ApiHostBuilderValidator : BuilderValidator<String> {

    override fun validate(value: String): String {
        value.startsWithHttpOrThrow()
        value.endsWithSlashOrThrow()
        return value
    }

    private fun String.startsWithHttpOrThrow() {
        if (startsWith("http").not()) {
            throw IllegalArgumentException("url should start with 'http'. e.g: https://api.appcues.net/")
        }
    }

    private fun String.endsWithSlashOrThrow() {
        if (endsWith("/").not()) {
            throw IllegalArgumentException("url should end with '/'. e.g: https://api.appcues.net/")
        }
    }
}
