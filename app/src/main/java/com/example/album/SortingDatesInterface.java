package com.example.album;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

public interface SortingDatesInterface {
    //===================================== QUOC WROTE THIS ======================================

    //Hàm sắp xếp một mảng các chuỗi "ngày tháng năm" theo thứ tự giảm dần
    static void sortDatesDescending(ArrayList<String> dateList) {
        Collections.sort(dateList, new DateComparator());
    }

    //Hàm sắp xếp Hashmap có key là chuỗi "ngày tháng năm" theo thứ tự giảm dần của key
    static void sortHashMapByKeyDescending(HashMap<String, ArrayList<imageModel>> map) {
        // Ép kiểu Hashmap về Treemap và sử dụng lớp DateComparator tự viết. Lớp này dùng để sắp xếp các key của hashmap theo thứ tự giảm dần
        TreeMap<String, ArrayList<imageModel>> sortedMap = new TreeMap<>(new DateComparator());
        //Đưa giá trị của map truyền vào vào treemap ở trên (sẽ tự động được sắp xếp luôn)
        sortedMap.putAll(map);

        //Xóa hết giá trị của map truyền vào đi
        map.clear();

        //Đưa các giá trị đã được sắp xếp từ TreeMap sắp xếp ở trên về lại map truyền vào ban đầu
        map.putAll(sortedMap);
    }

    //Lớp DateComparator tự viết để so sánh 2 chuỗi ngày tháng năm có định dạng "dd-MM-yyyy"
    static class DateComparator implements Comparator<String> {

        //Tạo dateFormat
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        //Hàm so sánh
        //date1 > date2: trả về 1
        //date1 <= date2: trả về 0
        @Override
        public int compare(String date1, String date2) {
            try {
                return dateFormat.parse(date2).compareTo(dateFormat.parse(date1));
            } catch (ParseException e) {
                e.printStackTrace(); // Handle parsing exception as needed
                return 0;
            }
        }
    }
    //===========================================================================================
}
