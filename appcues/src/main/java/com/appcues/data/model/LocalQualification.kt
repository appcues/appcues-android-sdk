package com.appcues.data.model

import com.appcues.data.model.rules.QualificationRule

internal data class LocalQualification(
    val qualifications: List<QualifiableExperience>
)

internal data class QualifiableExperience(
    val rule: QualificationRule,
    val experience: Experience,
    val sortingPriority: Int,
)
