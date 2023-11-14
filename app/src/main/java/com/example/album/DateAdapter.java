public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private ArrayList<String> dates;
    private HashMap<String, ArrayList<String>> imagesByDate;

    public DateAdapter(ArrayList<String> dates, HashMap<String, ArrayList<imageModel>> imagesByDate, Context context) {
        this.dates = dates;
        this.imagesByDate = imagesByDate;
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
        holder.imageRecyclerView.setAdapter(new ImageAdapter(images, this.context));
    }

    @Override
    public int getItemCount() {
        return dates.size();
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
}
