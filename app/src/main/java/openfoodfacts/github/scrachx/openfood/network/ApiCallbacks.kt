package openfoodfacts.github.scrachx.openfood.network

class ApiCallbacks {

    fun interface OnEditImageCallback {
        fun onEditResponse(value: Boolean, response: String?)
    }

}