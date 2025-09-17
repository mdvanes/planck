package nl.mdworld.planck4

import android.util.Log
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class ClusterCarAppService : CarAppService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("ClusterCarAppService", "Service created")
    }

    override fun createHostValidator(): HostValidator =
        HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = ClusterSession()
}
