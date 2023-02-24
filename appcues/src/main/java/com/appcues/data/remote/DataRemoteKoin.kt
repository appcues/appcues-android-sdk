package com.appcues.data.remote

import com.appcues.AppcuesConfig
import com.appcues.data.remote.retrofit.AppcuesService
import com.appcues.data.remote.retrofit.CustomerApiService
import com.appcues.data.remote.retrofit.ImageUploadService
import com.appcues.data.remote.retrofit.RetrofitWrapper
import com.appcues.data.remote.retrofit.SdkSettingsService
import com.appcues.di.KoinScopePlugin
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.koin.dsl.ScopeDSL
import java.util.concurrent.TimeUnit.SECONDS

internal object DataRemoteKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
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
                sessionMonitor = get(),
            )
        }

        scoped {
            SdkSettingsRemoteSource(
                service = RetrofitWrapper(
                    baseUrl = SdkSettingsRemoteSource.BASE_URL.toHttpUrl()
                ).create(SdkSettingsService::class),
                config = get(),
            )
        }

        scoped {
            CustomerApiRemoteSource(
                service = RetrofitWrapper(
                    baseUrl = null,
                    interceptors = listOf(CustomerApiHostInterceptor()),
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
