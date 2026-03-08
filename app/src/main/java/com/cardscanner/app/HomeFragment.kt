package com.cardscanner.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cardscanner.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val m = requireActivity() as MainActivity
        b.btnCamera.setOnClickListener { m.requestCameraOrLaunch() }
        b.btnGallery.setOnClickListener { m.launchGallery() }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
