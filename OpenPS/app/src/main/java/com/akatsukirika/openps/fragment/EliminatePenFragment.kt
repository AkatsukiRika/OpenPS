package com.akatsukirika.openps.fragment

import android.app.ProgressDialog
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.akatsukirika.openps.R
import com.akatsukirika.openps.compose.MODE_ERASER
import com.akatsukirika.openps.compose.MODE_LARIAT
import com.akatsukirika.openps.compose.MODE_PAINT
import com.akatsukirika.openps.compose.MODE_GENERATE
import com.akatsukirika.openps.compose.STATUS_LOADING
import com.akatsukirika.openps.compose.STATUS_SUCCESS
import com.akatsukirika.openps.databinding.FragmentEliminatePenBinding
import com.akatsukirika.openps.store.SettingsStore
import com.akatsukirika.openps.utils.BitmapUtils.isFullyTransparent
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
) : Fragment(), EliminateFragmentCallback {
    constructor() : this(null, null)

    private lateinit var binding: FragmentEliminatePenBinding

    private var isInit = false

    private var progressDialog: ProgressDialog? = null

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
            setCallback(object : EliminatePaintView.Callback {
                override fun onActionUpOrCancel() {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val isFullyTransparent = binding.viewEliminatePaint.getDrawingAreaBitmap()?.isFullyTransparent()
                        viewModel?.readyToGenerate?.emit(isFullyTransparent == false)
                    }
                }

                override fun onTouchEvent(touchX: Float, touchY: Float) {}
            })
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

                    MODE_GENERATE -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val isFullyTransparent = binding.viewEliminatePaint.getDrawingAreaBitmap()?.isFullyTransparent()
                            if (isFullyTransparent == false) {
                                viewModel.runInpaint(requireContext(), binding.viewEliminatePaint.getDrawingAreaMask())
                            }
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel?.inpaintStatus?.collect {
                if (it == STATUS_LOADING) {
                    progressDialog = ProgressDialog(requireContext()).apply {
                        setCancelable(false)
                        setCanceledOnTouchOutside(false)
                        setMessage(requireContext().getString(R.string.inpaint_loading))
                    }
                    progressDialog?.show()
                } else {
                    progressDialog?.dismiss()
                    if (it == STATUS_SUCCESS) {
                        binding.viewEliminatePaint.clearDrawing(strokeOnly = true)
                        viewModel.mode.emit(MODE_PAINT)
                        viewModel.readyToGenerate.emit(false)
                    }
                }
            }
        }
    }
}