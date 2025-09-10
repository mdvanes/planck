package nl.mdworld.planck4

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat

class MediaCarScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val playlistsAction = Action.Builder()
            .setTitle("Playlists")
            .setOnClickListener {
                screenManager.push(PlaylistsCarScreen(carContext))
            }
            .build()

        val radioAction = Action.Builder()
            .setTitle("Radio")
            .setOnClickListener {
                screenManager.push(RadioCarScreen(carContext))
            }
            .build()

        return ListTemplate.Builder()
            .setSingleList(
                ItemList.Builder()
                    .addItem(
                        Row.Builder()
                            .setTitle("My Playlists")
                            .setBrowsable(true)
                            .setOnClickListener {
                                screenManager.push(PlaylistsCarScreen(carContext))
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle("Radio")
                            .setBrowsable(true)
                            .setOnClickListener {
                                screenManager.push(RadioCarScreen(carContext))
                            }
                            .build()
                    )
                    .build()
            )
            .setHeader(Header.Builder().setTitle("Planck Media Player").build())
            .build()
    }
}
