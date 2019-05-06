package test.kozachenkotest.Adapters

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import test.kozachenkotest.FragmentMarker

class MarkersViewAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val fragments = ArrayList<FragmentMarker>()

    fun setFragments(list: ArrayList<FragmentMarker>){
        fragments.clear()
        fragments.addAll(list)
    }

    override fun getItem(position: Int): FragmentMarker? {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}
