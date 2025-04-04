// Copyright 2025, Reclaim Protocol. Use of this source code is governed by a license that can be found in the LICENSE file.
// Autogenerated from Pigeon (v24.2.2), do not edit directly.
// See also: https://pub.dev/packages/pigeon
@file:Suppress("UNCHECKED_CAST", "ArrayInDataClass")

package org.reclaimprotocol.inapp_sdk

import android.util.Log
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MessageCodec
import io.flutter.plugin.common.StandardMethodCodec
import io.flutter.plugin.common.StandardMessageCodec
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

private fun wrapResult(result: Any?): List<Any?> {
  return listOf(result)
}

private fun wrapError(exception: Throwable): List<Any?> {
  return if (exception is FlutterError) {
    listOf(
      exception.code,
      exception.message,
      exception.details
    )
  } else {
    listOf(
      exception.javaClass.simpleName,
      exception.toString(),
      "Cause: " + exception.cause + ", Stacktrace: " + Log.getStackTraceString(exception)
    )
  }
}

private fun createConnectionError(channelName: String): FlutterError {
  return FlutterError("channel-error",  "Unable to establish connection on channel: '$channelName'.", "")}

/**
 * Error class for passing custom error details to Flutter via a thrown PlatformException.
 * @property code The error code.
 * @property message The error message.
 * @property details The error details. Must be a datatype supported by the api codec.
 */
class FlutterError (
  val code: String,
  override val message: String? = null,
  val details: Any? = null
) : Throwable()

enum class ReclaimApiVerificationExceptionType(val raw: Int) {
  UNKNOWN(0),
  SESSION_EXPIRED(1),
  VERIFICATION_DISMISSED(2),
  VERIFICATION_FAILED(3),
  VERIFICATION_CANCELLED(4);

  companion object {
    fun ofRaw(raw: Int): ReclaimApiVerificationExceptionType? {
      return values().firstOrNull { it.raw == raw }
    }
  }
}

enum class ReclaimSessionStatus(val raw: Int) {
  USER_STARTED_VERIFICATION(0),
  USER_INIT_VERIFICATION(1),
  PROOF_GENERATION_STARTED(2),
  PROOF_GENERATION_RETRY(3),
  PROOF_GENERATION_SUCCESS(4),
  PROOF_GENERATION_FAILED(5),
  PROOF_SUBMITTED(6),
  PROOF_SUBMISSION_FAILED(7),
  PROOF_MANUAL_VERIFICATION_SUBMITTED(8);

  companion object {
    fun ofRaw(raw: Int): ReclaimSessionStatus? {
      return values().firstOrNull { it.raw == raw }
    }
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ReclaimApiVerificationRequest (
  val appId: String,
  val providerId: String,
  val secret: String,
  val signature: String,
  val timestamp: String? = null,
  val context: String,
  val sessionId: String,
  val parameters: Map<String, String>,
  val autoSubmit: Boolean,
  val acceptAiProviders: Boolean,
  val webhookUrl: String? = null
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ReclaimApiVerificationRequest {
      val appId = pigeonVar_list[0] as String
      val providerId = pigeonVar_list[1] as String
      val secret = pigeonVar_list[2] as String
      val signature = pigeonVar_list[3] as String
      val timestamp = pigeonVar_list[4] as String?
      val context = pigeonVar_list[5] as String
      val sessionId = pigeonVar_list[6] as String
      val parameters = pigeonVar_list[7] as Map<String, String>
      val autoSubmit = pigeonVar_list[8] as Boolean
      val acceptAiProviders = pigeonVar_list[9] as Boolean
      val webhookUrl = pigeonVar_list[10] as String?
      return ReclaimApiVerificationRequest(appId, providerId, secret, signature, timestamp, context, sessionId, parameters, autoSubmit, acceptAiProviders, webhookUrl)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      appId,
      providerId,
      secret,
      signature,
      timestamp,
      context,
      sessionId,
      parameters,
      autoSubmit,
      acceptAiProviders,
      webhookUrl,
    )
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ReclaimApiVerificationException (
  val message: String,
  val stackTraceAsString: String,
  val type: ReclaimApiVerificationExceptionType
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ReclaimApiVerificationException {
      val message = pigeonVar_list[0] as String
      val stackTraceAsString = pigeonVar_list[1] as String
      val type = pigeonVar_list[2] as ReclaimApiVerificationExceptionType
      return ReclaimApiVerificationException(message, stackTraceAsString, type)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      message,
      stackTraceAsString,
      type,
    )
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ReclaimApiVerificationResponse (
  val sessionId: String,
  val didSubmitManualVerification: Boolean,
  val proofs: List<Map<String, Any?>>,
  val exception: ReclaimApiVerificationException? = null
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ReclaimApiVerificationResponse {
      val sessionId = pigeonVar_list[0] as String
      val didSubmitManualVerification = pigeonVar_list[1] as Boolean
      val proofs = pigeonVar_list[2] as List<Map<String, Any?>>
      val exception = pigeonVar_list[3] as ReclaimApiVerificationException?
      return ReclaimApiVerificationResponse(sessionId, didSubmitManualVerification, proofs, exception)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      sessionId,
      didSubmitManualVerification,
      proofs,
      exception,
    )
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ClientProviderInformationOverride (
  val providerInformationUrl: String? = null,
  val providerInformationJsonString: String? = null,
  val canFetchProviderInformationFromHost: Boolean
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ClientProviderInformationOverride {
      val providerInformationUrl = pigeonVar_list[0] as String?
      val providerInformationJsonString = pigeonVar_list[1] as String?
      val canFetchProviderInformationFromHost = pigeonVar_list[2] as Boolean
      return ClientProviderInformationOverride(providerInformationUrl, providerInformationJsonString, canFetchProviderInformationFromHost)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      providerInformationUrl,
      providerInformationJsonString,
      canFetchProviderInformationFromHost,
    )
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ClientFeatureOverrides (
  val cookiePersist: Boolean? = null,
  val singleReclaimRequest: Boolean? = null,
  val idleTimeThresholdForManualVerificationTrigger: Long? = null,
  val sessionTimeoutForManualVerificationTrigger: Long? = null,
  val attestorBrowserRpcUrl: String? = null,
  val isAIFlowEnabled: Boolean? = null
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ClientFeatureOverrides {
      val cookiePersist = pigeonVar_list[0] as Boolean?
      val singleReclaimRequest = pigeonVar_list[1] as Boolean?
      val idleTimeThresholdForManualVerificationTrigger = pigeonVar_list[2] as Long?
      val sessionTimeoutForManualVerificationTrigger = pigeonVar_list[3] as Long?
      val attestorBrowserRpcUrl = pigeonVar_list[4] as String?
      val isAIFlowEnabled = pigeonVar_list[5] as Boolean?
      return ClientFeatureOverrides(cookiePersist, singleReclaimRequest, idleTimeThresholdForManualVerificationTrigger, sessionTimeoutForManualVerificationTrigger, attestorBrowserRpcUrl, isAIFlowEnabled)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      cookiePersist,
      singleReclaimRequest,
      idleTimeThresholdForManualVerificationTrigger,
      sessionTimeoutForManualVerificationTrigger,
      attestorBrowserRpcUrl,
      isAIFlowEnabled,
    )
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ClientLogConsumerOverride (
  val enableLogHandler: Boolean,
  val canSdkCollectTelemetry: Boolean,
  val canSdkPrintLogs: Boolean? = null
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ClientLogConsumerOverride {
      val enableLogHandler = pigeonVar_list[0] as Boolean
      val canSdkCollectTelemetry = pigeonVar_list[1] as Boolean
      val canSdkPrintLogs = pigeonVar_list[2] as Boolean?
      return ClientLogConsumerOverride(enableLogHandler, canSdkCollectTelemetry, canSdkPrintLogs)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      enableLogHandler,
      canSdkCollectTelemetry,
      canSdkPrintLogs,
    )
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ClientReclaimSessionManagementOverride (
  val enableSdkSessionManagement: Boolean
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ClientReclaimSessionManagementOverride {
      val enableSdkSessionManagement = pigeonVar_list[0] as Boolean
      return ClientReclaimSessionManagementOverride(enableSdkSessionManagement)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      enableSdkSessionManagement,
    )
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ClientReclaimAppInfoOverride (
  val appName: String,
  val appImageUrl: String,
  val isRecurring: Boolean
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ClientReclaimAppInfoOverride {
      val appName = pigeonVar_list[0] as String
      val appImageUrl = pigeonVar_list[1] as String
      val isRecurring = pigeonVar_list[2] as Boolean
      return ClientReclaimAppInfoOverride(appName, appImageUrl, isRecurring)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      appName,
      appImageUrl,
      isRecurring,
    )
  }
}

/**
 * Identification information of a session.
 *
 * Generated class from Pigeon that represents data sent in messages.
 */
data class ReclaimSessionIdentityUpdate (
  /** The application id. */
  val appId: String,
  /** The provider id. */
  val providerId: String,
  /** The session id. */
  val sessionId: String
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ReclaimSessionIdentityUpdate {
      val appId = pigeonVar_list[0] as String
      val providerId = pigeonVar_list[1] as String
      val sessionId = pigeonVar_list[2] as String
      return ReclaimSessionIdentityUpdate(appId, providerId, sessionId)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      appId,
      providerId,
      sessionId,
    )
  }
}

/** Generated class from Pigeon that represents data sent in messages. */
data class ReclaimApiVerificationOptions (
  /**
   * Whether to delete cookies before user journey starts in the client web view.
   * Defaults to true.
   */
  val canDeleteCookiesBeforeVerificationStarts: Boolean,
  /**
   * Whether module can use a callback to host that returns an authentication request when a Reclaim HTTP provider is provided.
   * Defaults to false.
   * {@macro CreateClaimOptions.attestorAuthenticationRequest}
   */
  val canUseAttestorAuthenticationRequest: Boolean
)
 {
  companion object {
    fun fromList(pigeonVar_list: List<Any?>): ReclaimApiVerificationOptions {
      val canDeleteCookiesBeforeVerificationStarts = pigeonVar_list[0] as Boolean
      val canUseAttestorAuthenticationRequest = pigeonVar_list[1] as Boolean
      return ReclaimApiVerificationOptions(canDeleteCookiesBeforeVerificationStarts, canUseAttestorAuthenticationRequest)
    }
  }
  fun toList(): List<Any?> {
    return listOf(
      canDeleteCookiesBeforeVerificationStarts,
      canUseAttestorAuthenticationRequest,
    )
  }
}
private open class MessagesPigeonCodec : StandardMessageCodec() {
  override fun readValueOfType(type: Byte, buffer: ByteBuffer): Any? {
    return when (type) {
      129.toByte() -> {
        return (readValue(buffer) as Long?)?.let {
          ReclaimApiVerificationExceptionType.ofRaw(it.toInt())
        }
      }
      130.toByte() -> {
        return (readValue(buffer) as Long?)?.let {
          ReclaimSessionStatus.ofRaw(it.toInt())
        }
      }
      131.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ReclaimApiVerificationRequest.fromList(it)
        }
      }
      132.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ReclaimApiVerificationException.fromList(it)
        }
      }
      133.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ReclaimApiVerificationResponse.fromList(it)
        }
      }
      134.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ClientProviderInformationOverride.fromList(it)
        }
      }
      135.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ClientFeatureOverrides.fromList(it)
        }
      }
      136.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ClientLogConsumerOverride.fromList(it)
        }
      }
      137.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ClientReclaimSessionManagementOverride.fromList(it)
        }
      }
      138.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ClientReclaimAppInfoOverride.fromList(it)
        }
      }
      139.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ReclaimSessionIdentityUpdate.fromList(it)
        }
      }
      140.toByte() -> {
        return (readValue(buffer) as? List<Any?>)?.let {
          ReclaimApiVerificationOptions.fromList(it)
        }
      }
      else -> super.readValueOfType(type, buffer)
    }
  }
  override fun writeValue(stream: ByteArrayOutputStream, value: Any?)   {
    when (value) {
      is ReclaimApiVerificationExceptionType -> {
        stream.write(129)
        writeValue(stream, value.raw)
      }
      is ReclaimSessionStatus -> {
        stream.write(130)
        writeValue(stream, value.raw)
      }
      is ReclaimApiVerificationRequest -> {
        stream.write(131)
        writeValue(stream, value.toList())
      }
      is ReclaimApiVerificationException -> {
        stream.write(132)
        writeValue(stream, value.toList())
      }
      is ReclaimApiVerificationResponse -> {
        stream.write(133)
        writeValue(stream, value.toList())
      }
      is ClientProviderInformationOverride -> {
        stream.write(134)
        writeValue(stream, value.toList())
      }
      is ClientFeatureOverrides -> {
        stream.write(135)
        writeValue(stream, value.toList())
      }
      is ClientLogConsumerOverride -> {
        stream.write(136)
        writeValue(stream, value.toList())
      }
      is ClientReclaimSessionManagementOverride -> {
        stream.write(137)
        writeValue(stream, value.toList())
      }
      is ClientReclaimAppInfoOverride -> {
        stream.write(138)
        writeValue(stream, value.toList())
      }
      is ReclaimSessionIdentityUpdate -> {
        stream.write(139)
        writeValue(stream, value.toList())
      }
      is ReclaimApiVerificationOptions -> {
        stream.write(140)
        writeValue(stream, value.toList())
      }
      else -> super.writeValue(stream, value)
    }
  }
}


/**
 * Apis implemented by the Reclaim module for use by the host.
 *
 * Generated class from Pigeon that represents Flutter messages that can be called from Kotlin.
 */
class ReclaimModuleApi(private val binaryMessenger: BinaryMessenger, private val messageChannelSuffix: String = "") {
  companion object {
    /** The codec used by ReclaimModuleApi. */
    val codec: MessageCodec<Any?> by lazy {
      MessagesPigeonCodec()
    }
  }
  fun startVerification(requestArg: ReclaimApiVerificationRequest, callback: (Result<ReclaimApiVerificationResponse>) -> Unit)
{
    val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
    val channelName = "dev.flutter.pigeon.reclaim_verifier_module.ReclaimModuleApi.startVerification$separatedMessageChannelSuffix"
    val channel = BasicMessageChannel<Any?>(binaryMessenger, channelName, codec)
    channel.send(listOf(requestArg)) {
      if (it is List<*>) {
        if (it.size > 1) {
          callback(Result.failure(FlutterError(it[0] as String, it[1] as String, it[2] as String?)))
        } else if (it[0] == null) {
          callback(Result.failure(FlutterError("null-error", "Flutter api returned null value for non-null return value.", "")))
        } else {
          val output = it[0] as ReclaimApiVerificationResponse
          callback(Result.success(output))
        }
      } else {
        callback(Result.failure(createConnectionError(channelName)))
      } 
    }
  }
  fun startVerificationFromUrl(urlArg: String, callback: (Result<ReclaimApiVerificationResponse>) -> Unit)
{
    val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
    val channelName = "dev.flutter.pigeon.reclaim_verifier_module.ReclaimModuleApi.startVerificationFromUrl$separatedMessageChannelSuffix"
    val channel = BasicMessageChannel<Any?>(binaryMessenger, channelName, codec)
    channel.send(listOf(urlArg)) {
      if (it is List<*>) {
        if (it.size > 1) {
          callback(Result.failure(FlutterError(it[0] as String, it[1] as String, it[2] as String?)))
        } else if (it[0] == null) {
          callback(Result.failure(FlutterError("null-error", "Flutter api returned null value for non-null return value.", "")))
        } else {
          val output = it[0] as ReclaimApiVerificationResponse
          callback(Result.success(output))
        }
      } else {
        callback(Result.failure(createConnectionError(channelName)))
      } 
    }
  }
  fun setOverrides(providerArg: ClientProviderInformationOverride?, featureArg: ClientFeatureOverrides?, logConsumerArg: ClientLogConsumerOverride?, sessionManagementArg: ClientReclaimSessionManagementOverride?, appInfoArg: ClientReclaimAppInfoOverride?, capabilityAccessTokenArg: String?, callback: (Result<Unit>) -> Unit)
{
    val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
    val channelName = "dev.flutter.pigeon.reclaim_verifier_module.ReclaimModuleApi.setOverrides$separatedMessageChannelSuffix"
    val channel = BasicMessageChannel<Any?>(binaryMessenger, channelName, codec)
    channel.send(listOf(providerArg, featureArg, logConsumerArg, sessionManagementArg, appInfoArg, capabilityAccessTokenArg)) {
      if (it is List<*>) {
        if (it.size > 1) {
          callback(Result.failure(FlutterError(it[0] as String, it[1] as String, it[2] as String?)))
        } else {
          callback(Result.success(Unit))
        }
      } else {
        callback(Result.failure(createConnectionError(channelName)))
      } 
    }
  }
  fun clearAllOverrides(callback: (Result<Unit>) -> Unit)
{
    val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
    val channelName = "dev.flutter.pigeon.reclaim_verifier_module.ReclaimModuleApi.clearAllOverrides$separatedMessageChannelSuffix"
    val channel = BasicMessageChannel<Any?>(binaryMessenger, channelName, codec)
    channel.send(null) {
      if (it is List<*>) {
        if (it.size > 1) {
          callback(Result.failure(FlutterError(it[0] as String, it[1] as String, it[2] as String?)))
        } else {
          callback(Result.success(Unit))
        }
      } else {
        callback(Result.failure(createConnectionError(channelName)))
      } 
    }
  }
  fun setVerificationOptions(optionsArg: ReclaimApiVerificationOptions?, callback: (Result<Unit>) -> Unit)
{
    val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
    val channelName = "dev.flutter.pigeon.reclaim_verifier_module.ReclaimModuleApi.setVerificationOptions$separatedMessageChannelSuffix"
    val channel = BasicMessageChannel<Any?>(binaryMessenger, channelName, codec)
    channel.send(listOf(optionsArg)) {
      if (it is List<*>) {
        if (it.size > 1) {
          callback(Result.failure(FlutterError(it[0] as String, it[1] as String, it[2] as String?)))
        } else {
          callback(Result.success(Unit))
        }
      } else {
        callback(Result.failure(createConnectionError(channelName)))
      } 
    }
  }
  fun ping(callback: (Result<Boolean>) -> Unit)
{
    val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
    val channelName = "dev.flutter.pigeon.reclaim_verifier_module.ReclaimModuleApi.ping$separatedMessageChannelSuffix"
    val channel = BasicMessageChannel<Any?>(binaryMessenger, channelName, codec)
    channel.send(null) {
      if (it is List<*>) {
        if (it.size > 1) {
          callback(Result.failure(FlutterError(it[0] as String, it[1] as String, it[2] as String?)))
        } else if (it[0] == null) {
          callback(Result.failure(FlutterError("null-error", "Flutter api returned null value for non-null return value.", "")))
        } else {
          val output = it[0] as Boolean
          callback(Result.success(output))
        }
      } else {
        callback(Result.failure(createConnectionError(channelName)))
      } 
    }
  }
}
/**
 * Apis implemented by the host using the Reclaim module.
 *
 * Generated interface from Pigeon that represents a handler of messages from Flutter.
 */
interface ReclaimHostOverridesApi {
  fun onLogs(logJsonString: String, callback: (Result<Unit>) -> Unit)
  fun createSession(appId: String, providerId: String, sessionId: String, callback: (Result<Boolean>) -> Unit)
  fun updateSession(sessionId: String, status: ReclaimSessionStatus, callback: (Result<Boolean>) -> Unit)
  fun logSession(appId: String, providerId: String, sessionId: String, logType: String, callback: (Result<Unit>) -> Unit)
  fun onSessionIdentityUpdate(update: ReclaimSessionIdentityUpdate?, callback: (Result<Unit>) -> Unit)
  fun fetchProviderInformation(appId: String, providerId: String, sessionId: String, signature: String, timestamp: String, callback: (Result<String>) -> Unit)

  companion object {
    /** The codec used by ReclaimHostOverridesApi. */
    val codec: MessageCodec<Any?> by lazy {
      MessagesPigeonCodec()
    }
    /** Sets up an instance of `ReclaimHostOverridesApi` to handle messages through the `binaryMessenger`. */
    @JvmOverloads
    fun setUp(binaryMessenger: BinaryMessenger, api: ReclaimHostOverridesApi?, messageChannelSuffix: String = "") {
      val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.reclaim_verifier_module.ReclaimHostOverridesApi.onLogs$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { message, reply ->
            val args = message as List<Any?>
            val logJsonStringArg = args[0] as String
            api.onLogs(logJsonStringArg) { result: Result<Unit> ->
              val error = result.exceptionOrNull()
              if (error != null) {
                reply.reply(wrapError(error))
              } else {
                reply.reply(wrapResult(null))
              }
            }
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.reclaim_verifier_module.ReclaimHostOverridesApi.createSession$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { message, reply ->
            val args = message as List<Any?>
            val appIdArg = args[0] as String
            val providerIdArg = args[1] as String
            val sessionIdArg = args[2] as String
            api.createSession(appIdArg, providerIdArg, sessionIdArg) { result: Result<Boolean> ->
              val error = result.exceptionOrNull()
              if (error != null) {
                reply.reply(wrapError(error))
              } else {
                val data = result.getOrNull()
                reply.reply(wrapResult(data))
              }
            }
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.reclaim_verifier_module.ReclaimHostOverridesApi.updateSession$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { message, reply ->
            val args = message as List<Any?>
            val sessionIdArg = args[0] as String
            val statusArg = args[1] as ReclaimSessionStatus
            api.updateSession(sessionIdArg, statusArg) { result: Result<Boolean> ->
              val error = result.exceptionOrNull()
              if (error != null) {
                reply.reply(wrapError(error))
              } else {
                val data = result.getOrNull()
                reply.reply(wrapResult(data))
              }
            }
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.reclaim_verifier_module.ReclaimHostOverridesApi.logSession$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { message, reply ->
            val args = message as List<Any?>
            val appIdArg = args[0] as String
            val providerIdArg = args[1] as String
            val sessionIdArg = args[2] as String
            val logTypeArg = args[3] as String
            api.logSession(appIdArg, providerIdArg, sessionIdArg, logTypeArg) { result: Result<Unit> ->
              val error = result.exceptionOrNull()
              if (error != null) {
                reply.reply(wrapError(error))
              } else {
                reply.reply(wrapResult(null))
              }
            }
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.reclaim_verifier_module.ReclaimHostOverridesApi.onSessionIdentityUpdate$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { message, reply ->
            val args = message as List<Any?>
            val updateArg = args[0] as ReclaimSessionIdentityUpdate?
            api.onSessionIdentityUpdate(updateArg) { result: Result<Unit> ->
              val error = result.exceptionOrNull()
              if (error != null) {
                reply.reply(wrapError(error))
              } else {
                reply.reply(wrapResult(null))
              }
            }
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.reclaim_verifier_module.ReclaimHostOverridesApi.fetchProviderInformation$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { message, reply ->
            val args = message as List<Any?>
            val appIdArg = args[0] as String
            val providerIdArg = args[1] as String
            val sessionIdArg = args[2] as String
            val signatureArg = args[3] as String
            val timestampArg = args[4] as String
            api.fetchProviderInformation(appIdArg, providerIdArg, sessionIdArg, signatureArg, timestampArg) { result: Result<String> ->
              val error = result.exceptionOrNull()
              if (error != null) {
                reply.reply(wrapError(error))
              } else {
                val data = result.getOrNull()
                reply.reply(wrapResult(data))
              }
            }
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
    }
  }
}
/** Generated interface from Pigeon that represents a handler of messages from Flutter. */
interface ReclaimHostVerificationApi {
  fun fetchAttestorAuthenticationRequest(reclaimHttpProvider: Map<Any?, Any?>, callback: (Result<String>) -> Unit)

  companion object {
    /** The codec used by ReclaimHostVerificationApi. */
    val codec: MessageCodec<Any?> by lazy {
      MessagesPigeonCodec()
    }
    /** Sets up an instance of `ReclaimHostVerificationApi` to handle messages through the `binaryMessenger`. */
    @JvmOverloads
    fun setUp(binaryMessenger: BinaryMessenger, api: ReclaimHostVerificationApi?, messageChannelSuffix: String = "") {
      val separatedMessageChannelSuffix = if (messageChannelSuffix.isNotEmpty()) ".$messageChannelSuffix" else ""
      run {
        val channel = BasicMessageChannel<Any?>(binaryMessenger, "dev.flutter.pigeon.reclaim_verifier_module.ReclaimHostVerificationApi.fetchAttestorAuthenticationRequest$separatedMessageChannelSuffix", codec)
        if (api != null) {
          channel.setMessageHandler { message, reply ->
            val args = message as List<Any?>
            val reclaimHttpProviderArg = args[0] as Map<Any?, Any?>
            api.fetchAttestorAuthenticationRequest(reclaimHttpProviderArg) { result: Result<String> ->
              val error = result.exceptionOrNull()
              if (error != null) {
                reply.reply(wrapError(error))
              } else {
                val data = result.getOrNull()
                reply.reply(wrapResult(data))
              }
            }
          }
        } else {
          channel.setMessageHandler(null)
        }
      }
    }
  }
}
