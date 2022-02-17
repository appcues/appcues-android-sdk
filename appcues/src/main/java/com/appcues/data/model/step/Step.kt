package com.appcues.data.model.step

import android.os.Parcelable
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.action.Action
import com.appcues.data.model.trait.Trait
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
internal data class Step(
    val id: UUID,
    val content: ExperiencePrimitive,
    val traits: List<Trait>,
    val actions: HashMap<UUID, List<Action>>
) : Parcelable
