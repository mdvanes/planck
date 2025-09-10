package nl.mdworld.planck4

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template

class PlaylistsCarScreen(carContext: CarContext) : Screen(carContext) {

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
                            .setTitle("My Music")
                            .setOnClickListener {
                                // Handle playlist selection
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle("Favorites")
                            .setOnClickListener {
                                // Handle playlist selection
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle("Recently Played")
                            .setOnClickListener {
                                // Handle playlist selection
                            }
                            .build()
                    )
                    .build()
            )
            .setHeader(
                Header.Builder()
                    .setTitle("Playlists")
                    .setStartHeaderAction(backAction)
                    .build()
            )
            .build()
    }
}
