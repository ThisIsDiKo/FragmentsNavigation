package com.example.fragmentsnavigation.presentation

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fragmentsnavigation.data.OrderItem
import com.example.fragmentsnavigation.databinding.OrdersItemBinding

class OrdersAdapter(
    private val onClick: (Int) -> Unit
): RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>(), View.OnClickListener {

    private val TAG = this::class.java.simpleName

    var ordersList: List<OrderItem> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(newValue){
            field = newValue
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = OrdersItemBinding.inflate(inflater, parent, false)

        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val orderItem = ordersList[position]
        with(holder.binding){
            holder.itemView.tag = orderItem.orderId
            root.setOnClickListener(this@OrdersAdapter)
            orderUserView.text = orderItem.username
            orderCreatedAtView.text = orderItem.createdAt
            orderNameView.text = orderItem.orderName
        }
    }

    override fun getItemCount(): Int = ordersList.size

    override fun onClick(p0: View) {
        val orderId = p0.tag as Int
        Log.d(TAG, "Order on click perfomed $orderId")
        onClick(orderId)
        //TODO: need to go to fragment details
    }

    class OrderViewHolder(
        val binding: OrdersItemBinding
    ): RecyclerView.ViewHolder(binding.root)
}