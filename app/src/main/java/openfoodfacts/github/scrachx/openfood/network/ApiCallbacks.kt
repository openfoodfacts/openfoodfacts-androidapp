package openfoodfacts.github.scrachx.openfood.network

import openfoodfacts.github.scrachx.openfood.models.Search

class ApiCallbacks {

    fun interface OnStoreCallback {
        fun onStoreResponse(value: Boolean, store: Search?)
    }

    fun interface OnEditImageCallback {
        fun onEditResponse(value: Boolean, response: String?)
    }

    fun interface OnContributorCallback {
        fun onContributorResponse(value: Boolean, contributor: Search?)
    }
}