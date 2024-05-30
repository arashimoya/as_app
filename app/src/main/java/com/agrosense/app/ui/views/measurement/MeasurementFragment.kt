package com.agrosense.app.ui.views.measurement

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.agrosense.app.BluetoothActivity
import com.agrosense.app.R
import com.agrosense.app.dsl.MeasurementRepository
import com.agrosense.app.dsl.db.AgroSenseDatabase
import com.agrosense.app.rds.bluetooth.MeasurementManager
import com.agrosense.app.timeprovider.CurrentTimeProvider
import com.agrosense.app.ui.views.main.NavFragment
import com.agrosense.app.viewmodelfactory.MeasurementViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch


class MeasurementFragment : Fragment() {
    private lateinit var measurementViewModel: MeasurementViewModel

    private lateinit var textView: TextView
    private lateinit var backButton: ImageView
    private lateinit var fabStop: FloatingActionButton

    companion object {
        fun newInstance() = MeasurementFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        measurementViewModel =
            ViewModelProvider(
                requireActivity(),
                MeasurementViewModelFactory(
                    AgroSenseDatabase.getDatabase(requireContext()).measurementDao(),
                    MeasurementRepository.getInstance(
                        AgroSenseDatabase.getDatabase(requireContext()).measurementDao(),
                        CurrentTimeProvider()
                    ),
                    MeasurementManager((requireActivity() as BluetoothActivity).getCommunicationService()!!)
                )
            )[MeasurementViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_measurement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textView = view.findViewById(R.id.latest_temperature)
        viewLifecycleOwner.lifecycleScope.launch {
            measurementViewModel.lastTemperatureReading.collect { reading ->
                changeTextWithAnimation(
                    textView,
                    reading?.value?.let { "%.1f".format(it) } ?: "N/A")
            }
        }

        backButton = view.findViewById(R.id.backButton)
        backButton.setOnClickListener { onBackPressed() }

        fabStop = view.findViewById(R.id.stop_measurement)
        fabStop.setOnClickListener { stopMeasurement() }

    }

    private fun changeTextWithAnimation(textView: TextView, newText: String) {
        val fadeOut = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f).setDuration(200)
        val fadeIn = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f).setDuration(200)

        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                textView.text = newText
                fadeIn.start()
            }
        })
        fadeOut.start()
    }

    private fun onBackPressed() {
        (requireActivity() as BluetoothActivity).replaceFragment(NavFragment.newInstance())
    }

    private fun stopMeasurement() {
        measurementViewModel.stopMeasurement()
        onBackPressed()
    }

}