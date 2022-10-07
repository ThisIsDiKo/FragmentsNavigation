package com.example.fragmentsnavigation

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.whenResumed
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.fragmentsnavigation.data.OrderItem
import com.example.fragmentsnavigation.databinding.FragmentListOrdersBinding
import com.example.fragmentsnavigation.presentation.OrderImagesAdapter
import com.example.fragmentsnavigation.presentation.OrdersAdapter
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListOrdersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListOrdersFragment : Fragment() {

    private val TAG = this::class.java.simpleName
    private lateinit var binding: FragmentListOrdersBinding
    private lateinit var adapter: OrdersAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var ordersList = emptyList<OrderItem>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListOrdersBinding.inflate(inflater, container, false)

        swipeRefreshLayout = binding.swipeRefreshLayout

        adapter = OrdersAdapter(){
            Log.e(TAG, "Need to get order with id $it")
            //(requireActivity() as Navigator).showDetails(it)
            val b = bundleOf("orderId" to it)
            findNavController().navigate(R.id.action_listOrdersFragment_to_orderDetailsFragment, b)
        }

        adapter.ordersList = ordersList

        val layoutManager = LinearLayoutManager(requireContext())
        binding.ordersRecyclerView.layoutManager = layoutManager
        binding.ordersRecyclerView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener {
            adapter.ordersList = emptyList()
            lifecycleScope.launch {
                updateList()
            }
        }

        binding.fab.setOnClickListener{
            Log.e(TAG, "Fab clicked")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



//        val config = AppBarConfiguration(findNavController().graph)
//        binding.toolbar.setupWithNavController(findNavController(), config)

        binding.toolbar.title = "Список заказов"

        val menuHost: MenuHost = binding.toolbar

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_with_search, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        lifecycleScope.launchWhenResumed {
            Log.e(TAG, "start getting orders")
            adapter.ordersList = (requireContext().applicationContext as App).uploadRepository.getAllOrders().map{
                OrderItem(
                    orderId = it.id,
                    orderName = it.orderName,
                    username = it.userName,
                    createdAt = it.createdAt
                )
            }
        }
    }

    private suspend fun updateList(){
        setRefresh(true)
        try {
            adapter.ordersList = (requireContext().applicationContext as App).uploadRepository.getAllOrders().map{
                OrderItem(
                    orderId = it.id,
                    orderName = it.orderName,
                    username = it.userName,
                    createdAt = it.createdAt
                )
            }
        }
        catch (e: Exception){
            Log.e(TAG, "Got update exception: $e")
        }

        setRefresh(false)
    }

    private fun setRefresh(state: Boolean){
        swipeRefreshLayout.isRefreshing = state
    }
}