package com.appcues.data.model.rules

internal sealed class Operator {

    abstract fun evaluate(original: String?, other: String?): Boolean

    object Invalid : Operator() {

        override fun evaluate(original: String?, other: String?) = false
    }

    object Equals : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return original == other
        }
    }

    object DoesntEquals : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return original != other
        }
    }

    object Contains : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return when {
                original == null -> false
                other == null -> true
                else -> original.contains(other)
            }
        }
    }

    object DoesntContains : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return when {
                original == null -> true
                other == null -> false
                else -> original.contains(other).not()
            }
        }
    }

    object StartsWith : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return when {
                original == null -> false
                other == null -> true
                else -> original.startsWith(other)
            }
        }
    }

    object DoesntStartsWith : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return when {
                original == null -> true
                other == null -> false
                else -> original.startsWith(other).not()
            }
        }
    }

    object EndsWith : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return when {
                original == null -> false
                other == null -> true
                else -> original.endsWith(other)
            }
        }
    }

    object DoesntEndsWith : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return when {
                original == null -> true
                other == null -> false
                else -> original.endsWith(other).not()
            }
        }
    }

    object MatchesRegex : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return when {
                original == null -> false
                other == null -> true
                else -> original.contains(other.toRegex())
            }
        }
    }

    object IsOneOf : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return when {
                original == null -> false
                other == null -> true
                else -> other.split(",", "/n").any { it.trim() == original }
            }
        }
    }

    object IsntOneOf : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return when {
                original == null -> true
                other == null -> false
                else -> other.split(",", "/n").none { it.trim() == original }
            }
        }
    }

    object Exists : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return original != null
        }
    }

    object DoesntExists : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return original == null
        }
    }

    object IsGreaterThan : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return IntVersion(original) > IntVersion(other)
        }
    }

    object IsGreaterOrEqual : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return IntVersion(original) >= IntVersion(other)
        }
    }

    object IsLessThan : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return IntVersion(original) < IntVersion(other)
        }
    }

    object IsLessOrEqual : Operator() {

        override fun evaluate(original: String?, other: String?): Boolean {
            return IntVersion(original) <= IntVersion(other)
        }
    }
}
