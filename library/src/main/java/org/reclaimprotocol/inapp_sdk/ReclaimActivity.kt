package org.reclaimprotocol.inapp_sdk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.BinaryMessenger

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

        private fun withCachedEngine(cachedEngineId: String): CachedEngineIntentBuilder? {
            val engine = FlutterEngineCache.getInstance().get(cachedEngineId)
            if (engine == null) {
                return null
            }

            return CachedEngineIntentBuilder(ReclaimActivity::class.java, cachedEngineId)
        }

        private fun getEngine(cachedEngineId: String): FlutterEngine? {
            return FlutterEngineCache.getInstance().get(cachedEngineId)
        }

        private fun hasEngine(cachedEngineId: String): Boolean {
            return getEngine(cachedEngineId) != null
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
            if (hasEngine(CACHED_ENGINE_ID)) {
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
            return getEngine(CACHED_ENGINE_ID)!!
        }

        public fun requireBinaryMessenger(context: Context): BinaryMessenger {
            val engine = requireEngine(context)
            return engine.dartExecutor.binaryMessenger
        }

        /**
         * Starts the ReclaimActivity.
         * This method will pre-warm the FlutterEngine if it is not already cached and then starts the ReclaimActivity.
         */
        public fun start(context: Context) {
            preWarm(context)
            val engineIntentBuilder = withCachedEngine(CACHED_ENGINE_ID)!!
            val intent = engineIntentBuilder.build(context)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        /**
         * Closes all instances of the ReclaimActivity.
         */
        public fun closeAll() {
            for (instance in instances) {
                instance.finish()
            }
        }

        private val instances: MutableList<ReclaimActivity> = mutableListOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReclaimActivity.instances.add(this)
    }

    override fun finish() {
        super.finish()
        ReclaimActivity.instances.remove(this)
    }
}