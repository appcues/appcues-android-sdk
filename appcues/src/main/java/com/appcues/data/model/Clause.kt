package com.appcues.data.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.UUID
import java.util.regex.PatternSyntaxException

internal sealed class Clause {
    data class And(val conditions: List<Clause>) : Clause() {
        override fun toString(): String = conditions.joinToString(" AND ") { it.toString() }
    }
    data class Or(val conditions: List<Clause>) : Clause() {
        override fun toString(): String = conditions.joinToString(" OR ") { it.toString() }
    }
    data class Not(val clause: Clause) : Clause() {
        override fun toString(): String = "NOT $clause"
    }
    data class Survey(val clause: SurveyClause) : Clause() {
        override fun toString(): String = clause.toString()
    }
    data class Token(val clause: TokenClause) : Clause() {
        override fun toString(): String = clause.toString()
    }
    data class Unknown(val type: String = "unknown") : Clause() {
        override fun toString(): String = "unknown clause"
    }

    fun evaluate(state: Map<UUID, String>): Boolean {
        return when (this) {
            is And -> conditions.all { it.evaluate(state) }
            is Or -> conditions.any { it.evaluate(state) }
            is Not -> !clause.evaluate(state)
            is Survey -> clause.evaluate(state)
            is Token -> clause.evaluate()
            is Unknown -> false
        }
    }
    
    data class SurveyClause(
        val block: UUID,
        val operator: Operator,
        val value: String
    ) {
        fun evaluate(state: Map<UUID, String>): Boolean {
            val propertyValue = state[block] ?: return false
            return operator.evaluate(propertyValue, value)
        }

        override fun toString(): String {
            return "$block $operator $value"
        }
    }

    data class TokenClause(
        val token: String,
        val operator: Operator,
        val value: String
    ) {
        fun evaluate(): Boolean {
            return operator.evaluate(token, value)
        }

        override fun toString(): String {
            return "$token $operator $value"
        }
    }

    internal enum class Operator(val rawValue: String) {
        // DEFAULT_OPERATORS
        EQUALS("=="),
        DOESNT_EQUAL("!="),
        CONTAINS("*"),
        DOESNT_CONTAIN("!*"),
        STARTS_WITH("^"),
        DOESNT_START_WITH("!^"),
        ENDS_WITH("$"),
        DOESNT_END_WITH("!$"),
        MATCHES_REGEX("regex"),

        // MULTI_VALUE_OPERATORS
        IS_ONE_OF("in"),
        ISNT_ONE_OF("not in"),

        // NUMERIC_OPERATORS
        IS_GREATER_THAN(">"),
        IS_GREATER_OR_EQUAL(">="),
        IS_LESS_THAN("<"),
        IS_LESS_OR_EQUAL("<=");

        @Suppress("SwallowedException")
        fun evaluate(lhs: String, rhs: String): Boolean {
            return when (this) {
                EQUALS -> lhs == rhs
                DOESNT_EQUAL -> lhs != rhs
                CONTAINS -> lhs.contains(rhs)
                DOESNT_CONTAIN -> !lhs.contains(rhs)
                STARTS_WITH -> lhs.startsWith(rhs)
                DOESNT_START_WITH -> !lhs.startsWith(rhs)
                ENDS_WITH -> lhs.endsWith(rhs)
                DOESNT_END_WITH -> !lhs.endsWith(rhs)
                MATCHES_REGEX -> {
                    try {
                        if (rhs.isEmpty()) return false
                        val regex = rhs.toRegex()
                        regex.containsMatchIn(lhs)
                    } catch (e: PatternSyntaxException) {
                        false
                    }
                }
                IS_ONE_OF -> lhs.split("\n").contains(rhs)
                ISNT_ONE_OF -> !lhs.split("\n").contains(rhs)
                IS_GREATER_THAN -> try { lhs.toDouble() > rhs.toDouble() } catch (e: NumberFormatException) { false }
                IS_GREATER_OR_EQUAL -> try { lhs.toDouble() >= rhs.toDouble() } catch (e: NumberFormatException) { false }
                IS_LESS_THAN -> try { lhs.toDouble() < rhs.toDouble() } catch (e: NumberFormatException) { false }
                IS_LESS_OR_EQUAL -> try { lhs.toDouble() <= rhs.toDouble() } catch (e: NumberFormatException) { false }
            }
        }

        override fun toString(): String {
            return when (this) {
                EQUALS -> "equals"
                DOESNT_EQUAL -> "doesn't equal"
                CONTAINS -> "contains"
                DOESNT_CONTAIN -> "doesn't contain"
                STARTS_WITH -> "starts with"
                DOESNT_START_WITH -> "doesn't start with"
                ENDS_WITH -> "ends with"
                DOESNT_END_WITH -> "doesn't end with"
                MATCHES_REGEX -> "matches regex"
                IS_ONE_OF -> "is one of"
                ISNT_ONE_OF -> "isn't one of"
                IS_GREATER_THAN -> "is greater than"
                IS_GREATER_OR_EQUAL -> "is greater than or equal to"
                IS_LESS_THAN -> "is less than"
                IS_LESS_OR_EQUAL -> "is less than or equal to"
            }
        }
    }
}

// Custom adapter for Clause sealed class
internal class ClauseAdapter {
    @FromJson
    fun fromJson(json: Map<String, Any>): Clause? {
        return when {
            json.containsKey("survey") -> parseSurveyClause(json["survey"] as? Map<String, Any>)
            json.containsKey("token") -> parseTokenClause(json["token"] as? Map<String, Any>)
            json.containsKey("and") -> parseLogicalClause(json["and"] as? List<Map<String, Any>>) { conditions -> Clause.And(conditions) }
            json.containsKey("or") -> parseLogicalClause(json["or"] as? List<Map<String, Any>>) { conditions -> Clause.Or(conditions) }
            json.containsKey("not") -> parseNotClause(json["not"] as? Map<String, Any>)
            else -> Clause.Unknown()
        }
    }

    @Suppress("SwallowedException")
    private fun parseSurveyClause(surveyData: Map<String, Any>?): Clause? {
        if (surveyData == null) return null
        
        return try {
            val block = UUID.fromString(surveyData["block"] as String)
            val operator = parseOperator(surveyData["operator"])
            val value = surveyData["value"] as String
            Clause.Survey(Clause.SurveyClause(block, operator, value))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun parseTokenClause(tokenData: Map<String, Any>?): Clause? {
        if (tokenData == null) return null
        
        val token = tokenData["token"] as String
        val operator = parseOperator(tokenData["operator"])
        val value = tokenData["value"] as String
        return Clause.Token(Clause.TokenClause(token, operator, value))
    }

    private fun parseLogicalClause(
        conditionsData: List<Map<String, Any>>?,
        constructor: (List<Clause>) -> Clause
    ): Clause? {
        if (conditionsData == null) return null
        
        val conditions = conditionsData.mapNotNull { fromJson(it) }
        return constructor(conditions)
    }

    private fun parseNotClause(notData: Map<String, Any>?): Clause? {
        if (notData == null) return null
        
        val clause = fromJson(notData)
        return clause?.let { Clause.Not(it) }
    }

    private fun parseOperator(operatorValue: Any?): Clause.Operator {
        val operatorString = operatorValue as? String
        return Clause.Operator.values().find { it.rawValue == operatorString }
            ?: throw IllegalArgumentException("Unknown operator: $operatorString")
    }

    @ToJson
    fun toJson(clause: Clause): Map<String, Any> {
        return when (clause) {
            is Clause.Survey -> {
                mapOf("survey" to mapOf(
                    "block" to clause.clause.block.toString(),
                    "operator" to clause.clause.operator.rawValue,
                    "value" to clause.clause.value
                ))
            }
            is Clause.Token -> {
                mapOf("token" to mapOf(
                    "token" to clause.clause.token,
                    "operator" to clause.clause.operator.rawValue,
                    "value" to clause.clause.value
                ))
            }
            is Clause.And -> {
                mapOf("and" to clause.conditions.map { toJson(it) })
            }
            is Clause.Or -> {
                mapOf("or" to clause.conditions.map { toJson(it) })
            }
            is Clause.Not -> {
                mapOf("not" to toJson(clause.clause))
            }
            is Clause.Unknown -> {
                mapOf()
            }
        }
    }
}
