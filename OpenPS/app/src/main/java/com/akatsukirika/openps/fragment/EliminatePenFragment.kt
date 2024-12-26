package com.akatsukirika.openps.fragment

import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.akatsukirika.openps.compose.MODE_ERASER
import com.akatsukirika.openps.compose.MODE_LARIAT
import com.akatsukirika.openps.compose.MODE_PAINT
import com.akatsukirika.openps.compose.MODE_RECOVER
import com.akatsukirika.openps.databinding.FragmentEliminatePenBinding
import com.akatsukirika.openps.store.SettingsStore
import com.akatsukirika.openps.view.EliminatePaintView
import com.akatsukirika.openps.viewmodel.EliminateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface EliminateFragmentCallback {
    fun onMatrixChange(matrix: Matrix)
}

class EliminatePenFragment(
    private val viewModel: EliminateViewModel?,
    private val outerView: View?
) : Fragment(), EliminateFragmentCallback, EliminatePaintView.Callback {
    constructor() : this(null, null)

    private lateinit var binding: FragmentEliminatePenBinding

    private var isInit = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEliminatePenBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onMatrixChange(matrix: Matrix) {
        binding.viewEliminatePaint.setImageMatrix(matrix, isInit)
    }

    private fun initView() {
        initPaintView()
        initObservers()
    }

    private fun initPaintView() {
        binding.viewEliminatePaint.apply {
            setMagnifier(binding.viewEliminateZoom)
            setOuterView(outerView ?: return, viewModel?.originalBitmap)
            setDebug(SettingsStore.isDebugMode)
            setDisableTouch(false)
            isInit = true
            setImageMatrix(viewModel?.matrix?.value ?: Matrix(), isInit)
            setCallback(this@EliminatePenFragment)
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            viewModel?.size?.collect {
                val mode = viewModel.mode.value
                if (mode != MODE_LARIAT) {
                    binding.viewEliminatePaint.setBrushSize(it)
                }
            }
        }
        lifecycleScope.launch {
            viewModel?.mode?.collect {
                when (it) {
                    MODE_PAINT -> {
                        binding.viewEliminatePaint.apply {
                            endRestore()
                            showIndicator(true)
                            setBrushSize(viewModel.size.value, false)
                            setErase(false)
                        }
                    }

                    MODE_LARIAT -> {
                        binding.viewEliminatePaint.apply {
                            endRestore()
                            showIndicator(true)
                            setDashedLine()
                            setErase(false)
                        }
                    }

                    MODE_ERASER -> {
                        binding.viewEliminatePaint.setErase(true)
                    }

                    MODE_RECOVER -> {
                        binding.viewEliminatePaint.apply {
                            clearDrawing(strokeOnly = true)
                            showIndicator(true)
                            setBrushSize(viewModel.size.value, false)
                            startRestore()
                        }
                    }
                }
            }
        }
    }

    override fun onActionUpOrCancel() {
        viewModel ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.runInpaint(requireContext(), binding.viewEliminatePaint.getDrawingAreaMask())
        }
    }

    override fun onTouchEvent(touchX: Float, touchY: Float) {}
}