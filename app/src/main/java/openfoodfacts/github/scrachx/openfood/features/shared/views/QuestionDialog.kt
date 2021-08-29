/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features.shared.views

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.DialogProductQuestionBinding

class QuestionDialog(context: Context) {
    private val binding = DialogProductQuestionBinding.inflate(LayoutInflater.from(context))
    private val dialog = Dialog(context, R.style.QuestionDialog).apply {
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
    }

    private val mAmbiguityFeedbackIcon = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.ic_help_black_24dp,
        context.theme
    )
    private val mAmbiguityFeedbackText = context.resources.getString(R.string.product_question_ambiguous_response)

    private val mNegativeFeedbackIcon = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.ic_cancel_black_24dp,
        context.theme
    )
    private val mNegativeFeedbackText = context.resources.getString(R.string.product_question_negative_response)

    private val mPositiveFeedbackIcon = ResourcesCompat.getDrawable(
        context.resources,
        R.drawable.ic_check_circle_black_24dp,
        context.theme
    )
    private val mPositiveFeedbackText = context.resources.getString(R.string.product_question_positive_response)


    @ColorRes
    var backgroundColor = 0
    var question: String? = null
    var value: String? = null


    var onPositiveFeedback: ((QuestionDialog) -> Unit)? = null
    var onNegativeFeedback: ((QuestionDialog) -> Unit)? = null
    var onAmbiguityFeedback: ((QuestionDialog) -> Unit)? = null
    var onCancelListener: ((QuestionDialog) -> Unit)? = null

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            dialog.window?.setLayout(
                (context.resources.displayMetrics.widthPixels * 0.90).toInt(),
                (context.resources.displayMetrics.heightPixels * 0.50).toInt()
            )
        }
    }


    private fun initiateListeners() {
        binding.postiveFeedbackLayout.setOnClickListener { onPositiveFeedback?.invoke(this) }
        binding.negativeFeedbackLayout.setOnClickListener { onNegativeFeedback?.invoke(this) }
        binding.ambiguityFeedbackLayout.setOnClickListener { onAmbiguityFeedback?.invoke(this) }
        dialog.setOnCancelListener { onCancelListener?.invoke(this) }
    }

    fun show() {
        initiateListeners()
        binding.reviewQuestion.text = question
        binding.reviewValue.text = value
        binding.positiveFeedbackText.text = mPositiveFeedbackText
        binding.postiveFeedbackIcon.setImageDrawable(mPositiveFeedbackIcon)
        binding.negativeFeedbackText.text = mNegativeFeedbackText
        binding.negativeFeedbackIcon.setImageDrawable(mNegativeFeedbackIcon)
        binding.ambiguityFeedbackText.text = mAmbiguityFeedbackText
        binding.ambiguityFeedbackIcon.setImageDrawable(mAmbiguityFeedbackIcon)
        binding.feedbackBodyLayout.setBackgroundResource(backgroundColor)
        dialog.show()
    }


    fun dismiss() = dialog.dismiss()
}

inline fun showQuestionDialog(context: Context, dialogAction: QuestionDialog.() -> Unit) {
    val dialog = QuestionDialog(context)
    dialog.dialogAction()
    dialog.show()
}
