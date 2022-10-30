package com.example.fragmentsnavigation.presentation

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fragmentsnavigation.R
import com.example.fragmentsnavigation.data.OrderImageItem
import com.example.fragmentsnavigation.databinding.ImageItemCardBinding

class OrderImagesAdapter(
    //private val names: String
    private val onDeleteClicked: (String) -> Unit,
    private val onShowImageClicked: (String) -> Unit,
): RecyclerView.Adapter<OrderImagesAdapter.MyViewHolder>(), View.OnClickListener {

    private val TAG = this::class.java.simpleName

    var imagesList: List<OrderImageItem> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(newValue){
            field = newValue
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = ImageItemCardBinding.inflate(inflater, parent, false)
        binding.deleteImageBtn.setOnClickListener(this)
//        val width = parent.measuredWidth / 4
//        binding.root.layoutParams = RecyclerView.LayoutParams(width, width)

        return MyViewHolder(binding)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val imageListItem = imagesList[position]
        with(holder.binding){
            if (imageListItem.loaded){
                imageSync.visibility = View.VISIBLE
            }
            else {
                imageSync.visibility = View.GONE
            }
            root.setOnClickListener(this@OrderImagesAdapter)
            holder.itemView.tag = imageListItem
            deleteImageBtn.tag = imageListItem
            val imageUri = Uri.parse(imageListItem.image)
            if (imageListItem.image.isNotBlank()){
                Glide.with(imageView.context)
                    .load(imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_query)
                    .error(R.drawable.ic_query)
                    .into(imageView)
            }
            else {
                Glide.with(imageView.context)
                    .load(R.drawable.ic_query)
                    .into(imageView)
            }
        }
    }

    override fun getItemCount(): Int = imagesList.size

    class MyViewHolder(val binding: ImageItemCardBinding): RecyclerView.ViewHolder(binding.root){
    }

    override fun onClick(v: View) {
        Log.d(TAG, "OnClick action")
        val image = v.tag as OrderImageItem
        when(v.id){
            R.id.deleteImageBtn -> {
                Log.d(TAG, "Image delete button clicked ${image.name}")
                onDeleteClicked(image.name)

            }
            else -> {
                Log.d(TAG, "Image details clicked")
                onShowImageClicked(image.name)
            }
        }
    }
}