package com.appcues.domain.gateway

import com.appcues.domain.entity.Experience

internal interface DataGateway {

    suspend fun getContent(contentId: String): Experience
}
