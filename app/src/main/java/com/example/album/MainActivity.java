package com.example.album;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//Quoc implement SortingDatesInterface de sap xep thoi gian cac anh giam dan theo ngay
public class MainActivity extends AppCompatActivity implements SortingDatesInterface {
    RecyclerView recyclerView;
    ArrayList<String> dates; // thông tin ngày cho từng list ảnh có cùng DATE_TAKEN
    // Hashmap có key là DATE_TAKEN, value là list các model ảnh có cùng DATE_TAKEN đó
    HashMap<String, ArrayList<imageModel>> imagesByDate;
    DateAdapter dateAdapter; // Pool chứa thông tin ngày và các ảnh có cùng key DATE_TAKEN


    // Tạo biến linkImage để chứa link của ảnh cần thêm vào.
    String linkImage="";
    // Biến listLinkAlbum dùng để chứa danh sách các ảnh của album
    ArrayList<String> listLinkAlbum;
    //Tạo mảng dữ liệu
    String nameAlbum[];
    ArrayList<String>listNameAlbum;
    ArrayList<Album> listAlbum;
    AlbumAdapter albumAdapter;
    ListView listViewAlbum;
    // Khởi tạo Database
    SQLiteDatabase dbAlbum;


    RecyclerView recyclerViewTrash;
    GridLayoutManager layoutManagerTrash;
    ImageAdapter adapterTrash;
    ArrayList<imageModel> imageListTrash;
    Button btnRestoreAll, btnDeleteAll;
    SQLiteDatabase dbTrash; // database Trash
    JobScheduler deleteScheduler; //service tự động xóa ảnh sau 24h


    // Các nút chuyển Activity
    Button btnAll, btnAlbum, btnTrash;
    Button activeButton; // button (tab) đang được chọn
    View frame; // khung để đặt 3 layout tương ứng với 3 tab
    int currentLayout = 0; // layout đang được chọn (có giá trị 1 hoặc 2 hoặc 3)

    // đánh dấu lần đầu tiên bấm vào 1 tab thì n của tab đó = 0 (không cần phải khôi phục trạng thái)
    int nAll = 0;
    int nAlbum = 0;
    int nTrash = 0;

    // 3 mảng lưu trạng thái của mỗi tab trước khi chuyển qua tab khác
    ArrayList<View> savedViewsAll = new ArrayList<>();
    ArrayList<View> savedViewsAlbum = new ArrayList<>();
    ArrayList<View> savedViewsTrash = new ArrayList<>();


    //Spinner và SearchView phục vụ cho việc filter ảnh theo thông tin exif
    SearchView exifSearchView;
    Spinner exifSpinner;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //service tự động xóa ảnh sau 24h
        deleteScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);


        //imageList = new ArrayList<>();
        imageListTrash = new ArrayList<>();
        listLinkAlbum= new ArrayList<>();
        dates = new ArrayList<>();
        imagesByDate = new HashMap<>();
        listNameAlbum= new ArrayList<>();
        listAlbum= new ArrayList<>();


        // 1. Tạo database album
        try {
            dbAlbum=this.openOrCreateDatabase("MyDatabase",MODE_PRIVATE,null);

        }
        catch (SQLException e){
            Toast.makeText(this, "Loi", Toast.LENGTH_SHORT).show();
        }

        // Tạo bảng chứa danh sách các tên album;
        CreateTable(dbAlbum,"listNameTable");

        // Tạo bảnh chứa danh sách album và ảnh để restore từ trash
        createTableToStoreAlbumAndImage(dbAlbum);

        // Insert giá trị Favorite vào, Favorite chính là album yêu thích.
        insertDataToTable(dbAlbum,"listNameTable","Favorite");

        // Lấy danh sách tên table từ bảng listNameTable
        getListFromTable(dbAlbum,listNameAlbum,"listNameTable");

        // Khởi tạo dữ liệu cho các Album, nếu Album chưa tồn tại thì tạo Table cho ALbum đó luôn
        int lenListNameAlbum= listNameAlbum.size();
        for(int index=0;index<lenListNameAlbum;index++)
        {
            CreateTable(dbAlbum,listNameAlbum.get(index));
        }

        // Tạo database Trash
        try
        {
            dbTrash=this.openOrCreateDatabase("dbTrash",MODE_PRIVATE,null);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        // tạo table trong database Trash
        CreateTableTrash();
        // đọc danh sách ảnh Trash từ database
        getListFromTableTrash();



        // khung để đặt 3 layout tương ứng với 3 tab
        frame = findViewById(R.id.frame);


        // load layout trash lên để kết nối với các widget, adapter,... trong đó
        loadLayout(R.layout.main_trash, 3);

        recyclerViewTrash = findViewById(R.id.recyclerViewTrash);
        btnRestoreAll = findViewById(R.id.btnRestoreAll);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);

        layoutManagerTrash = new GridLayoutManager(this, 3);
        recyclerViewTrash.setLayoutManager(layoutManagerTrash);
        adapterTrash = new ImageAdapter("2", imageListTrash,MainActivity.this);
        recyclerViewTrash.setAdapter(adapterTrash);

        nTrash = 1;

        // xử lý sự kiện nhấn nút Restore All trong Trash
        btnRestoreAll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // duyệt qua tất cả ảnh trong Trash
                while (imageListTrash.size() > 0)
                {
                    // lấy ra ID và dateTaken của từng ảnh
                    int imageID = imageListTrash.get(0).getId();
                    String imageDate = imageListTrash.get(0).getDateTaken();

                    // remove ảnh khỏi Trash
                    imageModel imgModel = imageListTrash.remove(0);

                    // cancel việc đếm số giờ còn lại
                    cancelCountTime(imgModel.getDeleteId());
                    imgModel.setDeleteTimeRemain(-1);
                    // cancel service tự động xóa sau 24h
                    cancelDelete(imgModel.getDeleteId());
                    imgModel.setDeleteId(0);

                    // nếu trong All không tồn tại dateTaken của ảnh này
                    if (!imagesByDate.containsKey(imageDate))
                    {
                        // thêm dateTaken này vào
                        dates.add(imageDate);
                        imagesByDate.put(imageDate, new ArrayList<imageModel>());
                        imagesByDate.get(imageDate).add(imgModel);

                        // sort lại ảnh sau khi thêm dateTaken
                        SortingDatesInterface.sortDatesDescending(dates);
                        SortingDatesInterface.sortHashMapByKeyDescending(imagesByDate);
                    }
                    // nếu trong All đã tồn tại dateTaken của ảnh này thì chỉ cần add ảnh này vào
                    // dateTaken đó
                    else
                    {
                        // lấy mảng hình ảnh hiện tại trong All có dakeTaken là dakeTaken của ảnh cần restore
                        ArrayList<imageModel> containerList = imagesByDate.get(imageDate);

                        // duyệt qua mảng hình ảnh (có ID đang được sắp xếp theo thứ tự tăng dần)
                        // để chèn hình ảnh cần restore vào đúng chỗ
                        int insertIndex = 0;
                        for (int i = 0; i < containerList.size(); i++)
                        {
                            if (imageID < containerList.get(i).getId())
                            {
                                break;
                            }

                            insertIndex++;
                        }

                        containerList.add(insertIndex, imgModel);

                        // xóa ảnh trong database Trash
                        Utils.deleteDataInTableTrash(getApplicationContext(), imageID);
                    }
                }
                adapterTrash.notifyDataSetChanged();
                dateAdapter.notifyDataSetChanged();
                restoreDataIntoAllTable(dbAlbum);
            }
        });

        // xử lý sự kiện nhấn nút Delete All trong Trash
        btnDeleteAll.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openDialogDeleteAllTrash();
            }
        });

        // load layout all lên để kết nối với các widget, adapter,... trong đó
        loadLayout(R.layout.main_all, 1);
        // Kết nối recycler và init các instance chứa thông tin ảnh khi đọc
        recyclerView = findViewById(R.id.recyclerView);
        // Cài đặt view layout cho recycler chính chứa các pool ảnh theo dạng trượt dọc (VERTICAL)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Tạo và gắn dateAdapter có chức năng tạo các pool ảnh có cùng DATE_TAKEN cho recyclerView chính
        dateAdapter = new DateAdapter(dates, imagesByDate, this);
        recyclerView.setAdapter(dateAdapter);

        //Set dữ liệu cho spinner là loại thông tin exif tìm kiếm
        String[] arraySpinner = new String[] {
                "Date taken", "Camera make"
        };

        //Ánh xạ exif
        exifSpinner = (Spinner) findViewById(R.id.exifSpinner);

        //set adapter cho spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exifSpinner.setAdapter(spinnerAdapter);

        //Ánh xạ searchView
        exifSearchView = findViewById(R.id.exifSearchView);

        nAll = 1;


        // Kết nối các button chuyển Activity
        btnAll = (Button)findViewById(R.id.btnAllTab);
        btnAlbum = (Button)findViewById(R.id.btnAlbumTab);
        btnTrash = (Button)findViewById(R.id.btnTrashTab);

        // custom tiêu đề của tab đang được chọn
        activeButton = btnAll; // tab được chọn mặc định khi vừa mở ứng dụng là All
        Drawable activeDrawable = getResources().getDrawable(R.drawable.custom_button_active,null);
        activeButton.setBackground(activeDrawable);


        // kiểm tra phiên bản Android để lựa chọn cách xin cấp quyền cho phù hợp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Kiểm tra xem ứng dụng có quyền MANAGE_EXTERNAL_STORAGE chưa
            // (quyền này cần để xóa ảnh khỏi thư viện ảnh của thiết bị)
            if(!Environment.isExternalStorageManager()){
                // Nếu chưa thì mở phần Setting để người dùng cấp quyền
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }

            // Kiểm tra cho phép truy cập bộ nhớ ngoài bằng Dexter
            Dexter.withContext(this)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            ReadSdcard(MainActivity.this);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
        }
        else
        {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1002);
                }
            } else {
                ReadSdcard(MainActivity.this);
            }
        }


        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                customActiveButton(btnAll); // custom tiêu đề của tab All
                loadLayout(R.layout.main_all, 1); // load layout của tab All lên frame

                //Xóa thông tin đang tìm kiếm trong search view đi và hủy chọn search view luôn
                if(!exifSearchView.isIconified()){
                    exifSearchView.setQuery("",false);
                    exifSearchView.setIconified(true);
                }
            }
        });

        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                customActiveButton(btnAlbum); // custom tiêu đề của tab Album
                loadLayout(R.layout.main_album, 2); // load layout của tab Album lên frame

                // Xử lý hiện list Album, xử dụng list View
                listViewAlbum = findViewById(R.id.lvAlbum);
                // Chuyển ArrayList<String> listNameAlbum sang String nameAlbum[]

                listAlbum.clear();
                for (int i=0;i<listNameAlbum.size();i++)
                {
                    listAlbum.add(new Album(listNameAlbum.get(i)));

                }

                albumAdapter= new AlbumAdapter(MainActivity.this,R.layout.list_albums,listAlbum);
                listViewAlbum.setAdapter(albumAdapter);

                // Xử lý khi click vào 1 album
                listViewAlbum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //tạo Intent để gửi dữ liệu
                        Intent albumIntent= new Intent(MainActivity.this,Current_Album.class);
                        // Gửi name
                        albumIntent.putExtra("name",listNameAlbum.get(position));

                        // Clear data trong listLinkAlbum
                        listLinkAlbum.clear();

                        // Lấy danh sách các link ảnh của album được chọn.
                        getListFromTable(dbAlbum,listLinkAlbum,listNameAlbum.get(position));

                        // Gửi danh sách các link
                        albumIntent.putStringArrayListExtra("listLink",listLinkAlbum);
                        // Bắt đầu gửi dữ liệu.
                        startActivity(albumIntent);
                    }
                });

                 ImageButton btnAddAlbum;
                 btnAddAlbum= (ImageButton) findViewById(R.id.btnAlbumAdd);
                 btnAddAlbum.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         openDialogAddAlbum(Gravity.CENTER);
                     }
                 });
            }
        });

        btnTrash.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                customActiveButton(btnTrash); // custom tiêu đề của tab Trash
                loadLayout(R.layout.main_trash, 3); // load layout của tab Trash lên frame
            }
        });


        // Broadcast của move vào Trash
        IntentFilter filter_moveToTrash = new IntentFilter("deleteImage");
        // Broadcast của restore 1 ảnh trong Trash
        IntentFilter filter_restore = new IntentFilter("restoreImage");
        // Broadcast của delete 1 ảnh trong Trash
        IntentFilter filter_deleteTrash = new IntentFilter("deleteTrash");
        // Broadcast của auto delete trash sau 24h
        IntentFilter filter_autoDeleteTrash = new IntentFilter("autoDeleteTrash");
        // Broadcast của auto update time remain của ảnh trong Trash
        IntentFilter filter_autoCountTime = new IntentFilter("autoCountTime");
        // Broadcast của click addFavorite
        IntentFilter filter_addFavorite = new IntentFilter("addFavorite");
        // Broadcast của click delete Album
        IntentFilter filter_deleteAlbum = new IntentFilter("deleteAlbum");
        // Broadcast của click add Image Album
        IntentFilter filter_addImageAlbum = new IntentFilter("addImageToAlbum");
        // Broadcast của Confirm insert Image to Album
        IntentFilter filter_insertImageToAlbum = new IntentFilter("addLinkImageToAlbumHadChoosen");

        IntentFilter filter_deleteInAlbum = new IntentFilter("deleteInAlbum");

        registerReceiver(receiver, filter_moveToTrash);
        registerReceiver(receiver, filter_restore);
        registerReceiver(receiver, filter_deleteTrash);
        registerReceiver(receiver, filter_autoDeleteTrash);
        registerReceiver(receiver, filter_autoCountTime);
        registerReceiver(receiver, filter_addFavorite);
        registerReceiver(receiver, filter_deleteAlbum);
        registerReceiver(receiver, filter_addImageAlbum);
        registerReceiver(receiver, filter_insertImageToAlbum);

        //Phương thức này dùng để xử lý khi nhập chuỗi tìm kiếm trong searchView
        exifSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //Xử lý khi nhập nguyên chuỗi tìm kiếm rồi bấm enter
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchStr;

                //Nếu chuỗi tìm kiếm rỗng
                if(query.isEmpty())
                    searchStr = query;
                else
                    //Nếu chuỗi tìm kiếm không rỗng, thì sửa lại chuỗi cho có định dạng: "loại thông tin/thông tin tìm kiếm"
                    searchStr = exifSpinner.getSelectedItem() + "/"  + query;

                //đưa chuỗi tìm kiếm vào dateAdapter để nó filter rồi hiện kết quả lên all
                dateAdapter.getFilter().filter(searchStr);
                return false;
            }

            //Xử lý khi khi có bất cứ thay đổi gì trong chuỗi tìm kiếm (ko cần bấm enter)
            @Override
            public boolean onQueryTextChange(String newText) {
                String searchStr;

                //Nếu chuỗi tìm kiếm rỗng
                if(newText.isEmpty())
                    searchStr = newText;
                else
                    //Nếu chuỗi tìm kiếm không rỗng, thì sửa lại chuỗi cho có định dạng: "loại thông tin/thông tin tìm kiếm"
                    searchStr = exifSpinner.getSelectedItem() + "/"  + newText;

                //đưa chuỗi tìm kiếm vào dateAdapter để nó filter rồi hiện kết quả lên all
                dateAdapter.getFilter().filter(searchStr);
                return false;
            }
        });
        registerReceiver(receiver, filter_deleteInAlbum);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // hủy đăng ký broadcast receiver
        unregisterReceiver(receiver);
    }

    // Refresh danh sach anh
    @Override
    protected void onResume() {
        super.onResume();
        // Cần clear hai biến này của DateAdapter trước khi đọc
        // danh sách ảnh từ bộ nhớ ngoài để tránh sự trùng lặp ảnh
        imagesByDate.clear();
        dates.clear();
        ReadSdcard(MainActivity.this);

    }


    // Tải dữ liệu ảnh URI trong bộ nhớ
    private void ReadSdcard(Context context){
        Uri collection;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }else
        {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String projection[] = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN,
        };

        // Sắp xếp danh sách Uri ảnh theo thứ tự ngày taken giảm dần
        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";

        try(Cursor cursor = MainActivity.this.getContentResolver().query(
                collection,
                projection,
                null,
                null,
                orderBy
        )){
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);

            int i = 0;

            while (cursor.moveToNext()){
                long id = cursor.getLong(idColumn);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);

                // Kiểm tra nếu ảnh đang trong Trash của app thì không đọc vào All nữa
                boolean isTrash = false;

                //duyệt qua mảng Trash
                for(int k=0;k<imageListTrash.size();k++)
                {
                    if(imageListTrash.get(k).getPath().equals(contentUri))
                    {
                        // update id của ảnh đó trong database Trash và mảng Trash vì lỡ người dùng
                        // vừa thêm xóa ảnh trong thiết bị, dẫn đến id cũ không còn đúng
                        updateIdInTableTrash(imageListTrash.get(k).getId(), i);
                        imageListTrash.get(k).setId(i);
                        isTrash = true;
                        break;
                    }
                }

                // nếu không phải ảnh trong Trash thì đọc bình thường
                if(!isTrash)
                {
                    // format date dạng unix timestamp sang dạng dd-MM yyyy và dùng làm key cho hashMap
                    String dateTaken_unix = cursor.getString(dateTakenColumn);
                    String dateTaken=null;
                    if (dateTaken_unix != null) {
                        try {
                            long dv = Long.valueOf(dateTaken_unix);
                            Date df = new java.util.Date(dv);
                            dateTaken = new SimpleDateFormat("dd-MM-yyyy").format(df);
                            // Thực hiện các công việc khác với dateTaken nếu cần
                        } catch (NumberFormatException e) {
                            // Xử lý lỗi khi không thể chuyển đổi thành số
                            e.printStackTrace();
                        }
                    }

                    if (!imagesByDate.containsKey(dateTaken)) {
                        dates.add(dateTaken);
                        imagesByDate.put(dateTaken, new ArrayList<imageModel>());
                    }

                    imageModel addingImage = new imageModel(i, dateTaken, contentUri);
                    addingImage.setExif(contentUri,linkImage,context);
                    imagesByDate.get(dateTaken).add(addingImage);
                }

                i++;
            }

            //Gọi hàm từ interface SortingDatesInterface để sắp xếp ngày giảm dần
            //sắp xếp lại hashmap theo thứ tự giảm dần của key (key là dateTaken)
            SortingDatesInterface.sortDatesDescending(dates);
            //sắp xếp lại date theo thứ tự giảm dần
            SortingDatesInterface.sortHashMapByKeyDescending(imagesByDate);

            dateAdapter.notifyDataSetChanged();
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // lắng nghe sự kiện bấm nút Move: di chuyển ảnh đó từ All qua Trash
            if ("deleteImage".equals(intent.getAction()))
            {
                String imageIndex = intent.getStringExtra("imageIndex");
                String imageDate = intent.getStringExtra("imageDate");

                ArrayList<imageModel> containerList = imagesByDate.get(imageDate);
                imageModel imgModel = containerList.remove(Integer.parseInt(imageIndex));
                imageListTrash.add(0, imgModel);

                if(containerList.size()==0)
                {
                    imagesByDate.remove(imageDate);
                    dates.remove(imageDate);
                }

                adapterTrash.notifyDataSetChanged();
                dateAdapter.notifyDataSetChanged();

                /* Lấy link ảnh rồi sau đó xóa ảnh khỏi toàn bộ album*/
                linkImage=intent.getStringExtra("imageLink");
                deleteDataFromAllTable(dbAlbum,linkImage);

                // tạo id riêng biệt cho mỗi lượt xóa để service, restore,... biết là ảnh nào
                int deleteId = (int) (System.currentTimeMillis() / 1000);
                insertDataToTableTrash(imgModel.getId(),deleteId,imgModel.getDateTaken(),imgModel.getPath().toString());
                imgModel.setDeleteId(deleteId);

                // đặt lịch đếm giờ và xóa tự động
                scheduleCountTime(deleteId, imgModel.getId());
                scheduleDelete(deleteId, imgModel.getId(), linkImage);
            }

            // lắng nghe sự kiện bấm nút Restore: di chuyển ảnh đó từ Trash qua All
            if ("restoreImage".equals(intent.getAction()))
            {
                String imageIndexTrash = intent.getStringExtra("imageIndexTrash");
                imageModel imgModel = imageListTrash.remove(Integer.parseInt(imageIndexTrash));

                cancelCountTime(imgModel.getDeleteId());
                imgModel.setDeleteTimeRemain(-1);
                cancelDelete(imgModel.getDeleteId());
                imgModel.setDeleteId(0);

                int imageID = imgModel.getId();
                String imageDate = imgModel.getDateTaken();

                if (!imagesByDate.containsKey(imageDate))
                {
                    dates.add(imageDate);
                    imagesByDate.put(imageDate, new ArrayList<imageModel>());
                    imagesByDate.get(imageDate).add(imgModel);

                    SortingDatesInterface.sortDatesDescending(dates);
                    SortingDatesInterface.sortHashMapByKeyDescending(imagesByDate);
                }
                else
                {
                    ArrayList<imageModel> containerList = imagesByDate.get(imageDate);

                    int insertIndex = 0;
                    for (int i = 0; i < containerList.size(); i++)
                    {
                        if (imageID < containerList.get(i).getId())
                        {
                            break;
                        }

                        insertIndex++;
                    }

                    containerList.add(insertIndex, imgModel);
                }

                adapterTrash.notifyDataSetChanged();
                dateAdapter.notifyDataSetChanged();


                /* Lấy link ảnh rồi restore ảnh vào toàn bộ album đã được thêm trước đó. */
                String imageLink=intent.getStringExtra("imageLink");
                restoreOneDataIntoALLTable(dbAlbum,imageLink);

                Utils.deleteDataInTableTrash(getApplicationContext(), imageID);
            }

            // lắng nghe sự kiện xóa 1 ảnh khỏi thùng rác (xóa vĩnh viễn)
            if ("deleteTrash".equals(intent.getAction()))
            {
                String imageIndexTrash = intent.getStringExtra("imageIndexTrash");
                imageModel imgModel = imageListTrash.remove(Integer.parseInt(imageIndexTrash));
                adapterTrash.notifyDataSetChanged();

                cancelCountTime(imgModel.getDeleteId());
                imgModel.setDeleteTimeRemain(-1);
                cancelDelete(imgModel.getDeleteId());
                imgModel.setDeleteId(0);

                //Thêm bởi quân
                String linkImage= intent.getStringExtra("linkImage");
                Utils.deleteOneDataFromTrashAlbumImage(getApplicationContext(),linkImage);
                //xong

                // xóa trong database Trash
                Utils.deleteDataInTableTrash(getApplicationContext(), imgModel.getId());
                // xóa trong thư viện ảnh của thiết bị
                Utils.deleteImageInDevice(getApplicationContext(), imgModel.getPath());
            }

            // lắng nghe sự kiện tự động xóa ảnh trong Trash sau 24h
            if ("autoDeleteTrash".equals(intent.getAction()))
            {
                int imageId = intent.getIntExtra("imageId", -1);

                for(int i=0;i<imageListTrash.size();i++)
                {
                    if(imageListTrash.get(i).getId() == imageId)
                    {
                        // xóa ảnh trong mảng Trash
                        imageListTrash.remove(imageListTrash.get(i));
                        adapterTrash.notifyDataSetChanged();
                        break;
                    }
                }
            }

            // lắng nghe sự kiện tự động update time remaining của ảnh trong Trash sau 1h
            if ("autoCountTime".equals(intent.getAction()))
            {
                int imageId = intent.getIntExtra("imageId", -1);
                int timeRemain = intent.getIntExtra("timeRemain", -1);

                for(int i=0;i<imageListTrash.size();i++)
                {
                    if(imageListTrash.get(i).getId() == imageId)
                    {
                        // cập nhật thuộc tính deleteTimeRemain của ảnh
                        imageListTrash.get(i).setDeleteTimeRemain(timeRemain);
                        // cập nhật sự thay đổi này lên giao diện (TextView)
                        adapterTrash.notifyItemChanged(i);
                        break;
                    }
                }
            }

            // lắng nghe sự kiện Thêm ảnh vào album Favorite (yêu thích)
            if("addFavorite".equals(intent.getAction()))
            {
                // Lấy link
                linkImage=intent.getStringExtra("imageLink");
                // Thêm link ảnh vào trong link Album
                getListFromTable(dbAlbum,listLinkAlbum,"Favorite");
                if(isValueExists(dbAlbum,"Favorite",linkImage))
                {
                    Toast.makeText(context, "Image existed in Favorite", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(context, "Successfully added image to Favorite", Toast.LENGTH_SHORT).show();

                    insertDataToTable(dbAlbum,"Favorite",linkImage);
                }
                //adapterTrash.notifyDataSetChanged();
                //dateAdapter.notifyDataSetChanged();
            }

            // lắng nghe sự kiện xóa album
            if("deleteAlbum".equals(intent.getAction()))
            {
                // Lấy Tên của Album muốn delete
                String nameAlbum= intent.getStringExtra("nameAlbum");

                // Xóa table chứa danh sách link của album

                deleteTable(dbAlbum,nameAlbum);
                // Xóa tên khỏi danh sách album
                deleteDataInTable(dbAlbum,"listNameTable",nameAlbum);


                // Xóa phần tử album trong listview;
                listAlbum.removeIf(album -> album.getName().equals(nameAlbum));
                // Load lại danh sách album
                getListFromTable(dbAlbum,listNameAlbum,"listNameTable");

                albumAdapter.notifyDataSetChanged();
            }

            // lắng nghe sự kiện Thêm ảnh vào album, sự kiện này sẽ gửi danh sách album cho dialog add image to album
            // sau đó dialog sẽ hiện thì danh sách album cho người dùng chọn.
            if("addImageToAlbum".equals(intent.getAction()))
            {
                Intent intentListAlbum= new Intent("listAlbumSender");
                getListFromTable(dbAlbum,listNameAlbum,"listNameTable");
                intentListAlbum.putStringArrayListExtra("listAlbum",listNameAlbum);
                sendBroadcast(intentListAlbum);
            }

            // lắng nghe sự kiện Thêm ảnh vào album, sự kiện này sẽ thêm ảnh vào album được chọn
            if("addLinkImageToAlbumHadChoosen".equals(intent.getAction()))
            {
                String nameAlbumToAdd=intent.getStringExtra("albumName");
                String linkImagetoAdd=intent.getStringExtra("imageLink");
                insertDataToTable(dbAlbum,nameAlbumToAdd,linkImagetoAdd);
                //albumAdapter.notifyDataSetChanged();
            }

            // lắng nghe sự kiện xóa ảnh khỏi album, sự kiện này sẽ thêm ảnh vào album được chọn
            if("deleteInAlbum".equals(intent.getAction()))
            {
                String nameAlbum=intent.getStringExtra("nameAlbum");
                String linkImage=intent.getStringExtra("imageLink");
                deleteDataInTable(dbAlbum,nameAlbum,linkImage);
            }
        }
    };

    // Xử lý hiện dialog Add album
    private void openDialogAddAlbum(int gravity)
    {
        final Dialog dialog= new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_add_album);

        Window window= dialog.getWindow();
        if(window==null)
        {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
        windowAttributes.gravity=gravity;
        window.setAttributes(windowAttributes);

        if(Gravity.CENTER== gravity)
        {
            dialog.setCancelable(true);
        }
        else
        {
            dialog.setCancelable(false);
        }

        EditText edtDialogNameAlbum= dialog.findViewById(R.id.edtDialogNameAlbum);
        Button btnDialogBack= dialog.findViewById(R.id.btnDialogBack);
        Button btnDialogAdd= dialog.findViewById(R.id.btnDialogAdd);


        btnDialogBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnDialogAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textName= edtDialogNameAlbum.getText().toString();
                if(textName.equals(""))
                {
                    //Trường họp người dùng không nhập tên
                    Toast.makeText(MainActivity.this, "Input a name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(listNameAlbum.contains(textName))
                    {
                        // trường hợp người dùng nhập tên trùng với tên album đã tồn tại
                        Toast.makeText(MainActivity.this, "An album with this name existed", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        listNameAlbum.add(textName);
                        listAlbum.add(new Album(textName));
                        insertDataToTable(dbAlbum,"listNameTable",textName);
                        CreateTable(dbAlbum,textName);
                        Toast.makeText(MainActivity.this, "Successfully added an album", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    albumAdapter.notifyDataSetChanged();
                }

            }
        });

        // gọi lệnh Show để hiện Dialog
        dialog.show();
    }


    // Xử lý hiện dialog xác nhận xóa tất cả ảnh khỏi thùng rác (xóa vĩnh viễn)
    private void openDialogDeleteAllTrash()
    {
        final Dialog dialog= new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_delete_all_trash);

        Window window= dialog.getWindow();
        if(window==null)
        {
            return;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
        windowAttributes.gravity= Gravity.BOTTOM;
        window.setAttributes(windowAttributes);
        dialog.setCancelable(true);

        Button btnDialogCancel= dialog.findViewById(R.id.btnDialogCancel);
        Button btnDialogDelete= dialog.findViewById(R.id.btnDialogDelete);

        // Khi nhấn nút Cancel của Dialog
        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Khi nhấn nút Delete của Dialog
        btnDialogDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                for(int i=0;i<imageListTrash.size();i++)
                {
                    cancelCountTime(imageListTrash.get(i).getDeleteId());
                    imageListTrash.get(i).setDeleteTimeRemain(-1);
                    cancelDelete(imageListTrash.get(i).getDeleteId());
                    imageListTrash.get(i).setDeleteId(0);

                    // xóa trong database Trash
                    Utils.deleteDataInTableTrash(getApplicationContext(), imageListTrash.get(i).getId());
                    // xóa trong thư viện ảnh của thiết bị
                    Utils.deleteImageInDevice(getApplicationContext(), imageListTrash.get(i).getPath());
                }

                // xóa trong mảng
                imageListTrash.clear();
                adapterTrash.notifyDataSetChanged();

                // Thêm bởi quân
                // gọi hàm xóa toàn bộ thông tin trong TrashAlbumImage
                deleteAllDataInTableTrashAlbumImage(dbAlbum);
                // xong

                dialog.dismiss();
            }
        });

        // gọi lệnh Show để hiện Dialog
        dialog.show();
    }


    // custom tiêu đề của tab đang được chọn
    private void customActiveButton(Button clickedButton)
    {
        Drawable normalDrawable = getResources().getDrawable(R.drawable.custom_button,null);
        activeButton.setBackground(normalDrawable);

        Drawable activeDrawable = getResources().getDrawable(R.drawable.custom_button_active,null);
        clickedButton.setBackground(activeDrawable);

        activeButton = clickedButton;
    }

    private void saveCurrentLayoutState()
    {
        switch (currentLayout)
        {
            case 1:
                saveViews((ViewGroup) frame, savedViewsAll);
                break;
            case 2:
                saveViews((ViewGroup) frame, savedViewsAlbum);
                break;
            case 3:
                saveViews((ViewGroup) frame, savedViewsTrash);
        }
    }

    // lưu trạng thái của tab hiện tại (ví dụ: vị trí scroll,...) trước khi chuyển tab
    private void saveViews(ViewGroup frame, ArrayList<View> savedViews)
    {
        savedViews.clear();
        int childCount = frame.getChildCount();

        for (int i = 0; i < childCount; i++)
        {
            savedViews.add(frame.getChildAt(i));
        }
    }

    // khôi phục trạng thái của tab (ví dụ: vị trí scroll,...)
    private void restoreViews(ViewGroup container, ArrayList<View> savedViews)
    {
        for (View savedView : savedViews)
        {
            container.addView(savedView);
        }
    }

    // load layout tương ứng với 1 trong 3 tab vào frame
    private void loadLayout(int layoutResId, int layoutType)
    {
        // lần load layout đầu tiên thì không cần lưu trạng thái layout trước nó
        if(nTrash!=0)
        {
            saveCurrentLayoutState();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View tabContent = inflater.inflate(layoutResId, (ViewGroup) frame, false);

        ((ViewGroup) frame).removeAllViews();
        ((ViewGroup) frame).addView(tabContent);

        switch (layoutType)
        {
            case 1:
                if(nAll!=0)
                {
                    restoreViews((ViewGroup) frame, savedViewsAll);
                }
                break;
            case 2:
                if(nAlbum!=0)
                {
                    restoreViews((ViewGroup) frame, savedViewsAlbum);
                }
                break;
            case 3:
                if(nTrash!=0)
                {
                    restoreViews((ViewGroup) frame, savedViewsTrash);
                }
                break;
        }

        currentLayout = layoutType;
    }

    // Create Table, hàm dùng để tạo bảng trong database
    public void CreateTable(SQLiteDatabase db, String nameTable)
    {
        try {
            String sqlQuery="CREATE TABLE IF NOT EXISTS \""+nameTable+ "\" ("
                    + " recID integer PRIMARY KEY autoincrement, "
                    + " nameText text ); ";
            db.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // insertDataToTable là hàm dùng để insert data vào trong table
    public void insertDataToTable(SQLiteDatabase db, String nameTable, String data)
    {
        if(isValueExists(db,nameTable,data))
        {
            return;
        }
        try {
            String sqlQuery="insert into \""+nameTable+"\"(nameText) values ('"+data+"');";
            db.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //deleteDataInTable là hàm dùng để xóa data khỏi table
    public void deleteDataInTable(SQLiteDatabase db, String nameTable, String data)
    {
        try {
            String sqlQuery="DELETE From \""+nameTable+"\" Where nameText= '"+ data +"' ;";
            db.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //getListFromTable là hàm dùng để lấy chuỗi các data từ table
    public void getListFromTable(SQLiteDatabase db, ArrayList<String> nameTextList, String nameTable)
    {
        try {
            //3. truy van
            String sql = "select * from \""+ nameTable+"\"";
            Cursor c1 = db.rawQuery(sql, null);
            c1.moveToPosition(-1);
            nameTextList.clear();
            while( c1.moveToNext() ){
                int recId = c1.getInt(0);
                String text = c1.getString(1);

                nameTextList.add(text);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // isValueExists là hàm dùng để kiểm tra xem giá trị valueToCheck đã tồn tại trong table hay chưa
    public boolean isValueExists(SQLiteDatabase db , String nameTable ,String valueToCheck) {
        //3. truy van
        String sql = "select * from \""+ nameTable+ "\" Where nameText = '"+ valueToCheck +"' ;";
        Cursor c1 = db.rawQuery(sql, null);
        c1.moveToPosition(-1);

        int count = 0;
        while( c1.moveToNext() ){
            ++count;
        }
        return count > 0;
    }

    // deleteTable là hàm dùng để xóa table
    public void deleteTable(SQLiteDatabase db, String nameTable)
    {
        try {
            String sqlQuery="DROP TABLE IF EXISTS \""+nameTable+"\"";
            db.execSQL(sqlQuery);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    // hàm dùng để xóa ảnh khỏi toàn bộ album
    public void deleteDataFromAllTable(SQLiteDatabase db, String data)
    {
        try {

            //3. truy van
            String sql = "select * from \"listNameTable\"";
            Cursor c1 = db.rawQuery(sql, null);
            c1.moveToPosition(-1);
            while( c1.moveToNext() ){
                int recId = c1.getInt(0);
                String nameTable = c1.getString(1);
                if(isValueExists(db,nameTable,data))
                {
                    deleteDataInTable(db,nameTable,data);
                    insertImageToDeleteAblbumTable(db,nameTable,data);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // Hàm dùng để restore ảnh, ảnh sẽ được đưa trở lại toàn bộ album đã được thêm trước đó.
    public void restoreDataIntoAllTable(SQLiteDatabase db)
    {
        try {

            //3. truy van
            String sql = "select * from TrashAlbumImage";
            Cursor c1 = db.rawQuery(sql, null);
            c1.moveToPosition(-1);
            while( c1.moveToNext() ){

                int recId = c1.getInt(0);
                String nameTable = c1.getString(1);
                String imageLink=c1.getString(2);
                if(isValueExists(db,"listNameTable",nameTable))
                {
                    insertDataToTable(db,nameTable,imageLink);
                }
            }
            deleteAllDataInTableTrashAlbumImage(db);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //Hàm dùng để tạo table TrashAlbumImage,
    // table này sẽ lưu tên album và ảnh đã bị xóa khỏi album đó khi người dùng xóa ảnh trong all
    public void createTableToStoreAlbumAndImage(SQLiteDatabase db){

        try {
            String sqlQuery="CREATE TABLE IF NOT EXISTS TrashAlbumImage (" +
                    "    recID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "    nameAlbum TEXT," +
                    "    nameImage TEXT" +
                    "); ";
            db.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    //- Phương thức onBackPressed để thoát cái bàn phím (hiện khi dùng searchView)
    //- Thoát cái bàn phím thôi chứ chưa thoát cái khung searchView
    @Override
    public void onBackPressed() {
        if(!exifSearchView.isIconified()){
            exifSearchView.setIconified(true);
            return;
        }
        super.onBackPressed();
    }

    //Hàm dùng để đưa giá trị album và image vào trong table TrashAlbumImage
    public void insertImageToDeleteAblbumTable(SQLiteDatabase db, String nameTable, String data) {
        try {
            String sqlQuery = "INSERT INTO TrashAlbumImage(nameAlbum, nameImage) VALUES  ('" + nameTable + "','" + data + "');";
            db.execSQL(sqlQuery);
        } catch (SQLException e) {
            Log.e("SQL_ERROR", "Có lỗi xảy ra", e);
        }
    }

    //Hàm dùng để xóa toàn bộ dữ liệu trong table TrashAlbumImage
    public void deleteAllDataInTableTrashAlbumImage(SQLiteDatabase db)
    {
        try {
            String sqlQuery="DELETE FROM TrashAlbumImage";
            db.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    //Hàm dùng để đưa ảnh trở về các album khi nhấn nút restore khi mở ảnh trong trash
    public void restoreOneDataIntoALLTable(SQLiteDatabase db, String data)
    {
        try {

            //3. truy van
            String sql = "select * from TrashAlbumImage where nameImage= '"+data+"'; ";
            Cursor c1 = db.rawQuery(sql, null);
            c1.moveToPosition(-1);
            while( c1.moveToNext() ){

                int recId = c1.getInt(0);
                String nameTable = c1.getString(1);
                String imageLink=c1.getString(2);
                if(isValueExists(db,"listNameTable",nameTable))
                {
                    insertDataToTable(db,nameTable,imageLink);
                }
            }
            // Sau khi đã restore ảnh vào toàn bộ album trước đó thì xóa thông tin về album và ảnh khỏi table restore
            Utils.deleteOneDataFromTrashAlbumImage(getApplicationContext(),data);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // tạo table trong database lưu ảnh trong Trash
    public void CreateTableTrash()
    {
        try {
            String sqlQuery="CREATE TABLE IF NOT EXISTS \"trash\"" + " ("
                    + "id integer PRIMARY KEY, "
                    + "idDelete integer, "
                    + "hourRemain integer, "
                    + "dateTaken text, "
                    + "link text); ";
            dbTrash.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // đọc danh sách ảnh trong Trash từ database
    public void getListFromTableTrash()
    {
        try
        {
            String sql = "select * from \"trash\"";
            Cursor c1 = dbTrash.rawQuery(sql, null);
            c1.moveToPosition(-1);

            imageListTrash.clear();

            //duyệt qua từng dòng trong database
            while(c1.moveToNext())
            {
                // lấy ra các thông tin của ảnh
                int id = c1.getInt(0);
                int idDelete =  c1.getInt(1);
                int hourRemain =  c1.getInt(2);
                String dateTaken = c1.getString(3);
                String linkImage = c1.getString(4);

                // tạo imageModel với các thông tin trên và setExif
                imageModel imgModel = new imageModel(id, dateTaken, Uri.parse(linkImage));
                imgModel.setDeleteId(idDelete);
                imgModel.setDeleteTimeRemain(hourRemain);
                imgModel.setExif(Uri.parse(linkImage),linkImage,MainActivity.this);

                // add imageModel vừa tạo vào mảng Trash
                imageListTrash.add(imgModel);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // thêm 1 ảnh vào database Trash
    public void insertDataToTableTrash(int id, int idDelete, String dateTaken, String linkImage)
    {
        try
        {
            String sqlQuery="insert into \"trash\""
                    + "(id, idDelete, hourRemain, dateTaken, link) values ("
                    + id + "," + idDelete + ",24,'" + dateTaken + "','" + linkImage + "');";
            dbTrash.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // cập nhật id của ảnh trong Trash trong lần bật app này
    public void updateIdInTableTrash(int oldId, int newId)
    {
        try
        {
            String sqlQuery="UPDATE \"trash\" SET id=" + newId + " WHERE id=" + oldId + ";";
            dbTrash.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }



    // đặt lịch xóa ảnh tự động sau 24h
    private void scheduleDelete(int jobId, int imageId, String imageLink) {
        JobInfo.Builder builder = new JobInfo.Builder(jobId, new ComponentName(this, AutoDeleteService.class))
                .setMinimumLatency(24 * 60 * 60 * 1000) // 24 giờ
                //.setMinimumLatency(24 * 1000) //24 giây
                .setPersisted(true);

        // truyền thông tin vào
        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt("imageId", imageId);
        bundle.putString("imageLink", imageLink);
        builder.setExtras(bundle);

        // đặt lịch
        deleteScheduler.schedule(builder.build());
    }

    // đặt lịch đếm số giờ còn lại của ảnh trong Trash
    private void scheduleCountTime(int jobId, int imageId) {
        // Tạo Data để truyền tham số
        Data imageData = new Data.Builder()
                .putInt("deleteId",jobId)
                .putInt("nRemain", 24) // khởi tạo là 24h
                .putInt("imageId", imageId)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AutoCountTime.class)
                .setInputData(imageData)
                .addTag(String.valueOf(jobId))
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
    }

    // cancel lịch tự động xóa ảnh sau 24h
    private void cancelDelete(int jobId) {
        deleteScheduler.cancel(jobId);
    }

    // cancel lịch đếm số giờ còn lại của ảnh trong Trash
    private void cancelCountTime(int jobId) {
        WorkManager.getInstance(this).cancelAllWorkByTag(String.valueOf(jobId));
    }
}
