# generated Moshi adapaters
-keep class com.appcues.**.*JsonAdapter { *; }

# required for element targeting with Jetpack Compose
-keep class androidx.compose.ui.platform.AndroidComposeView { *; }

# used in API levels <= 28 to find the root window for injecting content
-keep class android.view.WindowManagerGlobal { *; }

# *** START For AGP 8 issue with Retrofit and R8 https://github.com/square/retrofit/issues/3751#issuecomment-1192043644
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# *** END For AGP 8 issue with Retrofit and R8