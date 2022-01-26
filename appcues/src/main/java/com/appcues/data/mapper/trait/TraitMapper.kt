package com.appcues.data.mapper.trait

import com.appcues.data.remote.response.trait.TraitResponse
import com.appcues.domain.entity.trait.Trait

internal class TraitMapper {

    fun map(from: TraitResponse) = Trait(
        type = from.type
    )
}
