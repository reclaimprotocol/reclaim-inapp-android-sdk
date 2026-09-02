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

    private var staleRestoration: Boolean? = null

    /**
     * Whether Android restored this activity with an intent naming a cached FlutterEngine that no
     * longer exists in this process.
     *
     * The engine cache and [attemptIds] are both process local, so a missing engine always means
     * the verification this activity was launched for is gone: the process was killed and Android
     * is restoring the activity stack. [onCreate] finishes such an activity, but
     * [FlutterActivity.onCreate] resolves the cached engine before that and throws, so the cached
     * engine id must not be reported on this path.
     *
     * Computed once, on the first call from [FlutterActivity.onCreate], and then shared by
     * [getCachedEngineId], [provideFlutterEngine] and [shouldDestroyEngineWithHost] so that all
     * three agree for the lifetime of this activity.
     */
    private fun isStaleRestoration(): Boolean {
        val known = staleRestoration
        if (known != null) return known
        val value = super.getCachedEngineId() != null && !hasEngine()
        staleRestoration = value
        Log.i("ReclaimActivity", "isStaleRestoration: $value")
        return value
    }

    override fun getCachedEngineId(): String? {
        if (isStaleRestoration()) {
            // Reporting the id would make FlutterActivity throw, because the engine it names died
            // with the process. Returning null sends Flutter to provideFlutterEngine instead.
            return null
        }
        return super.getCachedEngineId()
    }

    override fun provideFlutterEngine(context: Context): FlutterEngine? {
        if (isStaleRestoration()) {
            // A throwaway engine for an activity that is about to finish. It is deliberately kept
            // out of the FlutterEngineCache: the next verification pre-warms its own engine and
            // applies the host's overrides to it, and this one is destroyed with the activity by
            // shouldDestroyEngineWithHost below. onCreate finishes this activity, so Flutter never
            // reaches onStart and never runs a Dart entrypoint on this engine.
            return FlutterEngine(context)
        }
        return super.provideFlutterEngine(context)
    }

    override fun shouldDestroyEngineWithHost(): Boolean {
        if (isStaleRestoration()) {
            return true
        }
        return super.shouldDestroyEngineWithHost()
    }

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

    override fun onDestroy() {
        super.onDestroy()
        // The system can destroy this activity without finish() being called, so clean up here
        // too. Otherwise instances keeps references to destroyed activities and closeAll() calls
        // finish() on them.
        instances.remove(this)
    }
}