package asunder.toche.sccmanagement

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

/**
 *Created by ToCHe on 27/2/2018 AD.
 */
object Model{

    data class Contact(var id:String = "",
                       val company:String = "",
                       val bill:String = "",
                       val contact_name:String = "",
                       val mobile:String = "",
                       val fax:String = "",
                       val telephone:String ="",
                       val email:String = "",
                       val website:String = "",
                       val address_type:String = "",
                       val address_factory:String = "",
                       val address:String = "",
                       var url_img_map:String = "",
                       var path_img_map:String ="",
                       val map_latitude:String = "",
                       val map_longitude:String = "")
    data class Issue(var id:String = "",
                     val status:String = "",
                     var company_id:String = "",
                     val issue_name:String = "",
                     val issue_desc:String = "",
                     val date:String = "",
                     var image_url:String = "",
                     var image_path:String = "",
                     var file_url:String = "",
                     var file_path:String = "")
    data class Product(var id:String = "",
                       val product_name:String = "",
                       val product_desc:String = "",
                       val import_from:String = "",
                       val pack_size:String = "",
                       val desc:String = "",
                       val date:String ="",
                       val medium_rate:MutableList<MediumRate> = ArrayList())
    data class MediumRate(val price:String = "",
                          val vat:Boolean = false,
                          val values:String = "",
                          val date:String = "",
                          val note:String = "",
                          var default:Boolean = false)
    data class Transaction(var id:String = "",
                           val company_id:String = "",
                           val product_id:String = "",
                           val medium_price:String = "",
                           val date:String = "",
                           val desc:String = "",
                           val sale_price:MutableList<SalePrice> = ArrayList())
    data class SalePrice(val price:String ="",
                         val vat:Boolean = false,
                         val values:String = "",
                         val date:String = "",
                         val note:String = "")
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

}