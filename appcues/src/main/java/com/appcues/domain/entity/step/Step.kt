package com.appcues.domain.entity.step

import android.os.Parcelable
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.action.Action
import com.appcues.domain.entity.trait.Trait
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
internal data class Step(
    val id: UUID,
    val content: ExperienceComponent,
    val traits: List<Trait>,
    val actions: HashMap<String, List<Action>>
) : Parcelable
