package openfoodfacts.github.scrachx.openfood.features.product.view.contributors

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentContributorsBinding
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import openfoodfacts.github.scrachx.openfood.utils.requireProductState
import java.text.SimpleDateFormat
import java.util.*

/*
* Created by prajwalm on 14/04/18.
*/
/**
 * @see R.layout.fragment_contributors
 */
class ContributorsFragment : BaseFragment() {
    private var _binding: FragmentContributorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var productState: ProductState

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContributorsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshView(this.requireProductState())
    }

    override fun refreshView(productState: ProductState) {
        super.refreshView(productState)
        this.productState = productState
        val product = this.productState.product!!

        if (!product.creator.isNullOrBlank()) {
            val createdDate = getDateTime(product.createdDateTime!!)
            val creatorTxt = getString(R.string.creator_history, createdDate.first, createdDate.second, product.creator)
            binding.creatorTxt.movementMethod = LinkMovementMethod.getInstance()
            binding.creatorTxt.text = creatorTxt
        } else {
            binding.creatorTxt.visibility = View.INVISIBLE
        }

        if (!product.lastModifiedBy.isNullOrBlank()) {
            val lastEditDate = getDateTime(product.lastModifiedTime!!)
            val editorTxt = getString(R.string.last_editor_history, lastEditDate.first, lastEditDate.second, product.lastModifiedBy)
            binding.lastEditorTxt.movementMethod = LinkMovementMethod.getInstance()
            binding.lastEditorTxt.text = editorTxt
        } else {
            binding.lastEditorTxt.visibility = View.INVISIBLE
        }

        if (product.editors.isNotEmpty()) {
            val otherEditorsTxt = getString(R.string.other_editors)
            binding.otherEditorsTxt.movementMethod = LinkMovementMethod.getInstance()
            binding.otherEditorsTxt.text = "$otherEditorsTxt "
            product.editors.forEach { editor ->
                binding.otherEditorsTxt.append(getContributorsTag(editor).subSequence(0, editor.length))
                binding.otherEditorsTxt.append(", ")
            }
            binding.otherEditorsTxt.append(getContributorsTag(product.editors[product.editors.size - 1]))
        } else {
            binding.otherEditorsTxt.visibility = View.INVISIBLE
        }

        if (product.statesTags.isNotEmpty()) {
            binding.statesTxt.movementMethod = LinkMovementMethod.getInstance()
            binding.statesTxt.text = ""
            product.statesTags.forEach { stateTag ->
                binding.statesTxt.append(getStatesTag(stateTag.split(":")[1]))
                binding.statesTxt.append("\n ")
            }
        }
    }

    /**
     * Get date and time in MMMM dd, yyyy and HH:mm:ss a format
     *
     * @param dateTime date and time in miliseconds
     */
    private fun getDateTime(dateTime: String): Pair<String, String> {
        val unixSeconds = dateTime.toLong()
        val date = Date(unixSeconds * 1000L)
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val sdf2 = SimpleDateFormat("HH:mm:ss a", Locale.getDefault())
        sdf2.timeZone = TimeZone.getTimeZone("CET")
        return sdf.format(date) to sdf2.format(date)
    }

    private fun getContributorsTag(contributor: String): CharSequence {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                start(context!!, SearchType.CONTRIBUTOR, contributor)
            }
        }
        return SpannableStringBuilder().apply {
            append(contributor)
            setSpan(clickableSpan, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(" ")
        }
    }

    private fun getStatesTag(state: String): CharSequence {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                start(requireContext(), SearchType.STATE, state)
            }
        }
        return SpannableStringBuilder().apply {
            append(state)
            setSpan(clickableSpan, 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(" ")
        }
    }

    companion object {
        fun newInstance(productState: ProductState) = ContributorsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ProductViewActivity.STATE_KEY, productState)
            }
        }
    }
}