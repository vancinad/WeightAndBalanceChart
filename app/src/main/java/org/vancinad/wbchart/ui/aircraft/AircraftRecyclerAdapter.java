package org.vancinad.wbchart.ui.aircraft;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.vancinad.wbchart.R;

public class AircraftRecyclerAdapter extends RecyclerView.Adapter<AircraftRecyclerAdapter.ViewHolder> {

    String[] mAircraftStrings;
    final String[] debuggingStrings= {"N113QW", "N127HJ", "N135MN", "N147HJ", "N155MN", "N167HJ", "N175MN", "N187HJ", "N195MN", "N207HJ", "N215MN", "N227HJ", "N235MN", "N247HJ", "N255MN", "N267HJ", "N275MN", "N287HJ", "N295MN", "N307HJ", "N315MN"};

    public AircraftRecyclerAdapter() {
        //TODO: Implement
//        AircraftFactory aircraftFactory = AircraftFactory.getInstance();
//        mAircraftStrings = aircraftFactory.getAircraftTailNumbers();
        mAircraftStrings = debuggingStrings;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;
        public ViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.textAircraftLine);
        }

        public TextView getTextView() {return mTextView;}

    } // end class ViewHolder

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.aircraft_list_item, viewGroup, false);
        v.setOnClickListener(view -> {
            int pos = ((RecyclerView)viewGroup).getChildAdapterPosition(view);
            Snackbar.make(view, mAircraftStrings[pos], Snackbar.LENGTH_LONG).show();
        });
        ViewHolder vh = new ViewHolder(v);
        TextView t = vh.getTextView();
        t.setLines(3);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTextView().setText(mAircraftStrings[position]+"\nCessna 172N");
    }

    @Override
    public int getItemCount() {
        return mAircraftStrings.length;
    }
}
