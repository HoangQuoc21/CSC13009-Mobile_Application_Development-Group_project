package com.example.album;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.album.ImageAdapter;
import com.example.album.R;
import com.example.album.imageModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> implements Filterable {
    private Context context;
    private ArrayList<String> dates;
    private HashMap<String, ArrayList<imageModel>> imagesByDate;
    private HashMap<String, ArrayList<imageModel>> imagesByDateOld;

    public DateAdapter(ArrayList<String> dates, HashMap<String, ArrayList<imageModel>> imagesByDate, Context context) {
        this.dates = dates;
        this.imagesByDate = imagesByDate;
        //sortImagesByDateDescending();
        this.imagesByDateOld = imagesByDate;
        this.context = context;
    }

    // Function to sort imagesByDates by dateTaken in descending order
    public void sortImagesByDateDescending() {
        // Ensure imagesByDates is not null
        if (imagesByDate == null) {
            return;
        }

        // Create a comparator for sorting dates in descending order
        Comparator<String> dateComparator = (date1, date2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

            try {
                Date parsedDate1 = sdf.parse(date1);
                Date parsedDate2 = sdf.parse(date2);

                // Sort in descending order
                if (parsedDate1 != null && parsedDate2 != null) {
                    return parsedDate2.compareTo(parsedDate1);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return 0;
        };

        // Convert the keys of imagesByDates to a list for sorting
        List<String> dateList = new ArrayList<>(imagesByDate.keySet());

        // Sort the dateList based on the dateComparator
        Collections.sort(dateList, dateComparator);

        // Create a new HashMap for sorted images
        HashMap<String, ArrayList<imageModel>> sortedImagesByDates = new HashMap<>();

        // Populate the new HashMap with sorted entries
        for (String date : dateList) {
            sortedImagesByDates.put(date, imagesByDate.get(date));
        }

        // Update imagesByDates with the sorted data
        imagesByDate = sortedImagesByDates;
        imagesByDateOld = sortedImagesByDates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.date_images, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String date = dates.get(position);
        ArrayList<imageModel> images = imagesByDate.get(date);

        holder.dateTextView.setText(date);
        GridLayoutManager layoutManager = new GridLayoutManager(context,3);
        holder.imageRecyclerView.setLayoutManager(layoutManager);
        holder.imageRecyclerView.setAdapter(new ImageAdapter("1", images, this.context));
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    //====================================== QUOC WROTE THIS =======================================
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String strSearch = constraint.toString();

                HashMap<String, ArrayList<imageModel>> filterList = new HashMap<>();

                if(strSearch.isEmpty())
                    filterList = imagesByDateOld;
                else{
                    if(imagesByDateOld.containsKey(strSearch)){
                        filterList.put(strSearch,imagesByDateOld.get(strSearch));
                    }
                }

                imagesByDate = filterList;

                FilterResults filterResults = new FilterResults();
                filterResults.values = imagesByDate;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                imagesByDate = (HashMap<String, ArrayList<imageModel>>)results.values;

                dates = new ArrayList<>(imagesByDate.keySet());

                notifyDataSetChanged();
            }
        };
    }
    //==============================================================================================

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public RecyclerView imageRecyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            imageRecyclerView = itemView.findViewById(R.id.imageRecyclerView);
        }
    }
}