package com.appcues.data.mapper.trait

import com.appcues.data.remote.response.trait.TraitResponse
import com.appcues.domain.entity.trait.Trait

internal fun List<TraitResponse>.mapToTrait(transform: (TraitResponse) -> Trait) = map { transform(it) }
