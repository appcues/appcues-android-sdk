package com.appcues.data.remote

import com.appcues.AppcuesConfig
import com.appcues.data.remote.appcues.AppcuesRemoteSource
import com.appcues.data.remote.appcues.AppcuesService
import com.appcues.data.remote.appcues.MetricsInterceptor
import com.appcues.data.remote.customerapi.CustomerApiBaseUrlInterceptor
import com.appcues.data.remote.customerapi.CustomerApiRemoteSource
import com.appcues.data.remote.customerapi.CustomerApiService
import com.appcues.data.remote.imageupload.ImageUploadRemoteSource
import com.appcues.data.remote.imageupload.ImageUploadService
import com.appcues.data.remote.sdksettings.AppcuesBundleRemoteSource
import com.appcues.data.remote.sdksettings.BundleService
import com.appcues.di.AppcuesModule
import com.appcues.di.scope.AppcuesScopeDSL
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.concurrent.TimeUnit.SECONDS

internal object DataRemoteModule : AppcuesModule {

    override fun AppcuesScopeDSL.install() {
        scoped {
            val config: AppcuesConfig = get()
            AppcuesRemoteSource(
                service = RetrofitWrapper(
                    baseUrl = (config.apiBasePath ?: AppcuesRemoteSource.BASE_URL).toHttpUrl(),
                    interceptors = listOf(MetricsInterceptor()),
                    okhttpConfig = {
                        it.readTimeout(AppcuesRemoteSource.READ_TIMEOUT_SECONDS, SECONDS)
                    }
                ).create(AppcuesService::class),
                config = config,
                storage = get(),
            )
        }

        scoped {
            AppcuesBundleRemoteSource(
                service = RetrofitWrapper(
                    baseUrl = AppcuesBundleRemoteSource.BASE_URL.toHttpUrl()
                ).create(BundleService::class),
                config = get(),
                experienceMapper = get(),
                ruleMapper = get()
            )
        }

        scoped {
            CustomerApiRemoteSource(
                service = RetrofitWrapper(
                    baseUrl = null,
                    interceptors = listOf(CustomerApiBaseUrlInterceptor()),
                ).create(CustomerApiService::class),
                config = get()
            )
        }

        scoped {
            ImageUploadRemoteSource(
                service = RetrofitWrapper(null).create(ImageUploadService::class)
            )
        }
    }
}
