package nl.mdworld.planck4

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class RadioCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val backAction = Action.Builder()
            .setTitle("Back")
            .setOnClickListener {
                screenManager.pop()
            }
            .build()

        return ListTemplate.Builder()
            .setSingleList(
                ItemList.Builder()
                    .addItem(
                        Row.Builder()
                            .setTitle("Radio 2")
                            .setOnClickListener {
                                // Handle radio station selection
                                // This could integrate with your existing radio functionality
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle("NPO Radio 1")
                            .setOnClickListener {
                                // Handle radio station selection
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle("3FM")
                            .setOnClickListener {
                                // Handle radio station selection
                            }
                            .build()
                    )
                    .build()
            )
            .setHeader(
                Header.Builder()
                    .setTitle("Radio Stations")
                    .setStartHeaderAction(backAction)
                    .build()
            )
            .build()
    }
}
