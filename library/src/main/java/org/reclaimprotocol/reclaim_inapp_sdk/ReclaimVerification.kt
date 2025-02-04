package org.reclaimprotocol.reclaim_inapp_sdk

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

/**
 * This class provides functionality to initiate and manage the verification process
 * for proving claims about user data through various providers.
 */
public class ReclaimVerification {
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
        val timestamp: String,
        /**
         * Unique identifier for the verification session
         */
        val sessionId: String,
        /**
         * Cryptographic signature to validate the session
         */
        val signature: String
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
         * <meta-data android:name="org.reclaimprotocol.reclaim_inapp_sdk.APP_ID"
         *            android:value="<YOUR_RECLAIM_APP_ID>" />
         * ```
         */
        val appId: String,
        /**
         * The Reclaim application secret for the verification process.
         * If not provided, the secret will be fetched from the AndroidManifest.xml metadata along with appId.
         * ```xml
         * <meta-data android:name="org.reclaimprotocol.reclaim_inapp_sdk.APP_SECRET"
         *            android:value="<YOUR_RECLAIM_APP_SECRET>" />
         * ```
         */
        val secret: String,
        /**
         * The identifier for the Reclaim data provider to use in verification
         */
        val providerId: String,
        /**
         * Optional session information. If nil, SDK generates new session details.
         */
        val session: ReclaimSessionInformation? = null,
        /**
         * Additional data to associate with the verification attempt
         */
        val contextString: String = "",
        /**
         * Key-value pairs for prefilling claim creation variables
         */
        val parameters: Map<String, String> = emptyMap(),
        /**
         * Whether to hide the landing page of the verification process. When false, shows an introductory page with claims to be proven.
         */
        val hideLanding: Boolean = true,
        /**
         * Whether to automatically submit the proof after generation.
         */
        val autoSubmit: Boolean = false,
        val acceptAiProviders: Boolean = false,
        val webhookUrl: String? = null
    ) {
        companion object {
            private const val META_APP_ID = "org.reclaimprotocol.reclaim_inapp_sdk.APP_ID"
            private const val META_APP_SECRET = "org.reclaimprotocol.reclaim_inapp_sdk.APP_SECRET"

            /**
             * Creates a Reclaim Verification Request object using app credentials from AndroidManifest.xml metadata.
             * 
             * You must add following metadata to your AndroidManifest.xml:
             * ```xml
             * <meta-data android:name="org.reclaimprotocol.reclaim_inapp_sdk.APP_ID"
             *            android:value="<YOUR_RECLAIM_APP_ID>" />
             * <meta-data android:name="org.reclaimprotocol.reclaim_inapp_sdk.APP_SECRET"
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
                hideLanding: Boolean = true,
                autoSubmit: Boolean = false,
                acceptAiProviders: Boolean = false,
                webhookUrl: String? = null
            ): Request {
                val ai: ApplicationInfo = context.packageManager
                    .getApplicationInfo(
                        context.packageName,
                        PackageManager.GET_META_DATA
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
                    hideLanding = hideLanding,
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
        val sessionId: String,
        val didSubmitManualVerification: Boolean
    ) : Exception() {
        public class Cancelled(sessionId: String, didSubmitManualVerification: Boolean) :
            ReclaimVerificationException(sessionId, didSubmitManualVerification)

        public class Dismissed(sessionId: String, didSubmitManualVerification: Boolean) :
            ReclaimVerificationException(sessionId, didSubmitManualVerification)

        public class SessionExpired(sessionId: String, didSubmitManualVerification: Boolean) :
            ReclaimVerificationException(sessionId, didSubmitManualVerification)

        public class Failed(
            sessionId: String,
            didSubmitManualVerification: Boolean,
            public val reason: String
        ) : ReclaimVerificationException(sessionId, didSubmitManualVerification)
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

    companion object {
        private fun getModuleApi(context: Context): ReclaimModuleApi {
            val engine = ReclaimActivity.requireEngine(context)
            val moduleApi = ReclaimModuleApi(engine.dartExecutor.binaryMessenger)
            return moduleApi
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
            context: Context,
            requestUrl: String,
            handler: ResultHandler
        ) {
            ReclaimActivity.preWarm(context)
            ReclaimActivity.start(context)
            val moduleApi = getModuleApi(context)
            moduleApi.startVerificationFromUrl(
                requestUrl
            ) { result ->
                ReclaimActivity.closeAll()
                onApiResult("", result, handler)
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
            context: Context,
            request: Request,
            handler: ResultHandler
        ) {
            ReclaimActivity.preWarm(context)
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
                    hideLanding = request.hideLanding,
                    autoSubmit = request.autoSubmit,
                    acceptAiProviders = request.acceptAiProviders,
                    webhookUrl = request.webhookUrl
                )
            ) { result ->
                ReclaimActivity.closeAll()
                onApiResult(request.session?.sessionId ?: "", result, handler)
            }
        }

        private fun onApiResult(
            maybeSessionId: String,
            result: Result<ReclaimApiVerificationResponse>,
            handler: ResultHandler,
        ) {
            result.fold(
                onSuccess = { response ->
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
                        val returnedException: ReclaimVerificationException =
                            when (exception.type) {
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
                },
                onFailure = { error ->
                    handler.onException(
                        ReclaimVerificationException.Failed(
                            sessionId = maybeSessionId,
                            didSubmitManualVerification = false,
                            reason = error.message ?: "Unknown error"
                        )
                    )
                }
            )
        }
    }
}