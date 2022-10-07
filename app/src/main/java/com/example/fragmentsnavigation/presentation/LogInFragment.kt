package com.example.fragmentsnavigation.presentation

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.fragmentsnavigation.R
import com.example.fragmentsnavigation.databinding.FragmentLogInBinding
import kotlinx.coroutines.launch

class LogInFragment : Fragment() {

    private val TAG = this::class.java.simpleName
    private lateinit var binding: FragmentLogInBinding

    companion object {
        fun newInstance() = LogInFragment()
    }

    private val viewModel: LogInViewModel by viewModels { LogInViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLogInBinding.inflate(inflater, container, false)

        binding.btnLogin.setOnClickListener {
            hideKeyboard()
            viewModel.login(binding.etUsername.text.toString(), binding.etPassword.text.toString())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        lifecycleScope.launchWhenStarted {
            viewModel.showProgressBar.collect{
                if (it){
                    binding.progressBar.visibility = View.VISIBLE
                }
                else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.uiEvent.collect{event ->
                when(event){
                    is UiEvent.NavigateTo ->{
                        if (event.dest == "list"){
                            findNavController().navigate(R.id.action_logInFragment_to_listOrdersFragment)
                        }
                        else {
                            Toast.makeText(requireContext(), "Unknown Dest", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is UiEvent.Back -> {

                    }
                    is UiEvent.ShowToast -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun hideKeyboard(){
        val view = requireActivity().currentFocus
        val imm =  requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

}