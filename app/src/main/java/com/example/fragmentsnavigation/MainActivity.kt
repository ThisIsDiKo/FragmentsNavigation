package com.example.fragmentsnavigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import com.example.fragmentsnavigation.presentation.OrderDetailsFragment

interface Navigator {

    fun showDetails(orderId: Int)

    fun showImage(imageName: String)
}

class MainActivity : AppCompatActivity(), Navigator {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //supportActionBar?.hide()
        setContentView(R.layout.activity_main)
//
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container_view, ListOrdersFragment())
//            .commit()
    }

    override fun showDetails(orderId: Int) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragment_container_view, OrderDetailsFragment.newInstance(orderId))
            .commit()
    }

    override fun showImage(imageName: String) {
        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .replace(R.id.fragment_container_view, ImageDetails.newInstance(imageName))
            .commit()
    }

}