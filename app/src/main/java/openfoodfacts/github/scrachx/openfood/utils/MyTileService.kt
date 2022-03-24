package openfoodfacts.github.scrachx.openfood.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*

@RequiresApi(Build.VERSION_CODES.N)
class MyTileService : TileService() {

    private var job = Job()
    private val applicationScope = CoroutineScope(Dispatchers.Main + job)


    override fun onTileAdded() {
        super.onTileAdded()

        // Update state
        qsTile.state = Tile.STATE_INACTIVE

        // Update looks
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (qsTile.state == Tile.STATE_INACTIVE) {
            // Turn on
            qsTile.state = Tile.STATE_ACTIVE
            startVibrating() // TODO
        } else {
            // Turn off
            qsTile.state = Tile.STATE_INACTIVE
            stopVibrating() // TODO
        }

        // Update looks
        qsTile.updateTile()
    }

    @SuppressLint("ServiceCast")
    private fun startVibrating() {
        applicationScope.launch {
            while (qsTile.state == Tile.STATE_ACTIVE) {
                (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
                    .vibrate(1000) // Vibrate for a second

                // Wait for a second before vibrating again
                delay(1000)
            }
        }
    }

    private fun stopVibrating() {
        (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
            .cancel()
    }
}