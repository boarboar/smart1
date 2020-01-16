package com.example.android.weatherapp.common_ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.android.weatherapp.MainActivity
import com.example.android.weatherapp.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        // better be in onCreateView...
        (activity as MainActivity).toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }
}