package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient

class ScanHistoryHolder(itemView: View, private val context: Context) : RecyclerView.ViewHolder(itemView) {
    val txtDate: TextView = itemView.findViewById(R.id.dateView)
    val txtTitle: TextView = itemView.findViewById(R.id.titleHistory)
    val txtBarcode: TextView = itemView.findViewById(R.id.barcodeHistory)
    val txtProductDetails: TextView = itemView.findViewById(R.id.productDetailsHistory)
    val imgProduct: ImageView = itemView.findViewById(R.id.imgHistoryProduct)
    val imgNutritionGrade: ImageView = itemView.findViewById(R.id.nutritionGradeImage)
    val historyImageProgressbar: ProgressBar = itemView.findViewById(R.id.historyImageProgressbar)

    init {
        itemView.setOnClickListener { v: View ->
            val cm = v.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
            if (isConnected) {
                OpenFoodAPIClient(context).openProduct(txtBarcode.text.toString(), (v.context as Activity))
            } else {
                Toast.makeText(context, R.string.history_network_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}