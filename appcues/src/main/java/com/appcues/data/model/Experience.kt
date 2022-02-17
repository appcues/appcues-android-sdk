package com.appcues.data.model

import android.os.Parcelable
import com.appcues.data.model.action.Action
import com.appcues.data.model.step.Step
import com.appcues.data.model.trait.Trait
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
