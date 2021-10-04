package me.coolrc.qrcode.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import me.coolrc.qrcode.R
import me.coolrc.qrcode.databinding.FragmentImageBinding
import me.coolrc.qrcode.utils.QRCodeBitmap

class ImageFragment : Fragment() {
    val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentImageBinding.inflate(layoutInflater)
        binding.text.text = viewModel.data.value

        //生成二维码
        val qrImg = QRCodeBitmap.createQRCode(viewModel.data.value, 1000)!!
        binding.qrImage.setImageBitmap(qrImg)

        return binding.root
    }

}