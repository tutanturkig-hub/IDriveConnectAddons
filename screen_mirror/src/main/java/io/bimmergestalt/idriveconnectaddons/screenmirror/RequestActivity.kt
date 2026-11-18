package io.bimmergestalt.idriveconnectaddons.screenmirror

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle

class RequestActivity: Activity() {
    companion object {
        const val PROJECTION_PERMISSION_CODE = 8345
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationService.startNotification(applicationContext)
        requestPermission()
    }

    private fun requestPermission() {
        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        val captureIntent = if (android.os.Build.VERSION.SDK_INT >= 34) {
            try {
                val configClass = Class.forName("android.media.projection.MediaProjectionConfig")
                val config = configClass.getMethod("createConfigForDefaultDisplay").invoke(null)
                projectionManager.javaClass
                    .getMethod("createScreenCaptureIntent", configClass)
                    .invoke(projectionManager, config) as Intent
            } catch (e: Exception) {
                projectionManager.createScreenCaptureIntent()
            }
        } else {
            projectionManager.createScreenCaptureIntent()
        }
        startActivityForResult(captureIntent, PROJECTION_PERMISSION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        if (requestCode == PROJECTION_PERMISSION_CODE && resultCode == RESULT_OK && data != null) {
            ScreenMirrorProvider.projection = projectionManager.getMediaProjection(resultCode, data)
        } else {
            ScreenMirrorProvider.projection = null
            NotificationService.startNotification(applicationContext)
        }
        finish()
    }
}
