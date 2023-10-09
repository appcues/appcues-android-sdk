package com.appcues.data.mapper.rules

import com.appcues.data.model.rules.Condition
import com.appcues.data.model.rules.Operator
import com.appcues.data.model.rules.Operator.Contains
import com.appcues.data.model.rules.Operator.DoesntContains
import com.appcues.data.model.rules.Operator.DoesntEndsWith
import com.appcues.data.model.rules.Operator.DoesntEquals
import com.appcues.data.model.rules.Operator.DoesntExists
import com.appcues.data.model.rules.Operator.DoesntStartsWith
import com.appcues.data.model.rules.Operator.EndsWith
import com.appcues.data.model.rules.Operator.Equals
import com.appcues.data.model.rules.Operator.Exists
import com.appcues.data.model.rules.Operator.Invalid
import com.appcues.data.model.rules.Operator.IsGreaterOrEqual
import com.appcues.data.model.rules.Operator.IsGreaterThan
import com.appcues.data.model.rules.Operator.IsLessOrEqual
import com.appcues.data.model.rules.Operator.IsLessThan
import com.appcues.data.model.rules.Operator.IsOneOf
import com.appcues.data.model.rules.Operator.IsntOneOf
import com.appcues.data.model.rules.Operator.MatchesRegex
import com.appcues.data.model.rules.Operator.StartsWith
import com.appcues.data.model.rules.QualificationRule
import com.appcues.data.model.rules.RuleFrequency
import com.appcues.data.model.rules.RuleFrequency.EVERY_TIME
import com.appcues.data.model.rules.condition.AndCondition
import com.appcues.data.model.rules.condition.AttributesCondition
import com.appcues.data.model.rules.condition.InvalidCondition
import com.appcues.data.model.rules.condition.NorCondition
import com.appcues.data.model.rules.condition.NotCondition
import com.appcues.data.model.rules.condition.OrCondition
import com.appcues.data.model.rules.condition.PropertiesCondition
import com.appcues.data.model.rules.condition.ScreenCondition
import com.appcues.data.model.rules.condition.TriggerCondition
import com.appcues.data.remote.appcues.response.rules.ConditionResponse
import com.appcues.data.remote.appcues.response.rules.RulesResponse

internal class RulesMapper {

    fun map(from: RulesResponse?): QualificationRule? {
        if (from == null) return null

        return QualificationRule(
            conditions = from.conditions.toCondition(),
            frequency = from.frequency.toRuleFrequency(),
            updatedAt = from.updatedAt,
        )
    }

    private fun ConditionResponse.toCondition(): Condition {
        return when {
            and != null -> AndCondition(and.map { it.toCondition() })
            or != null -> OrCondition(or.map { it.toCondition() })
            nor != null -> NorCondition(nor.map { it.toCondition() })
            not != null -> NotCondition(not.toCondition())
            screen != null -> ScreenCondition(screen.value, screen.operator.toOperator())
            trigger != null -> TriggerCondition(trigger.event, trigger.conditions?.toCondition())
            attributes != null -> AttributesCondition(attributes.attribute, attributes.value, attributes.operator.toOperator())
            properties != null -> PropertiesCondition(properties.property, properties.value, properties.operator.toOperator())
            else -> InvalidCondition()
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun String.toOperator(): Operator {
        return when (this) {
            "==" -> Equals
            "!=" -> DoesntEquals
            "*" -> Contains
            "!*" -> DoesntContains
            "^" -> StartsWith
            "!^" -> DoesntStartsWith
            "$" -> EndsWith
            "!$" -> DoesntEndsWith
            "regex" -> MatchesRegex
            "in" -> IsOneOf
            "not in" -> IsntOneOf
            "?" -> Exists
            "!?" -> DoesntExists
            ">" -> IsGreaterThan
            ">=" -> IsGreaterOrEqual
            "<" -> IsLessThan
            "<=" -> IsLessOrEqual
            else -> Invalid
        }
    }

    private fun String?.toRuleFrequency(): RuleFrequency {
        return when (this) {
            "once" -> RuleFrequency.ONCE
            else -> EVERY_TIME
        }
    }
}
