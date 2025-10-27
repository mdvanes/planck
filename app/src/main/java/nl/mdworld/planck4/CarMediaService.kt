package nl.mdworld.planck4

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Car media entry point service (renamed from CarAppActivity for Play Console expectations).
 */
class CarMediaService : CarAppService() {
    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    override fun onCreateSession(): Session = MediaCarSession()
}

