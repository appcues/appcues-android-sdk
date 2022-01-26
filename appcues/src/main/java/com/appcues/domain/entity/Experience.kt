package com.appcues.domain.entity

import android.os.Parcelable
import com.appcues.domain.entity.action.Action
import com.appcues.domain.entity.step.Step
import com.appcues.domain.entity.trait.Trait
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
internal data class Experience(
    val id: UUID,
    val name: String,
    val actions: HashMap<UUID, List<Action>>,
    val traits: List<Trait>,
    val steps: List<Step>
) : Parcelable
