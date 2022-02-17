package com.appcues.data.mapper.trait

import com.appcues.data.model.trait.Trait
import com.appcues.data.remote.response.trait.TraitResponse

internal class TraitMapper {

    fun map(from: TraitResponse) = Trait(
        type = from.type
    )
}
