package com.example.perfz.market

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.perfz.R
import com.example.perfz.databinding.FragmentMarketBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MarketFragment : Fragment(R.layout.fragment_market) {

    private lateinit var binding: FragmentMarketBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMarketBinding.bind(view)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        fetchFinancialData()
    }

    private fun fetchFinancialData() {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {

                val divisasDeferred = async(Dispatchers.IO) {
                    realizarPeticionSegura("https://api.frankfurter.app/latest?from=MXN")
                }

                val jsonDivisas = divisasDeferred.await()

                withContext(Dispatchers.Main) {
                    if (jsonDivisas.isNotEmpty()) {
                        val objDivisas = JSONObject(jsonDivisas)
                        val rates = objDivisas.getJSONObject("rates")

                        val usdToMxn = 1.0 / rates.getDouble("USD")
                        val eurToMxn = 1.0 / rates.getDouble("EUR")
                        val jpyToMxn = 1.0 / rates.getDouble("JPY")
                        val krwToMxn = 1.0 / rates.getDouble("KRW")
                        val gbpToMxn = 1.0 / rates.getDouble("GBP")

                        binding.tvUsdPrice.text = String.format("$%.2f", usdToMxn)
                        binding.tvEurPrice.text = String.format("$%.2f", eurToMxn)
                        binding.tvJpyPrice.text = String.format("$%.4f MXN", jpyToMxn)
                        binding.tvKrwPrice.text = String.format("$%.4f MXN", krwToMxn)
                        binding.tvGbpPrice.text = String.format("$%.2f MXN", gbpToMxn)
                    } else {
                        asignarValoresRespaldo()
                    }


                    binding.tvBtcPrice.text = "$1,214,580.00 MXN"
                    binding.tvEthPrice.text = "$64,230.50 MXN"


                    binding.tvSpyPrice.text = "$5,060.20"
                    binding.tvNasdaqPrice.text = "$16,248.50"

                    binding.progressBar.visibility = View.GONE
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    asignarValoresRespaldo()
                    binding.tvBtcPrice.text = "$1,214,580.00 MXN"
                    binding.tvEthPrice.text = "$64,230.50 MXN"
                    binding.tvSpyPrice.text = "$5,060.20"
                    binding.tvNasdaqPrice.text = "$16,248.50"
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun asignarValoresRespaldo() {
        binding.tvUsdPrice.text = "$17.27"
        binding.tvEurPrice.text = "$20.10"
        binding.tvJpyPrice.text = "$0.1102 MXN"
        binding.tvKrwPrice.text = "$0.0125 MXN"
        binding.tvGbpPrice.text = "$21.95 MXN"
    }

    private fun realizarPeticionSegura(urlString: String): String {
        var urlConnection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 4000
            urlConnection.readTimeout = 4000

            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                response.toString()
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        } finally {
            urlConnection?.disconnect()
        }
    }
}