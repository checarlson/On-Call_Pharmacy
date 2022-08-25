package com.ldd.on_callpharmacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PharmacyRVAdapter extends RecyclerView.Adapter<PharmacyRVAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<Pharmacy> pharmacyArrayList;

    // creating a constructor class.
    public PharmacyRVAdapter(Context context, ArrayList<Pharmacy> noticeArrayList) {
        this.context = context;
        this.pharmacyArrayList = noticeArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // passing our layout file for displaying our card item
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.pharmacy_rv_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        // setting data to our text views from our modal class.
        final Pharmacy pharmacy = pharmacyArrayList.get(position);

        holder.name.setText(pharmacy.getName());
        DecimalFormat df = new DecimalFormat("0.00");
        String distance = df.format(pharmacy.getDistance());
        holder.distance.setText("Distance: "+distance+"km");
        holder.open_close.setText(pharmacy.getOpen_close());

        ParseFile file = pharmacy.getPhaImage();
        file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                // Decode the Byte[] into Bitmap
               Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
               holder.image.setImageBitmap(bmp);
            }
        });

        holder.map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context.getApplicationContext(), MapRouteActivity.class);
//                intent.putExtra("image", notices.getPostImage());
                Utility.pharmLatLng = pharmacy.getpLatLng();
                Utility.myLocation = pharmacy.getMyLatLng();
                Utility.pharmName = pharmacy.getName();
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return pharmacyArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our text views.
        private final ImageView image;
        private final TextView distance, open_close, name, map;
//        private AdView adView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views.
            image = itemView.findViewById(R.id.pharmaImage);
            distance = itemView.findViewById(R.id.distance);
            name = itemView.findViewById(R.id.name);
            open_close = itemView.findViewById(R.id.open_close);
            map = itemView.findViewById(R.id.map);
        }
    }
}
