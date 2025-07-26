package org.reclaimprotocol.inapp_sdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.BinaryMessenger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The Reclaim Activity where Reclaim's verification and proof generation takes place.
 *
 * For using this activity in an android application, add the following in under application in your AndroidManifest.xml
 * ```xml
 * <activity
 *  android:name="org.reclaimprotocol.inapp_sdk.ReclaimActivity"
 *  android:theme="@style/Theme.ReclaimInAppSdk.LaunchTheme"
 *  android:configChanges="orientation|keyboardHidden|keyboard|screenSize|locale|layoutDirection|fontScale|screenLayout|density|uiMode"
 *  android:hardwareAccelerated="true"
 *  android:windowSoftInputMode="adjustResize"
 *  />
 * ```
 */
public class ReclaimActivity : FlutterActivity() {
    companion object {
        private const val CACHED_ENGINE_ID = "reclaim_flutter_engine"

        private fun withCachedEngineIntentBuilder(): CachedEngineIntentBuilder? {
            val engine = FlutterEngineCache.getInstance().get(CACHED_ENGINE_ID)
            if (engine == null) {
                return null
            }

            return CachedEngineIntentBuilder(ReclaimActivity::class.java, CACHED_ENGINE_ID)
        }

        private fun getEngine(): FlutterEngine? {
            return FlutterEngineCache.getInstance().get(CACHED_ENGINE_ID)
        }

        private fun hasEngine(): Boolean {
            return getEngine() != null
        }

        private fun setupEngine(engine: FlutterEngine) {

            // Start executing Dart code to pre-warm the FlutterEngine.
            engine.dartExecutor.executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
            )
            // Cache the FlutterEngine to be used by FlutterActivity.
            FlutterEngineCache.getInstance().put(CACHED_ENGINE_ID, engine)
        }

        /**
         * This instantiates and caches the FlutterEngine used by the ReclaimActivity.
         * Calling this method in advance is recommended to avoid the first launch of the ReclaimActivity from being slow.
         */
        public fun preWarm(context: Context) {
            if (hasEngine()) {
                return
            }
            // Instantiate a FlutterEngine.
            setupEngine(FlutterEngine(context))
        }

        /**
         * Returns the FlutterEngine used by the ReclaimActivity.
         * This method will pre-warm the FlutterEngine if it is not already cached.
         */
        public fun requireEngine(context: Context): FlutterEngine {
            preWarm(context)
            return getEngine()!!
        }

        public fun requireBinaryMessenger(context: Context): BinaryMessenger {
            val engine = requireEngine(context)
            return engine.dartExecutor.binaryMessenger
        }

        /**
         * Starts the ReclaimActivity.
         * This method will pre-warm the FlutterEngine if it is not already cached and then starts the ReclaimActivity.
         */
        @OptIn(ExperimentalUuidApi::class)
        public fun start(context: Context) {
            Log.i("ReclaimActivity", "Starting ReclaimActivity")
            preWarm(context)
            val engineIntentBuilder = withCachedEngineIntentBuilder()!!
            val intent = engineIntentBuilder.build(context)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val attemptId = Uuid.random().toString()
            attemptIds.add(attemptId)
            intent.putExtra(INTENT_EXTRA_ATTEMPT_ID, attemptId)
            context.startActivity(intent)
        }

        /**
         * Closes all instances of the ReclaimActivity.
         */
        public fun closeAll() {
            Log.i("ReclaimActivity", "Closing all instances (${instances.count()}) of ReclaimActivity")
            attemptIds.clear()
            for (instance in instances) {
                instance.finish()
            }
        }

        private val instances: MutableList<ReclaimActivity> = mutableListOf()
        private val attemptIds: MutableSet<String> = mutableSetOf()
        private const val INTENT_EXTRA_ATTEMPT_ID = "attempt_id"
    }

    var attemptId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("ReclaimActivity", "ReclaimActivity onCreate")
        instances.add(this)
        attemptId = intent.getStringExtra(INTENT_EXTRA_ATTEMPT_ID)
        val hasAttempt = attemptId != null && attemptIds.contains(attemptId)
        Log.i("ReclaimActivity", "Attempt id: $attemptId, hasAttempt: ${hasAttempt}")
        if (!hasAttempt) {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        if (attemptId != null) {
            attemptIds.remove(attemptId)
        }
        instances.remove(this)
    }
}