package me.coolrc.qrcode.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.sumimakito.awesomeqr.AwesomeQrRenderer
import com.github.sumimakito.awesomeqr.RenderResult
import com.github.sumimakito.awesomeqr.option.RenderOption
import com.github.sumimakito.awesomeqr.option.color.Color
import me.coolrc.qrcode.databinding.FragmentImageBinding

class ImageFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentImageBinding.inflate(layoutInflater)
        binding.text.text = viewModel.data.value

        val color = Color()
        color.light = 0xFFFFFFFF.toInt() // for blank spaces
        color.dark = android.graphics.Color.BLACK // for non-blank spaces
        color.background =
            0xFFFFFFFF.toInt() // for the background (will be overriden by background images, if set)
        color.auto =
            false // set to true to automatically pick out colors from the background image (will only work if background image is present)

        val renderOption = RenderOption()
        renderOption.content = viewModel.data.value.toString() // content to encode
        renderOption.size = 800 // size of the final QR code image
        renderOption.color = color
        renderOption.patternScale = 1f
        renderOption.borderWidth = 20 // width of the empty space around the QR code
        renderOption.roundedPatterns =
            true // (optional) if true, blocks will be drawn as dots instead
        renderOption.clearBorder =
            true // if set to true, the background will NOT be drawn on the border area


        try {
            val result = AwesomeQrRenderer.render(renderOption)
            when {
                result.bitmap != null -> {
                    // play with the bitmap
                    binding.qrImage.setImageBitmap(result.bitmap)
                }
                result.type == RenderResult.OutputType.GIF -> {
                    // If your Background is a GifBackground, the image
                    // will be saved to the output file set in GifBackground
                    // instead of being returned here. As a result, the
                    // result.bitmap will be null.
                }
                else -> {
                    // Oops, something gone wrong.
                }
            }
        } catch (e: Exception) {
            Log.e("scan", e.stackTraceToString())
            e.printStackTrace()
            // Oops, something gone wrong.
        }

        return binding.root
    }
}