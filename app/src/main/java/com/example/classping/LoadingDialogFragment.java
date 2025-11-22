package com.example.classping;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public class LoadingDialogFragment extends DialogFragment {
    public static LoadingDialogFragment newInstance() { return new LoadingDialogFragment(); }
    @NonNull
    @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_loading, null);
        b.setView(view);
        b.setCancelable(false);
        return b.create();
    }
}
