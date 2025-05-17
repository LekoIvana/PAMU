package ba.sum.fpmoz.pamu

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AdminPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2  // Dvije kartice: Korisnici i Usluge

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UsersFragment()
            1 -> ServicesFragment()
            else -> throw IllegalStateException("Nepozicija kartice: $position")
        }
    }
}
