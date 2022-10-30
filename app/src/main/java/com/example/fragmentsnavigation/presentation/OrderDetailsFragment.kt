package com.example.fragmentsnavigation.presentation

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.example.fragmentsnavigation.*
import com.example.fragmentsnavigation.R
import com.example.fragmentsnavigation.data.OrderImageItem
import com.example.fragmentsnavigation.databinding.FragmentOrderDetailsBinding
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class OrderDetailsFragment: Fragment(R.layout.fragment_order_details) {

    private lateinit var binding: FragmentOrderDetailsBinding
    private lateinit var adapter: OrderImagesAdapter

    private lateinit var workManager: WorkManager
    private lateinit var imageUri: Uri
    private var loaded = false

    private var orderId = 0

    private var listOfImages = mutableListOf<OrderImageItem>()

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()){uri ->
        uri?.let {
            val orderImage = OrderImageItem(loaded = false, name = "", image = it.toString())
            if (!listOfImages.contains(orderImage)){
                listOfImages += orderImage
                adapter.imagesList = listOfImages
                Log.e("", "image $uri added to list")
            }
            else {
                Log.e("", "image $uri is already in list")
            }
        }
    }

    private val makePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()){ result ->
        result?.let{
            if (it){
                Log.d("", "Image successfully stored")
                listOfImages.add(
                    OrderImageItem(
                        loaded=false,
                        name="Hello",
                        image = imageUri.toString()
                    )
                )
                adapter.imagesList = listOfImages
            }
            else {
                Log.e("", "Image not stored")
            }
        }
    }

    private var workerId: UUID? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        workManager = WorkManager.getInstance(requireContext())
        //listOfImages.add(OrderImageItem(image = "http://172.16.1.54:8080/image"))

//        val imagesList = (1..2).map {
//            OrderImageItem(
//                image = IMAGES[it % IMAGES.size]
//            )
//        }
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)

        binding.toolbar.setupWithNavController(findNavController(), AppBarConfiguration(findNavController().graph))

        adapter = OrderImagesAdapter(
            onDeleteClicked = {imageName ->
                lifecycleScope.launch {
                    deleteImage(imageName)
                }
            },
            onShowImageClicked = {imageName ->
                Log.e("HEHE2", "Need to load $imageName")
                //(requireActivity() as Navigator).showImage(imageName)
            }
        )

       // adapter.imagesList = listOfImages

        val layoutManager = GridLayoutManager(requireContext(), 3)
        binding.orderImagesRecyclerView.layoutManager = layoutManager
        binding.orderImagesRecyclerView.adapter = adapter

//        binding.addImageBtn.setOnClickListener{
//            //run activity launcher
//            getContent.launch("image/*")
//        }

        binding.addImageBtn.setOnClickListener {
            val photoFile = File.createTempFile(
                "IMG_",
                ".jpg",
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )

            imageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            Log.e("", "Uri to store image is ${imageUri.toString()} ${imageUri.encodedPath}")

            makePhoto.launch(imageUri)
        }

        binding.uploadBtn.setOnClickListener{
            val a = listOfImages.filter { !it.loaded }.map {
                it.image
            }.toTypedArray()
            Log.e("", "Array to send: $a")
            startUpload(
                orderId.toString(),
                "1234567890",
                a
            )
        }



        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            //val response = (requireContext().applicationContext as App).uploadRepository.getOrderById(requireArguments().getInt("ORDER_ID"))
            //TODO show only progressBar
            val orderName = arguments?.getString("orderName")
            Log.e("", "Got orderName to search: $orderName")
//            val response = (requireContext().applicationContext as App).uploadRepository.getOrderById(arguments?.getInt("orderId") ?: -1)
            val response = (requireContext().applicationContext as App).uploadRepository.getOrderByName(arguments?.getString("orderName") ?: "0")
            orderId = response?.id ?: -1

            if(response != null){
                loaded = true
            }

            binding.orderNameTextView.text = "Order: ${response?.orderName ?: orderName}"
            binding.orderUserTextView.text = "User: ${response?.userName ?: ""}"
            binding.orderCreatedAtTextView.text = "Created At: ${response?.createdAt ?: ""}"

            listOfImages = response?.images?.map{uri ->
                OrderImageItem(loaded = true, name = uri, image = "http://192.168.1.6:8080/orders/image/$uri")
            }?.toMutableList() ?: mutableListOf()
            adapter.imagesList = listOfImages

            binding.progressBar.visibility = View.GONE
            binding.group.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startUpload(orderId: String, userId: String, imagesUriArray: Array<String>){


        lifecycleScope.launch {
            val response = (requireContext().applicationContext as App).uploadRepository.newOrder(arguments?.getString("orderName") ?: "0")

            response?.let {

                binding.orderUserTextView.text = "User: ${it.userName ?: ""}"
                binding.orderCreatedAtTextView.text = "Created At: ${it.createdAt ?: ""}"

                if (imagesUriArray.isNotEmpty()){
                    val inputData = workDataOf(
                        ORDER_ID_KEY to it.id,
                        USER_ID_KEY to userId,
                        IMAGES_URI_ARRAY_KEY to imagesUriArray
                    )

                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val worker = OneTimeWorkRequestBuilder<UploadWorker>()
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build()

                    workerId = worker.id

                    workManager.enqueue(worker)
                    workManager.getWorkInfoByIdLiveData(workerId!!).observe(viewLifecycleOwner){workInfo ->
                        if(workInfo.state == WorkInfo.State.SUCCEEDED){
                            Log.e("Hehe", "Got output data: ${workInfo.outputData.getString("OUTPUT_DATA")}")
                        }
                    }
                }

            }
        }
    }

    private suspend fun deleteImage(name: String){
        try{
            (requireContext().applicationContext as App).uploadRepository.deleteImage(name)
            val response = (requireContext().applicationContext as App).uploadRepository.getOrderById(requireArguments().getInt("ORDER_ID"))
            adapter.imagesList = response?.images?.map{uri ->
                OrderImageItem(loaded = true, name = uri, image = "http://192.168.1.6/orders/image/$uri")
            } ?: emptyList()
        }
        catch (e: Exception){
            Log.e("Hehe", "Got exception on delete image: $e")
        }
    }

    companion object {
//        private val IMAGES = mutableListOf(
//            "https://images.unsplash.com/photo-1600267185393-e158a98703de?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NjQ0&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
//            "https://images.unsplash.com/photo-1579710039144-85d6bdffddc9?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0Njk1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
//            "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODE0&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
//            "https://images.unsplash.com/photo-1620252655460-080dbec533ca?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzQ1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
//            "https://images.unsplash.com/photo-1613679074971-91fc27180061?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzUz&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
//            "https://images.unsplash.com/photo-1485795959911-ea5ebf41b6ae?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzU4&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
//            "https://images.unsplash.com/photo-1545996124-0501ebae84d0?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0NzY1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
//            "https://images.unsplash.com/flagged/photo-1568225061049-70fb3006b5be?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0Nzcy&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
//            "https://images.unsplash.com/photo-1567186937675-a5131c8a89ea?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODYx&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800",
//            "https://images.unsplash.com/photo-1546456073-92b9f0a8d413?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixid=MnwxfDB8MXxyYW5kb218fHx8fHx8fHwxNjI0MDE0ODY1&ixlib=rb-1.2.1&q=80&utm_campaign=api-credit&utm_medium=referral&utm_source=unsplash_source&w=800"
//        )
        fun newInstance(orderId: Int): OrderDetailsFragment{
            val fragment = OrderDetailsFragment()
            fragment.arguments = bundleOf("ORDER_ID" to orderId)
            return fragment
        }
    }

}