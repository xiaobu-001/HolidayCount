package com.holidaycount.app.ui.add

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.holidaycount.app.R
import com.holidaycount.app.data.model.EventType
import com.holidaycount.app.data.model.RepeatRule
import com.holidaycount.app.databinding.FragmentAddEventBinding
import com.holidaycount.app.utils.DateCalculation
import com.holidaycount.app.widget.HolidayWidgetProvider4x1
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AddEventBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_EDIT_ID = "edit_id"

        fun newInstanceForEdit(eventId: Long): AddEventBottomSheet {
            val sheet = AddEventBottomSheet()
            sheet.arguments = Bundle().apply { putLong(ARG_EDIT_ID, eventId) }
            return sheet
        }
    }

    private var _binding: FragmentAddEventBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddEventViewModel by viewModels()
    private var selectedDateMs: Long = System.currentTimeMillis()
    private var editId: Long = 0
    private var selectedColor: Int = Color.parseColor("#6200EE")

    private val colorOptions = listOf(
        0xFF6200EE.toInt(), 0xFFE91E63.toInt(), 0xFFFF5722.toInt(),
        0xFF4CAF50.toInt(), 0xFF2196F3.toInt(), 0xFFFF9800.toInt(),
        0xFF9C27B0.toInt(), 0xFF607D8B.toInt()
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editId = arguments?.getLong(ARG_EDIT_ID, 0) ?: 0

        setupViews()
        setupColorPicker()
        observeState()

        if (editId > 0) {
            loadEventForEdit()
        }
    }

    private fun setupViews() {
        // 日期选择器
        binding.btnPickDate.setOnClickListener { showDatePicker() }
        updateDateDisplay()

        // Emoji 常用选择
        val emojis = listOf("🎉", "🎂", "🧧", "🎄", "💝", "🌕", "🎃", "🏖️", "✈️", "💍", "🎓", "🎊")
        val emojiChipGroup = binding.chipGroupEmoji
        emojis.forEach { emoji ->
            val chip = com.google.android.material.chip.Chip(requireContext())
            chip.text = emoji
            chip.isCheckable = true
            chip.setOnClickListener {
                binding.etEmoji.setText(emoji)
            }
            emojiChipGroup.addView(chip)
        }

        // 重复规则
        binding.radioNone.isChecked = true

        // 通知天数
        binding.checkNotify1.text = "提前 1 天"
        binding.checkNotify3.text = "提前 3 天"
        binding.checkNotify7.text = "提前 7 天"
        binding.checkNotify15.text = "提前 15 天"

        // 保存按钮
        binding.btnSave.setOnClickListener { saveEvent() }
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.tvTitle.text = if (editId > 0) "编辑事件" else "添加倒计时"
    }

    private fun showDatePicker() {
        val constraints = CalendarConstraints.Builder()
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("选择目标日期")
            .setSelection(selectedDateMs)
            .setCalendarConstraints(constraints)
            .build()

        datePicker.addOnPositiveButtonClickListener { selMs ->
            selectedDateMs = selMs
            updateDateDisplay()
        }

        datePicker.show(parentFragmentManager, "date_picker")
    }

    private fun updateDateDisplay() {
        binding.btnPickDate.text = DateCalculation.formatDate(selectedDateMs)
    }

    private fun setupColorPicker() {
        colorOptions.forEachIndexed { index, color ->
            val colorView = layoutInflater.inflate(
                R.layout.item_color_circle, binding.colorPickerContainer, false
            )
            colorView.background.setTint(color)
            colorView.setOnClickListener {
                selectedColor = color
                binding.colorPickerContainer.children().forEach { it.isSelected = false }
                colorView.isSelected = true
            }
            if (index == 0) colorView.isSelected = true
            binding.colorPickerContainer.addView(colorView)
        }
    }

    private fun android.view.ViewGroup.children() = (0 until childCount).map { getChildAt(it) }

    private fun getRepeatRule(): RepeatRule {
        return when {
            binding.radioYearly.isChecked -> RepeatRule.YEARLY
            binding.radioMonthly.isChecked -> RepeatRule.MONTHLY
            else -> RepeatRule.NONE
        }
    }

    private fun getNotifyDays(): List<Int> {
        val days = mutableListOf<Int>()
        if (binding.checkNotify1.isChecked) days.add(1)
        if (binding.checkNotify3.isChecked) days.add(3)
        if (binding.checkNotify7.isChecked) days.add(7)
        if (binding.checkNotify15.isChecked) days.add(15)
        return days
    }

    private fun saveEvent() {
        val name = binding.etEventName.text?.toString() ?: ""
        val emoji = binding.etEmoji.text?.toString()?.takeIf { it.isNotBlank() } ?: "🎉"

        viewModel.saveEvent(
            name = name,
            emoji = emoji,
            targetDateMs = selectedDateMs,
            eventType = EventType.CUSTOM,
            repeatRule = getRepeatRule(),
            backgroundColor = selectedColor,
            notifyDays = getNotifyDays(),
            notifyHour = binding.timePicker.hour,
            notifyMinute = binding.timePicker.minute,
            editId = editId
        )
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is AddEventState.Saving -> {
                            binding.btnSave.isEnabled = false
                            binding.btnSave.text = "保存中..."
                        }
                        is AddEventState.Success -> {
                            // 更新 Widget
                            HolidayWidgetProvider4x1.updateAllWidgets(requireContext())
                            dismiss()
                        }
                        is AddEventState.Error -> {
                            binding.btnSave.isEnabled = true
                            binding.btnSave.text = "保存"
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            binding.btnSave.isEnabled = true
                            binding.btnSave.text = "保存"
                        }
                    }
                }
            }
        }
    }

    private fun loadEventForEdit() {
        viewLifecycleOwner.lifecycleScope.launch {
            val event = viewModel.getEvent(editId) ?: return@launch
            binding.etEventName.setText(event.name)
            binding.etEmoji.setText(event.emoji)
            selectedDateMs = event.targetDateMs
            updateDateDisplay()
            selectedColor = event.backgroundColor

            when (event.repeatRule) {
                RepeatRule.YEARLY -> binding.radioYearly.isChecked = true
                RepeatRule.MONTHLY -> binding.radioMonthly.isChecked = true
                RepeatRule.NONE -> binding.radioNone.isChecked = true
            }

            val notifyDays = event.getNotifyDaysList()
            binding.checkNotify1.isChecked = 1 in notifyDays
            binding.checkNotify3.isChecked = 3 in notifyDays
            binding.checkNotify7.isChecked = 7 in notifyDays
            binding.checkNotify15.isChecked = 15 in notifyDays
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
