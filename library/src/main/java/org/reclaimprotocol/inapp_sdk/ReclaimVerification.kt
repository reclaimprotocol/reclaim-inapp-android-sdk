package org.reclaimprotocol.inapp_sdk

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import io.flutter.plugin.common.BinaryMessenger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.reclaimprotocol.inapp_sdk.ReclaimVerification.ReclaimSessionIdentity
import kotlin.coroutines.resume

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
        /** A `ProviderVersion` for resolving the version of provider that must be used in the verification flow.
         *  Defaults to a versionExpression with empty string that represents latest version.
         */
        public val providerVersion: ProviderVersion = ProviderVersion(),
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
                providerVersion: ProviderVersion = ProviderVersion()
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
                    providerVersion = providerVersion
                )
            }
        }

        public data class ProviderVersion (
            public val resolvedVersion: String,
            public val versionExpression: String
        ) {
            companion object {
                public fun resolved(exactVersion: String, versionExpression: String? = null): ProviderVersion {
                    return ProviderVersion(
                        resolvedVersion = exactVersion,
                        versionExpression = versionExpression ?: exactVersion
                    )
                }
            }

            public constructor(versionExpression: String = "") : this(
                resolvedVersion = versionExpression,
                versionExpression = versionExpression
            )
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

    public data class VerificationOptions(
        /**
         * Whether to delete cookies before user journey starts in the client web view.
         * Defaults to true.
         */
        public val canDeleteCookiesBeforeVerificationStarts: Boolean = true,
        /**
         * Use a callback to host that returns an authentication request when a Reclaim HTTP provider is provided.
         */
        public val attestorAuthRequestProvider: AttestorAuthRequestProvider? = null,
        public val claimCreationType: ClaimCreationType = ClaimCreationType.STANDALONE,
        /**
         * Whether to automatically submit the proof after generation.
         * Defaults to true.
         */
        public val canAutoSubmit: Boolean = true,
        /**
         * Whether the close button is visible.
         * Defaults to true.
         */
        public val isCloseButtonVisible: Boolean = true,
        /**
         * A language code & Country code for localization that should be enforced in the verification flow.
         */
        public val locale: String? = null,
        /**
         * Enables use of Reclaim's TEE+MPC protocol for HTTP Request claim verification and
         * attestation.
         *
         * When set to `true`, the verification will use Trusted Execution Environment
         * (TEE) with Multi-Party Computation (MPC) for enhanced security.
         *
         * When set to `false`, the standard Reclaim's proxy attestor verification flow is used.
         *
         * When `null` (default), the backend decides whether to use TEE based on
         * a feature flag (currently in staged rollout).
         */
        public val useTeeOperator: Boolean? = null
    ) {
        public interface AttestorAuthRequestProvider {
            public fun fetchAttestorAuthenticationRequest(
                reclaimHttpProvider: Map<Any?, Any?>,
                callback: (Result<String>) -> Unit
            )
        }

        public enum class ClaimCreationType() {
            STANDALONE,
            ME_CHAIN;

            internal fun toApi(): ClaimCreationTypeApi {
                return when(this) {
                    STANDALONE -> ClaimCreationTypeApi.STANDALONE
                    ME_CHAIN -> ClaimCreationTypeApi.ME_CHAIN
                }
            }
        }
    }

    /**
     * Represents exceptions that can occur during the verification process.
     */
    public sealed class ReclaimVerificationException(
        public val sessionId: String, public val reason: String, public val didSubmitManualVerification: Boolean
    ) : Exception() {
        public class Cancelled(sessionId: String, reason: String, didSubmitManualVerification: Boolean) :
            ReclaimVerificationException(sessionId, reason, didSubmitManualVerification)

        public class Dismissed(sessionId: String, didSubmitManualVerification: Boolean, reason: String) :
            ReclaimVerificationException(sessionId, reason, didSubmitManualVerification)

        public class SessionExpired(sessionId: String, didSubmitManualVerification: Boolean, reason: String) :
            ReclaimVerificationException(sessionId, reason, didSubmitManualVerification)

        public class Failed(
            sessionId: String, didSubmitManualVerification: Boolean, reason: String
        ) : ReclaimVerificationException(sessionId, reason, didSubmitManualVerification)
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

        // Pigeon generates `ReclaimModuleApi` as suspend functions. The public API
        // here stays callback-based, so each call runs on the main thread (where
        // Flutter platform channels must be used) and reports its outcome through
        // the callback, success or failure.
        private val moduleScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        private fun <T> callModule(call: suspend () -> T, onResult: (Result<T>) -> Unit) {
            moduleScope.launch {
                val result = try {
                    Result.success(call())
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Result.failure(e)
                }
                onResult(result)
            }
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
            callModule({ moduleApi.startVerificationFromUrl(requestUrl) }) { result ->
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
            val apiRequest = ReclaimApiVerificationRequest(
                    appId = request.appId,
                    providerId = request.providerId,
                    secret = request.secret,
                    signature = request.session?.signature ?: "",
                    timestamp = request.session?.timestamp ?: "",
                    context = request.contextString,
                    sessionId = request.session?.sessionId ?: "",
                    parameters = request.parameters,
                    providerVersion = ProviderVersionApi(
                        versionExpression = request.providerVersion.versionExpression,
                        resolvedVersion = request.providerVersion.resolvedVersion
                    )
                )
            callModule({ moduleApi.startVerification(apiRequest) }) { result ->
                ReclaimActivity.closeAll()
                onApiResult(ReclaimSessionIdentity.sessionId.ifBlank {
                    request.session?.sessionId ?: ""
                }, result, handler)
            }
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
        public fun startVerificationFromJson(
            context: Context, template: Map<Any?, Any?>, handler: ResultHandler
        ) {
            preWarm(context)
            ReclaimActivity.start(context)
            val moduleApi = getModuleApi(context)
            val maybeSessionId = template["sessionId"]
            callModule({ moduleApi.startVerificationFromJson(template) }) { result ->
                ReclaimActivity.closeAll()
                onApiResult(ReclaimSessionIdentity.sessionId.ifBlank {
                    maybeSessionId as? String ?: ""
                }, result, handler)
            }
        }

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
            val hostApi = ReclaimHostOverridesApiImpl.getInstance(messenger)
            hostApi.providerCallback = provider?.callback
            hostApi.logHandler = logConsumer?.logHandler
            hostApi.sessionHandler = sessionManagement?.handler
            hostApi.sessionIdentityUpdateHandler = sessionIdentityUpdateHandler

            callModule({ moduleApi.setOverrides(
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
                    isAIFlowEnabled = featureOptions.isAIFlowEnabled,
                    manualReviewMessage = featureOptions.manualReviewMessage,
                    loginPromptMessage = featureOptions.loginPromptMessage,
                    useTEE = featureOptions.useTEE,
                    interceptorOptions = featureOptions.interceptorOptions,
                    claimCreationTimeoutDurationInMins = featureOptions.claimCreationTimeoutDurationInMins,
                    sessionNoActivityTimeoutDurationInMins = featureOptions.sessionNoActivityTimeoutDurationInMins,
                    aiProviderNoActivityTimeoutDurationInSecs = featureOptions.aiProviderNoActivityTimeoutDurationInSecs,
                    pageLoadedCompletedDebounceTimeoutMs = featureOptions.pageLoadedCompletedDebounceTimeoutMs,
                    potentialLoginTimeoutS = featureOptions.potentialLoginTimeoutS,
                    screenshotCaptureIntervalSeconds = featureOptions.screenshotCaptureIntervalSeconds,
                    teeUrls = featureOptions.teeUrls,
                    privacyPolicyUrl = featureOptions.privacyPolicyUrl,
                    termsOfServiceUrl = featureOptions.termsOfServiceUrl,
                    potentialFailureReasonsUrl = featureOptions.potentialFailureReasonsUrl
                ),
                logConsumerArg = if (logConsumer == null) null else ClientLogConsumerOverride(
                    enableLogHandler = logConsumer.logHandler != null,
                    canSdkCollectTelemetry = logConsumer.canSdkCollectTelemetry,
                    canSdkPrintLogs = logConsumer.canSdkPrintLogs,
                    logLevel = logConsumer.logLevel,
                    canLogMetadata = logConsumer.canLogMetadata
                ),
                sessionManagementArg = if (sessionManagement == null) null else ClientReclaimSessionManagementOverride(
                    // A handler has been provided. We'll not let SDK manage sessions in this case.
                    // Disabling this lets the host manage sessions.
                    enableSdkSessionManagement = false
                ),
                appInfoArg = if (appInfo == null) null else ClientReclaimAppInfoOverride(
                    appName = appInfo.appName,
                    appImageUrl = appInfo.appImageUrl,
                    isRecurring = appInfo.isRecurring,
                    theme = appInfo.theme
                ),
                capabilityAccessTokenArg = capabilityAccessToken,
            ) }) { result ->
                result
                .onSuccess {
                    callback(Result.success(Unit))
                }.onFailure { exception ->
                    callback(Result.failure(ReclaimPlatformException(exception.message ?: "Could not set overrides", exception)))
                }
            }
        }

        /**
         * Configures Builder transport for `api=2` verification links.
         *
         * Optional: without it, Builder links use `https://build.reclaimprotocol.org`
         * and the SDK's in-app Verification Client. [clearAllOverrides] restores
         * those defaults. Builder links never fall back to the legacy flow.
         *
         * @param context The Android context
         * @param overrides The Builder base URL (HTTPS) and Verification Client UUID
         * @param callback Returns Result<Unit>; a failure is a [ReclaimPlatformException],
         *                 for example when the base URL is not HTTPS
         *
         * Example usage:
         * ```kotlin
         * ReclaimVerification.setBuilderModeOverrides(
         *     context = context,
         *     overrides = ReclaimOverrides.BuilderMode(
         *         baseUrl = "https://build.reclaimprotocol.org",
         *         verificationClientId = "01234567-89ab-4def-8123-456789abcdef",
         *     ),
         * ) { result ->
         *     result.onFailure { error ->
         *         // Handle error
         *     }
         * }
         * ```
         */
        public fun setBuilderModeOverrides(
            context: Context,
            overrides: ReclaimOverrides.BuilderMode,
            callback: (Result<Unit>) -> Unit
        ) {
            preWarm(context)
            val moduleApi = getModuleApi(context)
            val apiOverrides = ClientBuilderModeOverrides(
                baseUrl = overrides.baseUrl,
                verificationClientId = overrides.verificationClientId,
            )
            callModule({ moduleApi.setBuilderModeOverrides(apiOverrides) }) { result ->
                result
                .onSuccess {
                    callback(Result.success(Unit))
                }.onFailure { exception ->
                    callback(Result.failure(ReclaimPlatformException(exception.message ?: "Could not set Builder mode overrides", exception)))
                }
            }
        }

        public fun clearAllOverrides(
            context: Context, callback: (Result<Unit>) -> Unit
        ) {
            preWarm(context)
            val messenger = ReclaimActivity.requireBinaryMessenger(context)
            val hostApi = ReclaimHostOverridesApiImpl.getInstance(messenger)
            val moduleApi = getModuleApi(context)

            // As before, host-side overrides are cleared once the module call
            // finishes, whatever its outcome.
            callModule({ moduleApi.clearAllOverrides() }) {
                hostApi.clearOverrides()
                callback(Result.success(Unit))
            }
        }

        public fun setVerificationOptions(
            context: Context,
            options: VerificationOptions?,
            callback: (Result<Unit>) -> Unit
        ) {
            preWarm(context)
            val moduleApi = getModuleApi(context)
            val messenger = ReclaimActivity.requireBinaryMessenger(context)
            val hostApi = ReclaimHostVerificationApiImpl.getInstance(messenger)
            if (options == null) {
                callModule({ moduleApi.setVerificationOptions(null) }, callback)
            } else {
                val provider = options.attestorAuthRequestProvider
                hostApi.attestorAuthRequestProvider = provider
                val apiOptions = ReclaimApiVerificationOptions(
                    canDeleteCookiesBeforeVerificationStarts = options.canDeleteCookiesBeforeVerificationStarts,
                    canUseAttestorAuthenticationRequest = provider != null,
                    claimCreationType = options.claimCreationType.toApi(),
                    canAutoSubmit = options.canAutoSubmit,
                    isCloseButtonVisible = options.isCloseButtonVisible,
                    locale = options.locale,
                    useTeeOperator = options.useTeeOperator
                )
                callModule({ moduleApi.setVerificationOptions(apiOptions) }, callback)
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
                    val reason = "${response.exception.message}${if (response.exception.stackTraceAsString.isNotBlank()) "\n*********\nstacktrace: ${response.exception.stackTraceAsString}" else ""}"

                    val returnedException: ReclaimVerificationException = when (exception.type) {
                        ReclaimApiVerificationExceptionType.SESSION_EXPIRED -> ReclaimVerificationException.SessionExpired(
                            sessionId = response.sessionId,
                            reason = reason,
                            didSubmitManualVerification = response.didSubmitManualVerification
                        )

                        ReclaimApiVerificationExceptionType.VERIFICATION_DISMISSED -> ReclaimVerificationException.Dismissed(
                            sessionId = response.sessionId,
                            reason = reason,
                            didSubmitManualVerification = response.didSubmitManualVerification
                        )

                        ReclaimApiVerificationExceptionType.VERIFICATION_CANCELLED -> ReclaimVerificationException.Cancelled(
                            sessionId = response.sessionId,
                            reason = reason,
                            didSubmitManualVerification = response.didSubmitManualVerification
                        )

                        ReclaimApiVerificationExceptionType.VERIFICATION_FAILED, ReclaimApiVerificationExceptionType.UNKNOWN -> ReclaimVerificationException.Failed(
                            sessionId = response.sessionId,
                            reason = reason,
                            didSubmitManualVerification = response.didSubmitManualVerification,
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

        public fun setConsoleLogging(
            context: Context,
            enabled: Boolean,
            callback: (Result<Unit>) -> Unit
        ) {
            preWarm(context)
            val moduleApi = getModuleApi(context)
            callModule({ moduleApi.setConsoleLogging(enabled) }, callback)
        }
    }
}

private class ReclaimHostOverridesApiImpl private constructor() : ReclaimHostOverridesApi {
    companion object {
        private val instance = ReclaimHostOverridesApiImpl()

        fun getInstance(messenger: BinaryMessenger): ReclaimHostOverridesApiImpl {
            ReclaimHostOverridesApi.setUp(messenger, instance)
            return instance
        }
    }

    var providerCallback: ReclaimOverrides.ProviderInformation.FromCallback.Handler? = null
        set(value) {
            if (value == null) return
            field = value
        }
    var logHandler: ReclaimOverrides.LogConsumer.LogHandler? = null
        set(value) {
            if (value == null) return
            field = value
        }
    var sessionHandler: ReclaimOverrides.SessionManagement.SessionHandler? = null
        set(value) {
            if (value == null) return
            field = value
        }
    var sessionIdentityUpdateHandler: ReclaimOverrides.SessionIdentityUpdateHandler? = null
        set(value) {
            if (value == null) return
            field = value
        }

    fun clearOverrides() {
        providerCallback = null
        logHandler = null
        sessionHandler = null
        sessionIdentityUpdateHandler = null
    }

    override suspend fun onLogs(logJsonString: String) {
        logHandler?.onLogs(logJsonString)
    }

    override suspend fun createSession(
        appId: String,
        providerId: String,
        timestamp: String,
        signature: String,
        providerVersion: String,
    ): SessionInitResponseApi {
        val handler = sessionHandler ?: throw IllegalStateException("No session handler provided")
        val response = awaitHostResult<ReclaimOverrides.SessionManagement.InitResponse> { callback ->
            handler.createSession(
                appId = appId,
                providerId = providerId,
                timestamp = timestamp,
                signature = signature,
                providerVersion = providerVersion,
                callback = callback,
            )
        }
        return SessionInitResponseApi(
            sessionId = response.sessionId,
            resolvedProviderVersion = response.resolvedProviderVersion
        )
    }

    override suspend fun updateSession(
        sessionId: String,
        status: ReclaimSessionStatus,
        metadata: Map<String, Any?>?,
    ): Boolean {
        val handler = sessionHandler ?: throw IllegalStateException("No session handler provided")
        return awaitHostResult { callback ->
            handler.updateSession(sessionId = sessionId, status = status, callback = callback)
        }
    }

    override suspend fun logSession(
        appId: String,
        providerId: String,
        sessionId: String,
        logType: String,
        metadata: Map<String, Any?>?,
    ) {
        sessionHandler?.logSession(
            appId = appId,
            providerId = providerId,
            sessionId = sessionId,
            logType = logType,
            metadata = metadata,
        )
    }

    override suspend fun onSessionIdentityUpdate(update: ReclaimSessionIdentityUpdate?) {
        ReclaimSessionIdentity.appId = update?.appId ?: ""
        ReclaimSessionIdentity.sessionId = update?.sessionId ?: ""
        ReclaimSessionIdentity.providerId = update?.providerId ?: ""
        sessionIdentityUpdateHandler?.onSessionIdentityUpdate(ReclaimSessionIdentity)
    }

    override suspend fun fetchProviderInformation(
        appId: String,
        providerId: String,
        sessionId: String,
        signature: String,
        timestamp: String,
        resolvedVersion: String,
    ): String {
        val handler = providerCallback ?: throw Exception("No callback provided")
        return awaitHostResult { callback ->
            handler.fetchProviderInformation(
                appId = appId,
                providerId = providerId,
                sessionId = sessionId,
                signature = signature,
                timestamp = timestamp,
                resolvedVersion = resolvedVersion,
                callback = callback,
            )
        }
    }
}

private class ReclaimHostVerificationApiImpl private constructor() : ReclaimHostVerificationApi {
    companion object {
        private val instance = ReclaimHostVerificationApiImpl()

        fun getInstance(messenger: BinaryMessenger): ReclaimHostVerificationApiImpl {
            ReclaimHostVerificationApi.setUp(messenger, instance)
            return instance
        }
    }

    var attestorAuthRequestProvider: ReclaimVerification.VerificationOptions.AttestorAuthRequestProvider? = null
        set(value) {
            if (value == null) return
            field = value
        }

    override suspend fun fetchAttestorAuthenticationRequest(
        reclaimHttpProvider: Map<Any?, Any?>,
    ): String {
        val provider = attestorAuthRequestProvider
            ?: throw Exception("No attestorAuthRequestProvider was provided")
        return awaitHostResult { callback ->
            provider.fetchAttestorAuthenticationRequest(reclaimHttpProvider, callback)
        }
    }
}

/**
 * Bridges a callback-based host handler to Pigeon's suspend host API.
 *
 * Resumes once with the handler's first result; a later call is ignored rather
 * than crashing on a double resume.
 */
private suspend fun <T> awaitHostResult(register: (callback: (Result<T>) -> Unit) -> Unit): T =
    suspendCancellableCoroutine { continuation ->
        register { result ->
            if (continuation.isActive) continuation.resume(result)
        }
    }.getOrThrow()
