package org.reclaimprotocol.inapp_sdk

public class ReclaimOverrides {
    public class ProviderInformation {
        public interface Override {
            public val url: String?
            public val jsonString: String?
            public val callback: FromCallback.Handler?
        }

        /**
         * Represents a provider information override using url.
         * Provider information as a json is downloaded from the given url.
         */
        public data class FromUrl(override val url: String) : Override {
            public override val jsonString: String? = null
            public override val callback: FromCallback.Handler? = null
        }

        /**
         * Represents a provider information override using json string.
         */
        public data class FromJsonString(override val jsonString: String) : Override {
            public override val url: String? = null
            public override val callback: FromCallback.Handler? = null
        }

        /**
         * Represents a provider information override using callback handler.
         */
        public data class FromCallback(override val callback: FromCallback.Handler) : Override {
            public override val jsonString: String? = null
            public override val url: String? = null

            public interface Handler {
                public fun fetchProviderInformation(
                    appId: String,
                    providerId: String,
                    sessionId: String,
                    signature: String,
                    timestamp: String,
                    resolvedVersion: String,
                    callback: (Result<String>) -> Unit
                )
            }
        }

    }

    public data class FeatureOptions(
        public val cookiePersist: Boolean? = null,
        public val singleReclaimRequest: Boolean? = null,
        public val idleTimeThresholdForManualVerificationTrigger: Long? = null,
        public val sessionTimeoutForManualVerificationTrigger: Long? = null,
        public val attestorBrowserRpcUrl: String? = null,
        public val isAIFlowEnabled: Boolean? = null,
        public val manualReviewMessage: String? = null,
        public val loginPromptMessage: String? = null
    )

    public data class LogConsumer(
        /**
         * Handler for consuming logs exported from the SDK.
         */
        public val logHandler: LogHandler? = null,
        /**
         * When enabled, logs are sent to reclaim that can be used to help you.
         * Defaults to true.
         */
        public val canSdkCollectTelemetry: Boolean = true,
        /**
         * Defaults to enabled when not in release mode.
         */
        public val canSdkPrintLogs: Boolean? = null
    ) {
        public interface LogHandler {
            public fun onLogs(logJsonString: String)
        }
    }

    public data class SessionManagement(
        public val handler: SessionHandler
    ) {
        public interface SessionHandler {
            public fun createSession(
                appId: String,
                providerId: String,
                timestamp: String,
                signature: String,
                providerVersion: String,
                callback: (Result<InitResponse>) -> Unit
            )

            public fun updateSession(
                sessionId: String, status: ReclaimSessionStatus, callback: (Result<Boolean>) -> Unit
            )

            public fun logSession(
                appId: String, providerId: String, sessionId: String, logType: String, metadata: Map<String, Any?>?
            )
        }

        public data class InitResponse(
            public val sessionId: String,
            public val resolvedProviderVersion: String
        )
    }

    public data class ReclaimAppInfo(
        public val appName: String,
        public val appImageUrl: String,
        public val isRecurring: Boolean = false
    )

    public interface SessionIdentityUpdateHandler {
        public fun onSessionIdentityUpdate(identity: ReclaimVerification.ReclaimSessionIdentity)
    }
}