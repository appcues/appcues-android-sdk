package com.appcues.data.mapper.trait

import com.appcues.data.model.trait.Trait
import com.appcues.data.remote.response.trait.TraitResponse

internal fun List<TraitResponse>.mapToTrait(transform: (TraitResponse) -> Trait) = map { transform(it) }
