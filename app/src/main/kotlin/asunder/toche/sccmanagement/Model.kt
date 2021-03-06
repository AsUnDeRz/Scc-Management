package asunder.toche.sccmanagement

import android.annotation.SuppressLint
import android.os.Parcelable
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.parcel.Parcelize

/**
 *Created by ToCHe on 27/2/2018 AD.
 */
object Model{


    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Contact(var id:String = "",
                       val company:String = "",
                       val bill:String = "",
                       val contact_name:String = "",
                       val numbers:MutableList<Channel> = mutableListOf(),
                       val email:MutableList<Channel> = mutableListOf(),
                       val websites:MutableList<Channel> = mutableListOf(),
                       val addresses :MutableList<Address> = mutableListOf(),
                       val type_number:MutableList<String> = mutableListOf()) : Parcelable

    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Address(val address_type:String = "",
                       val address_factory:String = "",
                       var images_id:String = "",
                       var path_img_map:String ="",
                       val map_longitude:String = "",
                       val map_latitude:String ="",
                       var type:String ="") :Parcelable

    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Channel(var data:String ="",
                      var type:String ="") : Parcelable

    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Issue(var id:String = "",
                     val status:String = "",
                     var company_id:String = "",
                     val issue_name:String = "",
                     val issue_desc:String = "",
                     val date:String = "",
                     var pictures:MutableList<Content> = mutableListOf(),
                     var files:MutableList<Content> = mutableListOf()) : Parcelable

    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Content(var local_path:String = "",
                       var cloud_url:String = "") : Parcelable

    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Product(var id:String = "",
                       val product_name:String = "",
                       val product_desc:String = "",
                       val import_from:String = "",
                       val pack_size:String = "",
                       val desc:String = "",
                       val date:String ="",
                       val medium_rate:MutableList<MediumRate> = mutableListOf(),
                       val types:MutableList<String> = mutableListOf(),
                       var pictures:MutableList<ContentForProduct> = mutableListOf(),
                       var files:MutableList<ContentForProduct> = mutableListOf()) : Parcelable

    @SuppressLint("ParcelCreator")
    @Parcelize
    data class ContentForProduct(var local_path:String = "",
                                 var cloud_url:String = "",
                                 var title_type:String = "") : Parcelable

    @SuppressLint("ParcelCreator")
    @Parcelize
    data class MediumRate(val price:String = "",
                          val vat:Boolean = false,
                          val date:String = "",
                          val note:String = "",
                          var default:Boolean = false,
                          var priceNoVat:String = "") : Parcelable


    @SuppressLint("ParcelCreator")
    @Parcelize
    data class Transaction(var id:String = "",
                           val company_id:String = "",
                           val company_name:String = "",
                           val product_id:String = "",
                           val product_name: String ="",
                           var medium_price:String = "",
                           var date:String = "",
                           val desc:String = "",
                           val sale_price:MutableList<SalePrice> = ArrayList(),
                           var medium_price_novat:String = "") : Parcelable


    @SuppressLint("ParcelCreator")
    @Parcelize
    data class SalePrice(val price:String ="",
                         val vat:String = "",
                         val values:String = "",
                         val date:String = "",
                         val note:String = "") : Parcelable
    data class User(val uid:String="",
                    val status_user:String ="",
                    val request_date:String ="",
                    val approve_date:String ="",
                    val email:String ="",
                    val auth_with:String = "",
                    val contacts:MutableList<Contact> = ArrayList(),
                    val issues:MutableList<Issue> = ArrayList(),
                    val products:MutableList<Product> = ArrayList(),
                    val transactions:MutableList<Transaction> = ArrayList())
    data class UserAuth(val uid:String = "",
                        var status_user:String = "",
                        val request_date:String = "",
                        val approve_date:String = "",
                        val email:String = "",
                        val auth_with:String = "")
    data class ManagementUser(val users:MutableList<UserAuth> = ArrayList())
    data class ContactUser(val contacts:MutableList<Contact> = ArrayList())
    data class IssueUser(val issues:MutableList<Issue> = mutableListOf())
    data class ProductUser(val products: MutableList<Product> = mutableListOf())
    data class TransactionUser(val transactions: MutableList<Transaction> = mutableListOf())
    data class MasterGroup(var groupDate:MutableList<String> = mutableListOf(),
                           var groupCompany:MutableList<String> = mutableListOf(),
                           var resultMap:MutableMap<String,SectionedRecyclerViewAdapter> = mutableMapOf())
    data class MasterData(val contactUser: ContactUser = ContactUser(),
                          val issueUser: IssueUser = IssueUser(),
                          val productUser: ProductUser = ProductUser(),
                          val transactionUser: TransactionUser = TransactionUser(),
                          val create_date:String)

    data class MasterImage(val images:MutableList<ImageScc> = mutableListOf())
    data class ImageScc(val id:String="",
                     val base64:String="")

}