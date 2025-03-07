package com.appcues.util

import java.util.Locale

internal fun String.toSlug() = lowercase(Locale.getDefault())
    .replace("\n", " ")
    .replace("[^a-z\\d\\s]".toRegex(), " ")
    .split(" ")
    .filter { it.isNotEmpty() }
    .joinToString("-")
    .replace("-+".toRegex(), "-")
    .trimEnd('-')
    .trimStart('-')

@Suppress("TooGenericExceptionCaught", "SwallowedException")
internal fun String.beautify(indentationMultiplier: Int): String {
    try {
        val indent = " ".repeat(indentationMultiplier)
        val builder = StringBuilder()
        var indentation = 0
        var quoting = false

        forEachIndexed { index, char ->
            if (char == '"') quoting = !quoting

            if (quoting) {
                builder.append(char)
            } else if (!builder.handle(indent, char, indentation)) {
                indentation = beautifyAtIndex(indent, builder, char, index, indentation)
            }
        }

        return builder.toString()
    } catch (e: Exception) {
        // in case any parsing goes wrong just return existing text
        return this
    }
}

private fun StringBuilder.handle(indent: String, char: Char, sourceIndentation: Int): Boolean {
    return when {
        // break line and keep indentation
        char == ',' -> {
            appendLine(char)
            append(indent.repeat(sourceIndentation))
            true
        }
        // skip empty spaces when we are indenting
        char == ' ' && sourceIndentation > 0 -> true
        else -> false
    }
}

private fun String.beautifyAtIndex(indent: String, prettyJson: StringBuilder, char: Char, index: Int, sourceIndentation: Int): Int {
    var indentation = sourceIndentation
    when {
        char == '(' && get(index + 1) != ')' -> {
            prettyJson.appendLine(char)
            indentation++
            prettyJson.append(indent.repeat(indentation))
        }
        char == '{' && get(index + 1) != '}' -> {
            prettyJson.appendLine(char)
            indentation++
            prettyJson.append(indent.repeat(indentation))
        }
        char == '[' && get(index + 1) != ']' -> {
            prettyJson.appendLine(char)
            indentation++
            prettyJson.append(indent.repeat(indentation))
        }
        char == ']' && get(index - 1) != '[' -> {
            prettyJson.appendLine()
            indentation--
            prettyJson.append(indent.repeat(indentation))
            prettyJson.append(char)
        }
        char == '}' && get(index - 1) != '{' -> {
            prettyJson.appendLine()
            indentation--
            prettyJson.append(indent.repeat(indentation))
            prettyJson.append(char)
        }
        char == ')' && get(index - 1) != '(' -> {
            prettyJson.appendLine()
            indentation--
            prettyJson.append(indent.repeat(indentation))
            prettyJson.append(char)
        }
        else -> prettyJson.append(char)
    }

    return indentation
}
