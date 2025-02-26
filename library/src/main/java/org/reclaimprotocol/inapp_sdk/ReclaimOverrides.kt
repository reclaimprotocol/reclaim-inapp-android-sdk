package org.reclaimprotocol.inapp_sdk

public class ReclaimOverrides {
    public class ProviderInformation {
        interface Override {
            val url: String?
            val jsonString: String?
            val callback: FromCallbackHandler?
        }

        /**
         * Represents a provider information override using url.
         * Provider information as a json is downloaded from the given url.
         */
        data class FromUrl(override val url: String) : Override {
            override val jsonString: String? = null
            override val callback: FromCallbackHandler? = null
        }

        /**
         * Represents a provider information override using json string.
         */
        data class FromJsonString(override val jsonString: String) : Override {
            override val url: String? = null
            override val callback: FromCallbackHandler? = null
        }

        /**
         * Represents a provider information override using callback handler.
         */
        data class FromCallback(override val callback: FromCallbackHandler) : Override {
            override val jsonString: String? = null
            override val url: String? = null
        }

        public interface FromCallbackHandler {
            public fun fetchProviderInformation(
                appId: String,
                providerId: String,
                sessionId: String,
                signature: String,
                timestamp: String,
                callback: (Result<Map<String, Any?>>) -> Unit
            )
        }
    }

    public data class FeatureOptions(
        val cookiePersist: Boolean? = null,
        val singleReclaimRequest: Boolean? = null,
        val idleTimeThresholdForManualVerificationTrigger: Long? = null,
        val sessionTimeoutForManualVerificationTrigger: Long? = null,
        val attestorBrowserRpcUrl: String? = null,
        val isResponseRedactionRegexEscapingEnabled: Boolean? = null,
        val isAIFlowEnabled: Boolean? = null
    )

    public data class LogConsumer(
        /**
         * Handler for consuming logs exported from the SDK.
         */
        val logHandler: LogHandler? = null,
        /**
         * When enabled, logs are sent to reclaim that can be used to help you.
         * Defaults to true.
         */
        val canSdkCollectTelemetry: Boolean = true,
        /**
         * Defaults to enabled when not in release mode.
         */
        val canSdkPrintLogs: Boolean? = null
    ) {
        public interface LogHandler {
            public fun onLogs(logJsonString: String)
        }
    }

    public data class SessionManagement(
        val handler: SessionHandler
    ) {
        public interface SessionHandler {
            public fun createSession(
                appId: String,
                providerId: String,
                sessionId: String,
                callback: (Result<Boolean>) -> Unit
            )

            public fun updateSession(
                sessionId: String,
                status: ReclaimSessionStatus,
                callback: (Result<Boolean>) -> Unit
            )

            public fun logSession(
                appId: String,
                providerId: String,
                sessionId: String,
                logType: String
            )
        }
    }

    public data class ReclaimAppInfo(
        val appName: String,
        val appImageUrl: String,
        val isRecurring: Boolean = false
    )

    public interface SessionIdentityUpdateHandler {
        public fun onSessionIdentityUpdate(identity: ReclaimVerification.ReclaimSessionIdentity)
    }
}