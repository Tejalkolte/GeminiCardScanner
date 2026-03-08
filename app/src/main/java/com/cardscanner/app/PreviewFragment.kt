package com.cardscanner.app

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cardscanner.app.databinding.FragmentPreviewBinding

class PreviewFragment : Fragment() {
    private var _b: FragmentPreviewBinding? = null
    private val b get() = _b!!
    private val vm: CardScanViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentPreviewBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val uri = Uri.parse(arguments?.getString("imageUri") ?: return)
        b.imgCard.setImageURI(uri)
        b.btnScan.setOnClickListener { vm.scanCard(requireContext(), uri) }
        b.btnRetake.setOnClickListener { findNavController().popBackStack() }

        vm.scanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ScanState.Idle -> {
                    b.progressBar.visibility = View.GONE
                    b.btnScan.isEnabled = true
                    b.statusText.text = "Tap Scan to extract contact info"
                }
                is ScanState.Loading -> {
                    b.progressBar.visibility = View.VISIBLE
                    b.btnScan.isEnabled = false
                    b.statusText.text = "Gemini AI is reading the card..."
                }
                is ScanState.Success -> {
                    b.progressBar.visibility = View.GONE
                    findNavController().navigate(
                        R.id.action_preview_to_result,
                        Bundle().apply { putParcelable("contact", state.contact) }
                    )
                    vm.resetState()
                }
                is ScanState.Error -> {
                    b.progressBar.visibility = View.GONE
                    b.btnScan.isEnabled = true
                    b.statusText.text = "Error: ${state.message}"
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
