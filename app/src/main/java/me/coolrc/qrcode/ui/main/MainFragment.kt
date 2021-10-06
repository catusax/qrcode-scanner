/*
 *     qrcode-scanner : simple qrcode scanner and generator for android
 *     Copyright (C) <2021>  <coolrc>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import me.coolrc.qrcode.utils.Constraints

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //监听二维码扫描结果
        setFragmentResultListener(Constraints.SCAN_RESULT) { key, bundle ->
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