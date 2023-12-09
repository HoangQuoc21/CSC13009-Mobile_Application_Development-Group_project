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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

//[QUOC]: implements thêm interface Filterable để lọc thông tin exif; SortingDatesInterface để sắp xếp ảnh theo thứ tự ngày giảm dần
public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> implements Filterable, SortingDatesInterface {
    private Context context;
    private ArrayList<String> dates;
    private HashMap<String, ArrayList<imageModel>> imagesByDate;
    //========================================= QUOC ADDED THIS =====================================
    //Thêm biến này giữ giá trị là hashmap khởi tạo ban đầu (khi bật app) để phục vụ cho việc filter
    private HashMap<String, ArrayList<imageModel>> imagesByDateOld;
    //===============================================================================================

    public DateAdapter(ArrayList<String> dates, HashMap<String, ArrayList<imageModel>> imagesByDate, Context context) {
        this.dates = dates;
        this.imagesByDate = imagesByDate;

        //========================================= QUOC ADDED THIS =====================================
        this.imagesByDateOld = imagesByDate; //giữ giá trị hashmap khởi tạo ban đầu
        //===============================================================================================
        this.context = context;
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public RecyclerView imageRecyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            imageRecyclerView = itemView.findViewById(R.id.imageRecyclerView);
        }
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    //====================================== QUOC WROTE THIS =======================================
    //Phương thức getFilter() để lọc hình theo chuỗi yêu cầu truyền từ MainActivity vào
    @Override
    public Filter getFilter() {
        return new Filter() {
            //Phương thức xử lý việc filter
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                //Lấy chuỗi tìm kiếm yêu cầu (từ MainActivity)
                String strSearch = constraint.toString();

                //Tạo hashmap tạm thời dùng cho việc lọc
                HashMap<String, ArrayList<imageModel>> filterList = new HashMap<>();

                //Nếu chuỗi tìm kiếm rỗng thì gán hashmap lọc bằng hashmap khởi tạo
                if(strSearch.isEmpty() )
                    filterList = imagesByDateOld;
                else{
                    //Nếu chuỗi tìm kiếm không rỗng

                    //Tách chuỗi tìm kiếm lấy : loại thông tin và thông tin tìm kiếm
                    ArrayList<String> tokens = new ArrayList<String>(Arrays.asList(strSearch.split("/")));

                    //lọc dựa trên loại thông tin:

                    //1. Nếu loại thông tin là dateTaken
                    if(tokens.get(0).equals("Date taken")){
                        for (Map.Entry<String, ArrayList<imageModel>> entry : imagesByDateOld.entrySet()) {
                            for (imageModel img:entry.getValue()){
                                //Nếu tìm thấy ảnh có thông tin cần tìm
                                if((img.getDateTaken() != null) && img.getDateTaken().contains(tokens.get(1))){
                                    //Kiểm tra xem trong hashmap lọc đã tồn tại dateTaken của ảnh khớp thông tin chưa
                                    if(!filterList.containsKey(entry.getKey())){
                                        //nếu chưa thì thêm dateTaken vào hashmap lọc
                                        filterList.put(entry.getKey(),new ArrayList<imageModel>());
                                    }
                                    //Đưa ảnh khớp vào hashmap
                                    filterList.get(entry.getKey()).add(img);
                                }
                            }
                        }
                    }
                    //2. Nếu loại thooing tin là "Camera make" thì cần lọc lồng qua các ảnh của từng key trong hashmap
                    else if (tokens.get(0).equals("Camera make")){
                        for (Map.Entry<String, ArrayList<imageModel>> entry : imagesByDateOld.entrySet()) {
                            for (imageModel img:entry.getValue()){
                                //Nếu tìm thấy ảnh có thông tin cần tìm
                                if((img.cameraMake != null) && img.cameraMake.toLowerCase().contains(tokens.get(1).toLowerCase())){
                                    //Kiểm tra xem trong hashmap lọc đã tồn tại dateTaken của ảnh khớp thông tin chưa
                                    if(!filterList.containsKey(entry.getKey())){
                                        //nếu chưa thì thêm dateTaken vào hashmap lọc
                                        filterList.put(entry.getKey(),new ArrayList<imageModel>());
                                    }
                                    //Đưa ảnh khớp vào hashmap
                                    filterList.get(entry.getKey()).add(img);
                                }
                            }
                        }
                    }
                }

                //gán giá trị hashmap set cho adapter là hashmap lọc được
                imagesByDate = filterList;

                //Trả về kết quả lọc
                FilterResults filterResults = new FilterResults();
                filterResults.values = imagesByDate;

                return filterResults;
            }

            //Phương thức publishResults() để hiển thị kết quả lọc từ phương thức getFilter() ở trên
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //gán giá trị hashmap set cho adapter là kết quả lọc được (kết quả trả về của hàm getFilter())
                imagesByDate = (HashMap<String, ArrayList<imageModel>>)results.values;

                //gán giá trị date set cho adapter là bộ key của hashmap
                dates = new ArrayList<>(imagesByDate.keySet());

                //sắp xếp lại hashmap theo thứ tự giảm dần của key (key là dateTaken)
                SortingDatesInterface.sortHashMapByKeyDescending(imagesByDate);
                //sắp xếp lại date theo thứ tự giảm dần
                SortingDatesInterface.sortDatesDescending(dates);

                //Gọi phương thức này để adapter hiện thay đổi theo hashmap và date vừa thay đổi ở trên
                notifyDataSetChanged();
            }
        };
    }
    //==============================================================================================
}