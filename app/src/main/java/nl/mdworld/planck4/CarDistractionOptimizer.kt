package nl.mdworld.planck4

import androidx.car.app.CarContext
import androidx.car.app.constraints.ConstraintManager

/**
 * Utility class to help with car distraction optimization
 */
object CarDistractionOptimizer {

    /**
     * Check if the car is currently in driving mode
     */
    fun isDriving(carContext: CarContext): Boolean {
        val constraintManager = carContext.getCarService(CarContext.CONSTRAINT_SERVICE) as ConstraintManager
        // When driving, the system limits the number of list items to 6 or fewer
        return constraintManager.getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_LIST) <= 6
    }

    /**
     * Get the maximum number of items allowed in a list
     */
    fun getMaxListItems(carContext: CarContext): Int {
        val constraintManager = carContext.getCarService(CarContext.CONSTRAINT_SERVICE) as ConstraintManager
        return constraintManager.getContentLimit(ConstraintManager.CONTENT_LIMIT_TYPE_LIST)
    }

    /**
     * Check if a feature should be available based on driving state
     */
    fun isFeatureAvailable(carContext: CarContext, requiresParking: Boolean): Boolean {
        return if (requiresParking) {
            !isDriving(carContext)
        } else {
            true
        }
    }

    /**
     * Get appropriate title suffix based on driving state
     */
    fun getTitleSuffix(carContext: CarContext, parkingOnlyFeature: Boolean): String {
        return if (parkingOnlyFeature && !isDriving(carContext)) {
            " (Parking Mode)"
        } else if (isDriving(carContext)) {
            " (Driving Mode)"
        } else {
            ""
        }
    }
}
