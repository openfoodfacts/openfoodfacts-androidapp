package openfoodfacts.github.scrachx.openfood.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import openfoodfacts.github.scrachx.openfood.R;


public class QuestionDialog {

    private Context mContext;


    private Drawable mIcon;

    @ColorRes
    private int mIconColor;

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


    private String mPositiveFeedbackText;
    private Drawable mPositiveFeedbackIcon;
    private String mNegativeFeedbackText;
    private Drawable mNegativeFeedbackIcon;
    private String mAmbiguityFeedbackText;
    private Drawable mAmbiguityFeedbackIcon;
    private Dialog mDialog;
    private QuestionActionListeners mReviewActionsListener;

    public QuestionDialog(Context mContext) {
        this.mContext = mContext;
        Resources resources = mContext.getResources();

        mDialog = new Dialog(mContext, R.style.QuestionDialog);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_product_question);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int width = (int) (resources.getDisplayMetrics().widthPixels * 0.90);
            int height = (int) (resources.getDisplayMetrics().heightPixels * 0.50);

            if (mDialog.getWindow() != null) {
                mDialog.getWindow().setLayout(width, height);
            }
        }
        mIcon = resources.getDrawable(R.drawable.ic_feedback_black_24dp);
        mIconColor = R.color.gray;
        mPositiveFeedbackText = resources.getString(R.string.product_question_positive_response);
        mNegativeFeedbackText = resources.getString(R.string.product_question_negative_response);
        mAmbiguityFeedbackText = resources.getString(R.string.product_question_ambiguous_response);

        mPositiveFeedbackIcon = resources.getDrawable(R.drawable.ic_check_circle_black_24dp);
        mNegativeFeedbackIcon = resources.getDrawable(R.drawable.ic_cancel_black_24dp);
        mAmbiguityFeedbackIcon = resources.getDrawable(R.drawable.ic_help_black_24dp);
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

            LayerDrawable layerDrawable = (LayerDrawable) mContext.getResources().getDrawable(R.drawable.reviewdialog_round_icon);
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.round_background);
            gradientDrawable.setColor(Color.parseColor("#FFFFFF"));
            layerDrawable.setDrawableByLayerId(R.id.round_background, gradientDrawable);

            Drawable drawable = this.mIcon;
            Drawable wrappedDrawable = DrawableCompat.wrap(drawable);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DrawableCompat.setTint(drawable.mutate(), mContext.getResources().getColor(mIconColor));
            } else {
                drawable.setColorFilter(mContext.getResources().getColor(mIconColor), PorterDuff.Mode.SRC_IN);
            }


            layerDrawable.setDrawableByLayerId(R.id.drawable_image, drawable);

            titleImageView.setImageDrawable(layerDrawable);
            reviewQuestionTextView.setText(this.mQuestion);
            reviewValueTextView.setText(this.mValue);

            positiveFeedbackTextView.setText(this.mPositiveFeedbackText);
            positiveFeedbackIconView.setImageDrawable(this.mPositiveFeedbackIcon);
            positiveFeedbackIconView.setColorFilter(mContext.getResources().getColor(mIconColor));

            negativeFeedbackTextView.setText(this.mNegativeFeedbackText);
            negativeFeedbackIconView.setImageDrawable(this.mNegativeFeedbackIcon);
            negativeFeedbackIconView.setColorFilter(mContext.getResources().getColor(mIconColor));

            ambiguityFeedbackTextView.setText(this.mAmbiguityFeedbackText);
            ambiguityFeedbackIconView.setImageDrawable(this.mAmbiguityFeedbackIcon);
            ambiguityFeedbackIconView.setColorFilter(mContext.getResources().getColor(mIconColor));

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