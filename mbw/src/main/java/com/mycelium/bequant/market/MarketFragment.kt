package com.mycelium.bequant.market

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.tabs.TabLayoutMediator
import com.mycelium.bequant.BequantPreference
import com.mycelium.bequant.Constants
import com.mycelium.bequant.common.ErrorHandler
import com.mycelium.bequant.common.loader
import com.mycelium.bequant.kyc.BequantKycActivity
import com.mycelium.bequant.market.adapter.MarketFragmentAdapter
import com.mycelium.bequant.remote.SignRepository
import com.mycelium.bequant.sign.SignActivity
import com.mycelium.wallet.R
import kotlinx.android.synthetic.main.fragment_bequant_main.*


class MarketFragment : Fragment(R.layout.fragment_bequant_main) {

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            pager.setCurrentItem(1, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (!BequantPreference.isDemo() && !BequantPreference.hasKeys()) {
            loader(true)
            SignRepository.repository.getApiKeys(lifecycleScope, {
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent(Constants.ACTION_BEQUANT_KEYS))
            }, error = { _, message ->
                ErrorHandler(requireContext()).handle(message)
            }, finallyBlock = {
                loader(false)
            })
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter(Constants.ACTION_EXCHANGE))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pager.adapter = MarketFragmentAdapter(this)
        pager.offscreenPageLimit = 2
        TabLayoutMediator(tabs, pager) { tab, position ->
            when (position) {
                0 -> tab.text = "Markets"
                1 -> tab.text = "Exchange"
                2 -> tab.text = "Account"
            }
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bequant_market, menu)
        menu.findItem(R.id.logIn).isVisible = !BequantPreference.isLogged()
        menu.findItem(R.id.logOut).isVisible = BequantPreference.isLogged()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.supportCenter -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LINK_SUPPORT_CENTER)))
                    true
                }
                R.id.kyc -> {
                    startActivity(Intent(requireActivity(), BequantKycActivity::class.java))
                    true
                }
                R.id.logOut -> {
                    SignRepository.repository.logout()
                    activity?.finish()
                    startActivity(Intent(requireContext(), SignActivity::class.java))
                    true
                }
                R.id.logIn -> {
                    activity?.finish()
                    startActivity(Intent(requireContext(), SignActivity::class.java))
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
        super.onDestroy()
    }
}