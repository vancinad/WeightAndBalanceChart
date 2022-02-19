package org.vancinad.wbchart.ui.aircraft;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.vancinad.wbchart.R;

public class AircraftFragment extends Fragment {

    private AircraftViewModel aircraftViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("GalleryFragment", "onCreateView() called");
        aircraftViewModel =
                new ViewModelProvider(this).get(AircraftViewModel.class);
        View root = inflater.inflate(R.layout.fragment_aircraft, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        aircraftViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}