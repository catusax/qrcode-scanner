package me.coolrc.qrcode.ui.main

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import me.coolrc.qrcode.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                0
            )
        }
        //监听二维码扫描结果
        setFragmentResultListener("qrcode") { key, bundle ->
            val result = bundle.getString(key)
            viewModel.data.value = result
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainFragmentBinding.inflate(layoutInflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.btnClear.setOnClickListener {
            viewModel.data.value = ""
        }
        binding.btnCopy.setOnClickListener {
            val clipboard =
                it.context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText("qr data", viewModel.data.value)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "copied", Toast.LENGTH_SHORT).show()

        }
        binding.btnPaste.setOnClickListener {
            val clipboard =
                it.context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val item = clipboard.primaryClip?.getItemAt(0)
            if (item != null) {
                val pasteData = item.text
                if (pasteData != null)
                    viewModel.data.value = pasteData.toString()
            }

        }
        binding.btnGenerate.setOnClickListener {
            if (viewModel.data.value == null || viewModel.data.value == "") {
                Toast.makeText(context, "no input!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val action = MainFragmentDirections.actionMainFragmentToImageFragment()
            it.findNavController().navigate(action)

        }
        binding.btnScan.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToScanFragment()
            it.findNavController().navigate(action)
        }

        return binding.root
    }

}