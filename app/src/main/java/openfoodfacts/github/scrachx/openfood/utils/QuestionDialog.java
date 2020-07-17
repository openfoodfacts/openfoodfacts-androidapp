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

package openfoodfacts.github.scrachx.openfood.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import openfoodfacts.github.scrachx.openfood.R;

public class QuestionDialog {
    private final Drawable mAmbiguityFeedbackIcon;
    private final String mAmbiguityFeedbackText;
    private final Context mContext;
    @ColorRes
    private int mBackgroundColor;
    private String mQuestion;
    private String mValue;
    private ImageView titleImageView;
    private TextView reviewQuestionTextView;
    private TextView reviewValueTextView;
    private LinearLayout positiveFeedbackLayout;
    private LinearLayout negativeFeedbackLayout;
    private LinearLayout ambiguityFeedbackLayout;
    private LinearLayout feedbackBodyLayout;
    private TextView positiveFeedbackTextView;
    private TextView negativeFeedbackTextView;
    private TextView ambiguityFeedbackTextView;
    private ImageView positiveFeedbackIconView;
    private ImageView negativeFeedbackIconView;
    private ImageView ambiguityFeedbackIconView;
    private final Dialog mDialog;
    private final Drawable mIcon;
    @ColorRes
    private final int mIconColor;
    private final Drawable mNegativeFeedbackIcon;
    private final String mNegativeFeedbackText;
    private final Drawable mPositiveFeedbackIcon;
    private final String mPositiveFeedbackText;
    private QuestionActionListeners mReviewActionsListener;

    public QuestionDialog(Context mContext) {
        this.mContext = mContext;

        mDialog = new Dialog(mContext, R.style.QuestionDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_product_question);

        final Resources resources = mContext.getResources();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int width = (int) (resources.getDisplayMetrics().widthPixels * 0.90);
            int height = (int) (resources.getDisplayMetrics().heightPixels * 0.50);

            if (mDialog.getWindow() != null) {
                mDialog.getWindow().setLayout(width, height);
            }
        }
        final Resources.Theme theme = mContext.getTheme();
        mIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_feedback_black_24dp, theme);
        mIconColor = R.color.gray;
        mPositiveFeedbackText = resources.getString(R.string.product_question_positive_response);
        mNegativeFeedbackText = resources.getString(R.string.product_question_negative_response);
        mAmbiguityFeedbackText = resources.getString(R.string.product_question_ambiguous_response);

        mPositiveFeedbackIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_check_circle_black_24dp, theme);
        mNegativeFeedbackIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_cancel_black_24dp, theme);
        mAmbiguityFeedbackIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_help_black_24dp, theme);
    }

    private void initiateAllViews() {
        titleImageView = mDialog.findViewById(R.id.review_icon);
        reviewQuestionTextView = mDialog.findViewById(R.id.review_question);
        reviewValueTextView = mDialog.findViewById(R.id.review_value);

        feedbackBodyLayout = mDialog.findViewById(R.id.feedback_body_layout);

        positiveFeedbackLayout = mDialog.findViewById(R.id.postive_feedback_layout);
        negativeFeedbackLayout = mDialog.findViewById(R.id.negative_feedback_layout);
        ambiguityFeedbackLayout = mDialog.findViewById(R.id.ambiguity_feedback_layout);

        positiveFeedbackTextView = mDialog.findViewById(R.id.positive_feedback_text);
        negativeFeedbackTextView = mDialog.findViewById(R.id.negative_feedback_text);
        ambiguityFeedbackTextView = mDialog.findViewById(R.id.ambiguity_feedback_text);

        positiveFeedbackIconView = mDialog.findViewById(R.id.postive_feedback_icon);
        negativeFeedbackIconView = mDialog.findViewById(R.id.negative_feedback_icon);
        ambiguityFeedbackIconView = mDialog.findViewById(R.id.ambiguity_feedback_icon);
    }

    private void initiateListeners() {
        positiveFeedbackLayout.setOnClickListener(this::onPositiveFeedbackClicked);
        negativeFeedbackLayout.setOnClickListener(this::onNegativeFeedbackClicked);
        ambiguityFeedbackLayout.setOnClickListener(this::onAmbiguityFeedbackClicked);

        if (mDialog != null) {
            mDialog.setOnCancelListener(this::onCancelListener);
        }
    }

    public QuestionDialog show() {
        if (mDialog != null && mContext != null) {
            initiateAllViews();
            initiateListeners();

            LayerDrawable layerDrawable = (LayerDrawable) ResourcesCompat.getDrawable(
                mContext.getResources(),
                R.drawable.reviewdialog_round_icon,
                mContext.getTheme()
            );
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.round_background);
            final int whiteColor = ResourcesCompat.getColor(mContext.getResources(), R.color.white, mContext.getTheme());
            gradientDrawable.setColor(whiteColor);
            layerDrawable.setDrawableByLayerId(R.id.round_background, gradientDrawable);

            Drawable wrappedDrawable = DrawableCompat.wrap(this.mIcon);

            DrawableCompat.setTint(wrappedDrawable.mutate(), mContext.getResources().getColor(mIconColor));

            layerDrawable.setDrawableByLayerId(R.id.drawable_image, wrappedDrawable);

            titleImageView.setImageDrawable(layerDrawable);
            reviewQuestionTextView.setText(this.mQuestion);
            reviewValueTextView.setText(this.mValue);

            positiveFeedbackTextView.setText(this.mPositiveFeedbackText);
            positiveFeedbackIconView.setImageDrawable(this.mPositiveFeedbackIcon);

            negativeFeedbackTextView.setText(this.mNegativeFeedbackText);
            negativeFeedbackIconView.setImageDrawable(this.mNegativeFeedbackIcon);

            ambiguityFeedbackTextView.setText(this.mAmbiguityFeedbackText);
            ambiguityFeedbackIconView.setImageDrawable(this.mAmbiguityFeedbackIcon);

            feedbackBodyLayout.setBackgroundResource(this.mBackgroundColor);

            mDialog.show();
        }
        return this;
    }

    public QuestionDialog setQuestion(String question) {
        this.mQuestion = question;
        return this;
    }

    public QuestionDialog setBackgroundColor(@ColorRes int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
        return this;
    }

    public String getValue() {
        return mValue;
    }

    public QuestionDialog setValue(String value) {
        this.mValue = value;
        return this;
    }

    public QuestionDialog setOnReviewClickListener(QuestionActionListeners reviewActionsListeners) {
        this.mReviewActionsListener = reviewActionsListeners;
        return this;
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    private void onPositiveFeedbackClicked(View view) {
        if (mReviewActionsListener != null) {
            mReviewActionsListener.onPositiveFeedback(this);
        }
    }

    private void onNegativeFeedbackClicked(View view) {
        if (mReviewActionsListener != null) {
            mReviewActionsListener.onNegativeFeedback(this);
        }
    }

    private void onAmbiguityFeedbackClicked(View view) {
        if (mReviewActionsListener != null) {
            mReviewActionsListener.onAmbiguityFeedback(this);
        }
    }

    private void onCancelListener(DialogInterface dialog) {
        if (mReviewActionsListener != null) {
            mReviewActionsListener.onCancelListener(dialog);
        }
    }
}
