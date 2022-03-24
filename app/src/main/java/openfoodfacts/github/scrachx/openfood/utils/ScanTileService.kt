package openfoodfacts.github.scrachx.openfood.utils

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import openfoodfacts.github.scrachx.openfood.features.MainActivity

@RequiresApi(Build.VERSION_CODES.N)
class ScanTileService : TileService() {

    override fun onTileAdded() {
        super.onTileAdded()

        // Update state
        qsTile.state = Tile.STATE_ACTIVE

        // Update looks
        qsTile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = "SCAN"
        startActivity(intent)
    }
}