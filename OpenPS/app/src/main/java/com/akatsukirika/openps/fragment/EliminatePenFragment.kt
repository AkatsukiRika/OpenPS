package com.akatsukirika.openps.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.akatsukirika.openps.compose.MODE_LARIAT
import com.akatsukirika.openps.databinding.FragmentEliminatePenBinding
import com.akatsukirika.openps.viewmodel.EliminateViewModel
import kotlinx.coroutines.launch

class EliminatePenFragment(private val viewModel: EliminateViewModel, private val outerView: View, private val bitmap: Bitmap?) : Fragment() {
    private lateinit var binding: FragmentEliminatePenBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEliminatePenBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        initPaintView()
        initObservers()
    }

    private fun initPaintView() {
        binding.viewEliminatePaint.apply {
            setMagnifier(binding.viewEliminateZoom)
            setOuterView(outerView, bitmap)
            setDebug(true)
            setDisableTouch(false)
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            viewModel.size.collect {
                val mode = viewModel.mode.value
                if (mode != MODE_LARIAT) {
                    binding.viewEliminatePaint.setBrushSize(it)
                }
            }
        }
    }
}