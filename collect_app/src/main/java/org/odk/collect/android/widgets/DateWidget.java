/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.LocalDateTime;
import org.odk.collect.android.R;
import org.odk.collect.android.databinding.WidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.CustomDatePickerViewModel;
import org.odk.collect.android.fragments.dialogs.FixedDatePickerDialog.FixedDatePickerViewModel;
import org.odk.collect.android.logic.DatePickerDetails;
import org.odk.collect.android.utilities.DateTimeUtils;
import org.odk.collect.android.utilities.ScreenContext;
import org.odk.collect.android.widgets.interfaces.DateTimeWidgetListener;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.DateTimeWidgetUtils;

import java.util.Date;

@SuppressLint("ViewConstructor")
public class DateWidget extends QuestionWidget implements WidgetDataReceiver {
    WidgetAnswerBinding binding;

    private final DateTimeWidgetListener listener;

    private LocalDateTime selectedDate;
    private DatePickerDetails datePickerDetails;

    public DateWidget(Context context, QuestionDetails prompt, LifecycleOwner lifecycleOwner, DateTimeWidgetListener listener) {
        super(context, prompt);
        this.listener = listener;
        FixedDatePickerViewModel fixedDatePickerViewModel = new ViewModelProvider(((ScreenContext) context).getActivity())
                .get(FixedDatePickerViewModel.class);

        fixedDatePickerViewModel.getSelectedDate().observe(lifecycleOwner, localDateTime -> {
            if (localDateTime != null && listener.isWidgetWaitingForData(getFormEntryPrompt().getIndex())) {
                selectedDate = localDateTime;
                binding.widgetAnswerText.setText(DateTimeUtils.getDateTimeLabel(
                        selectedDate.toDate(), datePickerDetails, false, getContext()));
                widgetValueChanged();
            }
        });

/*        CustomDatePickerViewModel customDatePickerViewModel = new ViewModelProvider(((ScreenContext) context).getActivity())
                .get(CustomDatePickerViewModel.class);

            customDatePickerViewModel.getSelectedDate().observe(lifecycleOwner, localDateTime -> {
                if (localDateTime != null && listener.isWidgetWaitingForData(getFormEntryPrompt().getIndex())) {
                    selectedDate = localDateTime;
                    binding.widgetAnswerText.setText(DateTimeUtils.getDateTimeLabel(
                            selectedDate.toDate(), datePickerDetails, false, getContext()));
                    widgetValueChanged();
                }
            });*/
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = WidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        datePickerDetails = DateTimeUtils.getDatePickerDetails(prompt.getQuestion().getAppearanceAttr());

        if (prompt.isReadOnly()) {
            binding.widgetButton.setVisibility(GONE);
        } else {
            binding.widgetButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.widgetButton.setText(getContext().getString(R.string.select_date));

            binding.widgetButton.setOnClickListener(v -> {
                listener.setWidgetWaitingForData(prompt.getIndex());
                listener.displayDatePickerDialog(context, prompt.getIndex(), datePickerDetails, selectedDate);
            });
        }
        binding.widgetAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);

        if (prompt.getAnswerValue() == null) {
            selectedDate = DateTimeWidgetUtils.getCurrentDateTime();
            binding.widgetAnswerText.setText(R.string.no_date_selected);
        } else {
            selectedDate = DateTimeWidgetUtils.getSelectedDate(new LocalDateTime(getFormEntryPrompt().getAnswerValue().getValue()),
                    LocalDateTime.now());
            binding.widgetAnswerText.setText(DateTimeUtils.getDateTimeLabel(
                    (Date) getAnswer().getValue(), datePickerDetails, false, context));
        }

        return binding.getRoot();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.widgetButton.setOnLongClickListener(l);
        binding.widgetAnswerText.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.widgetButton.cancelLongPress();
        binding.widgetAnswerText.cancelLongPress();
    }

    @Override
    public void clearAnswer() {
        selectedDate = DateTimeWidgetUtils.getCurrentDateTime();
        binding.widgetAnswerText.setText(R.string.no_date_selected);
        widgetValueChanged();
    }

    @Override
    public IAnswerData getAnswer() {
        return binding.widgetAnswerText.getText().equals(getContext().getString(R.string.no_date_selected))
                ? null
                : new DateData(selectedDate.toDate());
    }

    @Override
    public void setData(Object answer) {
        selectedDate = (LocalDateTime) answer;
        binding.widgetAnswerText.setText(DateTimeUtils.getDateTimeLabel(
                selectedDate.toDate(), datePickerDetails, false, getContext()));
        widgetValueChanged();
    }
}
