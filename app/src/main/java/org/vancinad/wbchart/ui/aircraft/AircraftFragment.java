package org.vancinad.wbchart.ui.aircraft;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.vancinad.wbchart.R;

public class AircraftFragment extends Fragment {

    //private AircraftViewModel aircraftViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("AircraftFragment", "onCreateView() called");
        //aircraftViewModel = new ViewModelProvider(this).get(AircraftViewModel.class);
        View root = inflater.inflate(R.layout.fragment_aircraft, container, false);
        final RecyclerView aircraftListView = root.findViewById(R.id.aircraftListView);
        aircraftListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        aircraftListView.setAdapter(new AircraftRecyclerAdapter());
        aircraftListView.addItemDecoration(new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));

//        aircraftViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }
}