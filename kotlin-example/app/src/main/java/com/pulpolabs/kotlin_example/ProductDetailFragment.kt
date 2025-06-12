package com.pulpolabs.kotlin_example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

data class MockProduct(
    val id: Int,
    val name: String,
    val description: String,
    val price: String
)

class ProductDetailFragment : Fragment() {

    interface ProductDetailListener {
        fun onProductTryOnClicked()
        fun onBackClicked()
    }

    private var listener: ProductDetailListener? = null
    private var product: MockProduct? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val productId = it.getInt(ARG_PRODUCT_ID)
            product = getMockProduct(productId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)
        
        val productTitle: TextView = view.findViewById(R.id.product_title)
        val productDescription: TextView = view.findViewById(R.id.product_description)
        val productPrice: TextView = view.findViewById(R.id.product_price)
        val tryOnButton: Button = view.findViewById(R.id.try_on_button)
        val backButton: Button = view.findViewById(R.id.back_button)
        
        product?.let {
            productTitle.text = it.name
            productDescription.text = it.description
            productPrice.text = it.price
        }
        
        tryOnButton.setOnClickListener {
            listener?.onProductTryOnClicked()
        }
        
        backButton.setOnClickListener {
            listener?.onBackClicked()
        }
        
        return view
    }

    fun setProductDetailListener(listener: ProductDetailListener) {
        this.listener = listener
    }

    private fun getMockProduct(productId: Int): MockProduct {
        return when (productId) {
            1 -> MockProduct(1, "Glamour Lipstick", "Beautiful matte finish lipstick with long-lasting color", "$19.99")
            2 -> MockProduct(2, "Foundation Pro", "Full coverage foundation for all-day wear", "$29.99")
            3 -> MockProduct(3, "Eye Shadow Palette", "12-color palette with shimmer and matte finishes", "$24.99")
            else -> MockProduct(1, "Glamour Lipstick", "Beautiful matte finish lipstick with long-lasting color", "$19.99")
        }
    }

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"

        @JvmStatic
        fun newInstance(productId: Int) =
            ProductDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PRODUCT_ID, productId)
                }
            }
    }
} 