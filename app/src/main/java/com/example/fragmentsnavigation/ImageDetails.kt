package com.example.fragmentsnavigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fragmentsnavigation.data.OrderImageItem
import com.example.fragmentsnavigation.databinding.FragmentImageDetailsBinding


class ImageDetails : Fragment(R.layout.fragment_image_details) {

    private lateinit var binding: FragmentImageDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageName = requireArguments().getString("IMAGE_NAME")
        val url = "http://172.16.1.54:8080/orders/image/$imageName"

        Glide.with(binding.ImageView.context)
            .load(url)
            .centerCrop()
            .placeholder(R.drawable.ic_query)
            .error(R.drawable.ic_query)
            .into(binding.ImageView)
    }

    companion object {
        @JvmStatic
        fun newInstance(imageName: String) =
            ImageDetails().apply {
                arguments = Bundle().apply {
                    putString("IMAGE_NAME", imageName)
                }
            }
    }
}