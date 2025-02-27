package org.reclaimprotocol.inapp_sdk

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/**
 * This class provides functionality to initiate and manage the verification process
 * for proving claims about user data through various providers.
 */
public class ReclaimVerification {
    public object ReclaimSessionIdentity {
        public var appId: String = ""
            internal set

        public var sessionId: String = ""
            internal set

        public var providerId: String = ""
            internal set
    }

    /**
     * Represents user's session information for a verification attempt.
     * This data class contains the necessary data to identify and validate a verification session.
     */
    public data class ReclaimSessionInformation(
        /**
         * The timestamp of the session creation.
         *
         * Represented as a string from number of milliseconds since
         * the "Unix epoch" 1970-01-01T00:00:00Z (UTC).
         *
         * This value is independent of the time zone.
         *
         * This value is at most 8,640,000,000,000,000ms (100,000,000 days) from the Unix epoch.
         */
        public val timestamp: String,
        /**
         * Unique identifier for the verification session
         */
        public val sessionId: String,
        /**
         * Cryptographic signature to validate the session
         */
        public val signature: String
    )

    /**
     * Represents a request for a verification attempt.
     *
     * You can create a request using the [ReclaimVerification.Request] constructor or the [ReclaimVerification.Request.fromManifestMetaData] factory method.
     */
    public data class Request(
        /**
         * The Reclaim application ID for the verification process.
         * If not provided, the appId will be fetched from the AndroidManifest.xml metadata along with secret.
         * ```xml
         * <meta-data android:name="org.reclaimprotocol.inapp_sdk.APP_ID"
         *            android:value="<YOUR_RECLAIM_APP_ID>" />
         * ```
         */
        public val appId: String,
        /**
         * The Reclaim application secret for the verification process.
         * If not provided, the secret will be fetched from the AndroidManifest.xml metadata along with appId.
         * ```xml
         * <meta-data android:name="org.reclaimprotocol.inapp_sdk.APP_SECRET"
         *            android:value="<YOUR_RECLAIM_APP_SECRET>" />
         * ```
         */
        public val secret: String,
        /**
         * The identifier for the Reclaim data provider to use in verification
         */
        public val providerId: String,
        /**
         * Optional session information. If nil, SDK generates new session details.
         */
        public val session: ReclaimSessionInformation? = null,
        /**
         * Additional data to associate with the verification attempt
         */
        public val contextString: String = "",
        /**
         * Key-value pairs for prefilling claim creation variables
         */
        public val parameters: Map<String, String> = emptyMap(),
        /**
         * Whether to automatically submit the proof after generation.
         */
        public val autoSubmit: Boolean = false,
        public val acceptAiProviders: Boolean = false,
        public val webhookUrl: String? = null
    ) {
        companion object {
            private const val META_APP_ID = "org.reclaimprotocol.inapp_sdk.APP_ID"
            private const val META_APP_SECRET = "org.reclaimprotocol.inapp_sdk.APP_SECRET"

            /**
             * Creates a Reclaim Verification Request object using app credentials from AndroidManifest.xml metadata.
             *
             * You must add following metadata to your AndroidManifest.xml:
             * ```xml
             * <meta-data android:name="org.reclaimprotocol.inapp_sdk.APP_ID"
             *            android:value="<YOUR_RECLAIM_APP_ID>" />
             * <meta-data android:name="org.reclaimprotocol.inapp_sdk.APP_SECRET"
             *            android:value="<YOUR_RECLAIM_APP_SECRET>" />
             * ```
             *
             * Throws [ReclaimVerificationException.Failed] when metadata is missing.
             * See also [ReclaimVerification.Request(appId:secret:)]
             */
            public fun fromManifestMetaData(
                context: Context,
                providerId: String,
                session: ReclaimSessionInformation? = null,
                contextString: String = "",
                parameters: Map<String, String> = emptyMap(),
                autoSubmit: Boolean = false,
                acceptAiProviders: Boolean = false,
                webhookUrl: String? = null
            ): Request {
                val ai: ApplicationInfo = context.packageManager.getApplicationInfo(
                    context.packageName, PackageManager.GET_META_DATA
                )
                val appId = ai.metaData.getString(META_APP_ID) ?: ""
                val appSecret = ai.metaData.getString(META_APP_SECRET) ?: ""
                if (appId.isEmpty() || appSecret.isEmpty()) {
                    throw ReclaimVerificationException.Failed(
                        sessionId = session?.sessionId ?: "",
                        didSubmitManualVerification = false,
                        reason = "Either \"${META_APP_ID}\" or \"${META_APP_SECRET}\" metadata are missing in AndroidManifest.xml. Either provide appId and secret in AndroidManifest.xml or use ReclaimVerification.Request(appId:secret:) constructor"
                    )
                }
                return Request(
                    appId = appId,
                    secret = appSecret,
                    providerId = providerId,
                    session = session,
                    contextString = contextString,
                    parameters = parameters,
                    autoSubmit = autoSubmit,
                    acceptAiProviders = acceptAiProviders,
                    webhookUrl = webhookUrl
                )
            }
        }
    }

    /**
     * Contains the proof and response data after verification
     */
    public data class Response(
        /**
         * The session ID for the verification attempt
         */
        public val sessionId: String,
        /**
         * Whether the proof was submitted manually
         */
        public val didSubmitManualVerification: Boolean,
        /**
         * The list of proofs generated during the verification attempt
         */
        public val proofs: List<Map<String, Any?>>
    )

    /**
     * Represents exceptions that can occur during the verification process.
     */
    public sealed class ReclaimVerificationException(
        public val sessionId: String, public val didSubmitManualVerification: Boolean, public open val reason: String? = null
    ) : Exception() {
        public class Cancelled(sessionId: String, didSubmitManualVerification: Boolean) :
            ReclaimVerificationException(sessionId, didSubmitManualVerification)

        public class Dismissed(sessionId: String, didSubmitManualVerification: Boolean) :
            ReclaimVerificationException(sessionId, didSubmitManualVerification)

        public class SessionExpired(sessionId: String, didSubmitManualVerification: Boolean) :
            ReclaimVerificationException(sessionId, didSubmitManualVerification)

        public class Failed(
            sessionId: String, didSubmitManualVerification: Boolean, public override val reason: String
        ) : ReclaimVerificationException(sessionId, didSubmitManualVerification)
    }

    public final class ReclaimPlatformException internal constructor(
        override val message: String,
        override val cause: Throwable
    ) : Exception(message, cause) {
        private fun asFlutterError(): FlutterError? {
            val e = cause
            if (e is FlutterError) return e
            return null
        }
        public var errorCode: String? = asFlutterError()?.code
            internal set
        public var internalErrorMessage: String? = asFlutterError()?.message
            internal set
        public var internalErrorDetails: Any? = asFlutterError()?.details
            internal set
    }

    /**
     * A handler for the verification result.
     */
    public interface ResultHandler {
        /**
         * Called when the verification is successful.
         */
        public fun onResponse(response: Response)

        /**
         * Called when an exception occurs during the verification process.
         */
        public fun onException(exception: ReclaimVerificationException)
    }

    public companion object {
        private fun getModuleApi(context: Context): ReclaimModuleApi {
            val messenger = ReclaimActivity.requireBinaryMessenger(context)
            val moduleApi = ReclaimModuleApi(messenger)
            return moduleApi
        }

        /**
         * This instantiates and caches the backend used by the ReclaimActivity.
         * Calling this method in advance is recommended to avoid the first launch of the ReclaimActivity from being slow.
         */
        public fun preWarm(context: Context) {
            ReclaimActivity.preWarm(context = context)
        }

        /**
         * Starts the verification process from a URL.
         * Initiates the verification process by presenting a full-screen interface.
         * This method handles the entire verification flow, including:
         * - Presenting the user interface for verification
         * - Managing the verification session
         * - Processing the verification result
         * - Returning the proof upon successful completion
         *
         * See also [ReclaimVerification.startVerification] which starts the verification process from a Request object.
         */
        public fun startVerificationFromUrl(
            context: Context, requestUrl: String, handler: ResultHandler
        ) {
            preWarm(context)
            ReclaimActivity.start(context)
            val moduleApi = getModuleApi(context)
            moduleApi.startVerificationFromUrl(
                requestUrl
            ) { result ->
                ReclaimActivity.closeAll()
                onApiResult(ReclaimSessionIdentity.sessionId, result, handler)
            }
        }

        /**
         * Starts the verification process from a Request object.
         * Initiates the verification process by presenting a full-screen interface.
         * This method handles the entire verification flow, including:
         * - Presenting the user interface for verification
         * - Managing the verification session
         * - Processing the verification result
         * - Returning the proof upon successful completion
         *
         * See also [ReclaimVerification.startVerificationFromUrl] which starts the verification process from a URL.
         */
        public fun startVerification(
            context: Context, request: Request, handler: ResultHandler
        ) {
            preWarm(context)
            ReclaimActivity.start(context)
            val moduleApi = getModuleApi(context)
            moduleApi.startVerification(
                ReclaimApiVerificationRequest(
                    appId = request.appId,
                    providerId = request.providerId,
                    secret = request.secret,
                    signature = request.session?.signature ?: "",
                    timestamp = request.session?.timestamp ?: "",
                    context = request.contextString,
                    sessionId = request.session?.sessionId ?: "",
                    parameters = request.parameters,
                    autoSubmit = request.autoSubmit,
                    acceptAiProviders = request.acceptAiProviders,
                    webhookUrl = request.webhookUrl
                )
            ) { result ->
                ReclaimActivity.closeAll()
                onApiResult(ReclaimSessionIdentity.sessionId.ifBlank {
                    request.session?.sessionId ?: ""
                }, result, handler)
            }
        }

        private var previousReclaimApiImpl: ReclaimApi? = null

        /**
         * Configures overrides for the Reclaim verification process.
         * This method allows customization of various aspects of the verification flow.
         *
         * @param context The Android context
         * @param provider Optional override for provider information configuration
         *                Use this to override the default provider information from URL or JSON
         * @param featureOptions Optional override for feature-specific settings
         *                      Customize behavior like cookie persistence, session timeouts, etc.
         * @param logConsumer Optional override for logging configuration
         *                   Configure how logs are handled and whether telemetry is collected
         * @param sessionManagement Optional override for session management
         *                         Provide a handler implementation for custom session management
         * @param appInfo Optional override for application information
         *               Customize how your app information appears in the verification flow
         * @param callback Callback to handle the result of setting overrides
         *                Returns Result<Unit> indicating success or failure
         *
         * Example usage:
         * ```kotlin
         * ReclaimVerification.setOverrides(
         *     context = context,
         *     featureOptions = ReclaimOverrides.FeatureOptions(
         *         cookiePersist = true,
         *         singleReclaimRequest = true
         *     ),
         *     appInfo = ReclaimOverrides.ReclaimAppInfo(
         *         appName = "My App",
         *         appImageUrl = "https://example.com/app-logo.png"
         *     )
         * ) { result ->
         *     result.onSuccess {
         *         // Overrides set successfully
         *     }.onFailure { error ->
         *         // Handle error
         *     }
         * }
         * ```
         */
        public fun setOverrides(
            context: Context,
            provider: ReclaimOverrides.ProviderInformation.Override? = null,
            featureOptions: ReclaimOverrides.FeatureOptions? = null,
            logConsumer: ReclaimOverrides.LogConsumer? = null,
            sessionManagement: ReclaimOverrides.SessionManagement? = null,
            appInfo: ReclaimOverrides.ReclaimAppInfo? = null,
            capabilityAccessToken: String? = null,
            sessionIdentityUpdateHandler: ReclaimOverrides.SessionIdentityUpdateHandler? = null,
            callback: (Result<Unit>) -> Unit
        ) {
            preWarm(context)
            val moduleApi = getModuleApi(context)
            val messenger = ReclaimActivity.requireBinaryMessenger(context)
            val currentReclaimApi = object : ReclaimApi {
                var previousApi: ReclaimApi? = null

                override fun ping(callback: (Result<Boolean>) -> Unit) {
                    callback(Result.success(true))
                }

                override fun onLogs(logJsonString: String, callback: (Result<Unit>) -> Unit) {
                    logConsumer?.logHandler?.onLogs(logJsonString)
                    if (logConsumer == null) {
                        val api = previousApi
                        if (api != null) {
                            return api.onLogs(logJsonString, callback)
                        }
                    }
                    callback(Result.success(Unit))
                }

                override fun createSession(
                    appId: String,
                    providerId: String,
                    sessionId: String,
                    callback: (Result<Boolean>) -> Unit
                ) {
                    sessionManagement?.handler?.createSession(
                        appId = appId,
                        providerId = providerId,
                        sessionId = sessionId,
                        callback = callback
                    )
                    if (sessionManagement == null) {
                        val api = previousApi
                        if (api != null) {
                            return api.createSession(
                                appId = appId,
                                providerId = providerId,
                                sessionId = sessionId,
                                callback = callback
                            )
                        }
                    }
                }

                override fun updateSession(
                    sessionId: String,
                    status: ReclaimSessionStatus,
                    callback: (Result<Boolean>) -> Unit
                ) {
                    sessionManagement?.handler?.updateSession(
                        sessionId = sessionId, status = status, callback = callback
                    )
                    if (sessionManagement == null) {
                        val api = previousApi
                        if (api != null) {
                            return api.updateSession(
                                sessionId = sessionId, status = status, callback = callback
                            )
                        }
                    }
                }

                override fun logSession(
                    appId: String,
                    providerId: String,
                    sessionId: String,
                    logType: String,
                    callback: (Result<Unit>) -> Unit
                ) {
                    sessionManagement?.handler?.logSession(
                        appId = appId,
                        providerId = providerId,
                        sessionId = sessionId,
                        logType = logType
                    )
                    if (sessionManagement == null) {
                        val api = previousApi
                        if (api != null) {
                            return api.logSession(
                                appId = appId,
                                providerId = providerId,
                                sessionId = sessionId,
                                logType = logType,
                                callback = callback
                            )
                        }
                    }
                    callback(Result.success(Unit))
                }

                override fun onSessionIdentityUpdate(
                    update: ReclaimSessionIdentityUpdate?, callback: (Result<Unit>) -> Unit
                ) {
                    ReclaimSessionIdentity.appId = update?.appId ?: ""
                    ReclaimSessionIdentity.sessionId = update?.sessionId ?: ""
                    ReclaimSessionIdentity.providerId = update?.providerId ?: ""
                    sessionIdentityUpdateHandler?.onSessionIdentityUpdate(ReclaimSessionIdentity)
                    if (sessionIdentityUpdateHandler == null) {
                        val api = previousApi
                        if (api != null) {
                            return api.onSessionIdentityUpdate(
                                update = update, callback = callback
                            )
                        }
                    }
                }

                override fun fetchProviderInformation(
                    appId: String,
                    providerId: String,
                    sessionId: String,
                    signature: String,
                    timestamp: String,
                    callback: (Result<String>) -> Unit
                ) {
                    val handler = provider?.callback
                    if (handler == null) {
                        if (provider == null) {
                            val api = previousApi
                            if (api != null) {
                                return api.fetchProviderInformation(
                                    appId = appId,
                                    providerId = providerId,
                                    sessionId = sessionId,
                                    signature = signature,
                                    timestamp = timestamp,
                                    callback = callback
                                )
                            }
                        }
                        callback(Result.failure(Exception("No callback provided")))
                        return
                    }
                    handler.fetchProviderInformation(
                        appId = appId,
                        providerId = providerId,
                        sessionId = sessionId,
                        signature = signature,
                        timestamp = timestamp,
                        callback = callback
                    )
                }
            }
            currentReclaimApi.previousApi = previousReclaimApiImpl
            ReclaimApi.setUp(
                binaryMessenger = messenger,
                api = currentReclaimApi,
            )
            previousReclaimApiImpl = currentReclaimApi
            moduleApi.setOverrides(
                providerArg = if (provider == null) null else ClientProviderInformationOverride(
                    providerInformationUrl = provider.url,
                    providerInformationJsonString = provider.jsonString,
                    canFetchProviderInformationFromHost = provider.callback != null,
                ),
                featureArg = if (featureOptions == null) null else ClientFeatureOverrides(
                    cookiePersist = featureOptions.cookiePersist,
                    singleReclaimRequest = featureOptions.singleReclaimRequest,
                    idleTimeThresholdForManualVerificationTrigger = featureOptions.idleTimeThresholdForManualVerificationTrigger,
                    sessionTimeoutForManualVerificationTrigger = featureOptions.sessionTimeoutForManualVerificationTrigger,
                    attestorBrowserRpcUrl = featureOptions.attestorBrowserRpcUrl,
                    isResponseRedactionRegexEscapingEnabled = featureOptions.isResponseRedactionRegexEscapingEnabled,
                    isAIFlowEnabled = featureOptions.isAIFlowEnabled
                ),
                logConsumerArg = if (logConsumer == null) null else ClientLogConsumerOverride(
                    enableLogHandler = logConsumer.logHandler != null,
                    canSdkCollectTelemetry = logConsumer.canSdkCollectTelemetry,
                    canSdkPrintLogs = logConsumer.canSdkPrintLogs
                ),
                sessionManagementArg = if (sessionManagement == null) null else ClientReclaimSessionManagementOverride(
                    // A handler has been provided. We'll not let SDK manage sessions in this case.
                    // Disabling this lets the host manage sessions.
                    enableSdkSessionManagement = false
                ),
                appInfoArg = if (appInfo == null) null else ClientReclaimAppInfoOverride(
                    appName = appInfo.appName,
                    appImageUrl = appInfo.appImageUrl,
                    isRecurring = appInfo.isRecurring
                ),
                capabilityAccessTokenArg = capabilityAccessToken,
                callback = { result ->
                    result
                    .onSuccess {
                        callback(Result.success(Unit))
                    }.onFailure { exception ->
                        callback(Result.failure(ReclaimPlatformException(exception.message ?: "Could not set overrides", exception)))
                    }
            },
        )
    }

    public fun clearAllOverrides(
        context: Context, callback: (Result<Unit>) -> Unit
    ) {
        preWarm(context)
        val moduleApi = getModuleApi(context)
        moduleApi.clearAllOverrides {
            previousReclaimApiImpl = null
            callback(Result.success(Unit))
        }
    }

    private fun onApiResult(
        maybeSessionId: String,
        result: Result<ReclaimApiVerificationResponse>,
        handler: ResultHandler,
    ) {
        result.fold(onSuccess = { response ->
            val exception = response.exception
            if (exception == null) {
                handler.onResponse(
                    Response(
                        sessionId = response.sessionId,
                        didSubmitManualVerification = response.didSubmitManualVerification,
                        proofs = response.proofs
                    )
                )
            } else {
                val returnedException: ReclaimVerificationException = when (exception.type) {
                    ReclaimApiVerificationExceptionType.SESSION_EXPIRED -> ReclaimVerificationException.SessionExpired(
                        sessionId = response.sessionId,
                        didSubmitManualVerification = response.didSubmitManualVerification
                    )

                    ReclaimApiVerificationExceptionType.VERIFICATION_DISMISSED -> ReclaimVerificationException.Dismissed(
                        sessionId = response.sessionId,
                        didSubmitManualVerification = response.didSubmitManualVerification
                    )

                    ReclaimApiVerificationExceptionType.VERIFICATION_CANCELLED -> ReclaimVerificationException.Cancelled(
                        sessionId = response.sessionId,
                        didSubmitManualVerification = response.didSubmitManualVerification
                    )

                    ReclaimApiVerificationExceptionType.VERIFICATION_FAILED, ReclaimApiVerificationExceptionType.UNKNOWN -> ReclaimVerificationException.Failed(
                        sessionId = response.sessionId,
                        didSubmitManualVerification = response.didSubmitManualVerification,
                        reason = response.exception.message
                    )
                }
                handler.onException(returnedException)
            }
        }, onFailure = { error ->
            handler.onException(
                ReclaimVerificationException.Failed(
                    sessionId = maybeSessionId,
                    didSubmitManualVerification = false,
                    reason = error.message ?: "Unknown error"
                )
            )
        })
    }
}
}