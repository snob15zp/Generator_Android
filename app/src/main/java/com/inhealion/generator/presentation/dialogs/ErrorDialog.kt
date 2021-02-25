package com.inhealion.generator.presentation.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.inhealion.generator.R
import com.inhealion.generator.model.ErrorDialogData

const val ERROR_DIALOG_REQUEST_KEY = "ERROR_DIALOG_REQUEST_KEY"

class ErrorDialog : DialogFragment() {
    private val errorDialogData get() = requireArguments().getParcelable<ErrorDialogData>(KEY_ERROR_DATA)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(errorDialogData?.title)
            .setMessage(errorDialogData?.message)
            .setPositiveButton(R.string.dialog_button_ok, null)
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        parentFragmentManager.setFragmentResult(ERROR_DIALOG_REQUEST_KEY, bundleOf())
    }

    companion object {
        private const val KEY_ERROR_DATA = "KEY_ERROR_DATA"

        fun show(
            fragmentManager: FragmentManager,
            errorDialogData: ErrorDialogData
        ) {
            ErrorDialog().apply {
                arguments = bundleOf(
                    KEY_ERROR_DATA to errorDialogData,
                )
            }.show(fragmentManager, "ErrorDialog")
        }
    }

}
