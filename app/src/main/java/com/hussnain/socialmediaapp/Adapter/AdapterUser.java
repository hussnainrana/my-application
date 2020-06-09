package com.hussnain.socialmediaapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hussnain.socialmediaapp.Model.ModelUser;
import com.hussnain.socialmediaapp.R;

import java.util.List;
import java.util.zip.Inflater;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    Context context;
    List<ModelUser> modelUsers;

    public AdapterUser(Context context, List<ModelUser> modelUsers) {
        this.context = context;
        this.modelUsers = modelUsers;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view=LayoutInflater.from(context).inflate(R.layout.user_rowitem,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {

        String name=modelUsers.get(position).getUserName();
        String email=modelUsers.get(position).getUserEmail();
        String photo=modelUsers.get(position).getUserPhoto();

        holder.mName.setText(name);
        holder.mEmail.setText(email);

        try{
            Glide.with(context)
                    .load(photo)
                    .placeholder(R.drawable.item_image)
                    .into(holder.mAvatar);
        }
        catch (Exception e)
        {
            Glide.with(context)
                    .load(photo)
                    .placeholder(R.drawable.item_image)
                    .into(holder.mAvatar);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "" +modelUsers.get(position).getUserName(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class MyHolder extends RecyclerView.ViewHolder {

        TextView mName, mEmail;
        ImageView mAvatar;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.text_userName_row_item);
            mEmail = itemView.findViewById(R.id.text_userEmail_row_item);
            mAvatar = itemView.findViewById(R.id.image_user_row_item);

        }
    }

}
