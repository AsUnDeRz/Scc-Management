package asunder.toche.sccmanagement.preference

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.Filter
import asunder.toche.sccmanagement.Model
import com.crashlytics.android.Crashlytics
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.snatik.storage.Storage
import io.paperdb.Paper
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.GsonBuilder
import com.google.gson.Gson
import java.io.*


/**
 *Created by ToCHe on 9/3/2018 AD.
 */
object Utils{

    fun scanForActivity(cont: Context?): Activity? {
        return when (cont) {
            null -> null
            is Activity -> cont
            is ContextWrapper -> scanForActivity(cont.baseContext)
            else -> null
        }
    }

    fun getCurrentDateForBackupFile():String{
        val fmtOut = SimpleDateFormat("yyyyMMdd_HH:mm:ss", Locale("en",""))
        return fmtOut.format(Date())
    }

    fun getCurrentDateShort() :String{
        val fmtOut = SimpleDateFormat("yyyy/MM/dd", Locale("en",""))
        return fmtOut.format(getCurrentDate())
    }

    fun getCurrentDateString() : String{
            val fmtOut = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale("en",""))
            return fmtOut.format(getCurrentDate())
    }

    fun getDateWithString(date :String) : Date{
        val fmtOut = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale("en",""))
        return fmtOut.parse(date)
    }
    fun getDateString(date :String) : Date{
        val fmtOut = SimpleDateFormat("yyyy/MM/dd", Locale("en",""))
        return fmtOut.parse(date)
    }

    fun format2DigiYMD(date: String):String{
        val fmtOut = SimpleDateFormat("yyMMdd", Locale("en",""))
        return fmtOut.format(getDateWithString(date))
    }

    fun format4DigiYMD(date: String):String{
        val fmtOut = SimpleDateFormat("yyyy/MM/dd", Locale("en",""))
        return fmtOut.format(getDateWithString(date))
    }

    fun getDateWithNumberFromCurrent(number:Int):Date{
        val calendar = Calendar.getInstance()
        calendar.time = getCurrentDate()
        calendar.add(Calendar.DATE,number)
        return calendar.time
    }

    fun getDateWithNumber(number:Int,date: Date):Date{
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE,number)
        return calendar.time
    }

    fun getPreviusDate() :Date{
        val calendar = Calendar.getInstance()
        calendar.time = getCurrentDate()
        calendar.add(Calendar.DATE,-1)
        return calendar.time
    }

    fun getDateStringWithDate(date: Date) : String{
        val fmtOut = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale("en",""))
        return fmtOut.format(date)
    }

    fun getCurrentDate():Date{
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR))
        //calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)+543)
        calendar.set(calendar[Calendar.YEAR],calendar[Calendar.MONTH],calendar[Calendar.DAY_OF_MONTH],0,0)
        return calendar.time
    }

    fun getTypeFaceMedium(context : Context):Typeface {
        //return Typeface.createFromAsset(context.assets, "fonts/set_medium.ttf")
        return Typeface.DEFAULT
    }

    interface OnFindCompanyListener {
        fun onResults(results: MutableList<Model.Contact>)
    }
    interface OnFindIssueListener {
        fun onResults(results: MutableList<Model.Issue>)
    }
    interface OnFindProductListener{
        fun onResults(results: MutableList<Model.Product>)
    }
    interface OnFindTransactionsListener{
        fun onResults(results: MutableList<Model.Transaction>)
    }


    fun findCompany(query: String, listener: OnFindCompanyListener, masterData:MutableList<Model.Contact>) {
            object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val suggestionList = mutableListOf<Model.Contact>()
                    if (!(constraint == null || constraint.isEmpty())) {
                        masterData.filterTo(suggestionList) {
                            it.company.contains(constraint.toString(),true) ||
                                    it.bill.contains(constraint.toString()) ||
                                    it.contact_name.contains(constraint.toString()) ||
                                    it.numbers.any { it.data.contains(constraint.toString()) } ||
                                    it.id.contains(constraint.toString()) ||
                                    it.numbers.any { it.type.contains(constraint.toString()) }
                        }
                    }
                    val results = FilterResults()
                    results.values = suggestionList
                    results.count = suggestionList.size
                    return results
                }

                override fun publishResults(constraint: CharSequence, results: FilterResults) {
                    if (results.count != 0) {
                        listener.onResults(results.values as MutableList<Model.Contact>)
                    }else{
                        if (constraint.isEmpty()){
                            listener.onResults(masterData)
                        }else {
                            listener.onResults(mutableListOf())
                        }
                    }
                }
            }.filter(query)

    }

    fun findIssue(query: String, listener: OnFindIssueListener,masterData:MutableList<Model.Issue>) {
            object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val suggestionList = mutableListOf<Model.Issue>()
                    if (!(constraint == null || constraint.isEmpty())) {
                        masterData.filterTo(suggestionList) {
                            it.issue_name.contains(constraint.toString()) ||
                                    it.issue_name.contains(constraint.toString()) ||
                                            it.date.contains(constraint.toString())
                        }
                    }
                    val results = FilterResults()
                    results.values = suggestionList
                    results.count = suggestionList.size
                    return results
                }

                override fun publishResults(constraint: CharSequence, results: FilterResults) {
                    if (results.count != 0) {
                        listener.onResults(results.values as MutableList<Model.Issue>)
                    }else{
                        listener.onResults(masterData)
                    }
                }
            }.filter(query)
    }
    fun findProduct(query: String, listener: OnFindProductListener,masterData:MutableList<Model.Product>){
        object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val suggestionList = mutableListOf<Model.Product>()
                if(!(constraint == null || constraint.isEmpty())){
                    masterData.filterTo(suggestionList){
                        it.product_name.contains(constraint.toString(),true) ||
                                it.product_desc.contains(constraint.toString(),true)
                    }
                }
                val result = FilterResults()
                result.values = suggestionList
                result.count = suggestionList.size
                return result
            }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.count != 0){
                    listener.onResults(results?.values as MutableList<Model.Product>)
                }else{
                    listener.onResults(masterData)
                }
            }
        }.filter(query)
    }

    fun findTransaction(query: String, listener : OnFindTransactionsListener,
                        masterData:MutableList<Model.Transaction>,optional:String?){
        object :Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val suggestionList = mutableListOf<Model.Transaction>()
                if(!(constraint == null || constraint.isEmpty())){
                    when(optional){
                        ROOT.PRODUCTS ->{
                            masterData.filterTo(suggestionList){
                                it.product_id == constraint.toString()
                            }
                        }
                        ROOT.CONTACTS ->{
                            masterData.filterTo(suggestionList){
                                it.company_id == constraint.toString()
                            }
                        }
                        else ->{
                            /*
                           masterData.filterTo(suggestionList) {
                                        it.company_name.contains(constraint.toString(), true) ||
                                                it.product_name.contains(constraint.toString(), true)
                           }
                           */
                            val sectionAll = masterData.filter {
                                it.company_name.contains(constraint.toString(), true) ||
                                        it.product_name.contains(constraint.toString(), true)
                            }
                            val contacts = sectionAll.filter {
                                it.company_name.contains(constraint.toString(), true)
                            }
                            val products = sectionAll.filter {
                                it.product_name.contains(constraint.toString(), true)
                            }

                            when {
                                contacts.size == products.size -> {
                                    suggestionList.addAll(sectionAll)
                                }
                                contacts.size > products.size -> {
                                    suggestionList.addAll(contacts)
                                }
                                else -> {
                                    suggestionList.addAll(products)
                                }
                            }


                        }
                    }

                }
                val result = FilterResults()
                result.values = suggestionList
                result.count = suggestionList.size
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if(results?.count != 0){
                    listener.onResults(results?.values as MutableList<Model.Transaction>)
                }else{
                    when(optional){
                        ROOT.PRODUCTS ->{
                            listener.onResults(mutableListOf())
                        }
                        else ->{
                            listener.onResults(masterData)
                        }
                    }
                }
            }

        }.filter(query)
    }


    fun getPath(context: Context):String{
        val externalPath = Environment.getExternalStorageDirectory().absolutePath
        return externalPath + File.separator + Prefer.getUUID(context)
    }

    fun exportDB(context: Context){
        createFileDB(context)
        val path = getPath(context)
        var contact = Paper.book().read<Model.ContactUser>(ROOT.CONTACTS)
        var issue = Paper.book().read<Model.IssueUser>(ROOT.ISSUE)
        var product = Paper.book().read<Model.ProductUser>(ROOT.PRODUCTS)
        var transaction = Paper.book().read<Model.TransactionUser>(ROOT.TRANSACTIONS)
        //var images = Paper.book().read<Model.MasterImage>(ROOT.IMAGESCC)

        if (contact == null){
            contact = Model.ContactUser()
        }
        if (issue == null){
            issue = Model.IssueUser()
        }
        if (product == null){
            product = Model.ProductUser()
        }
        if (transaction ==null){
            transaction = Model.TransactionUser()
        }

        val masterData = Model.MasterData(
                contact,
                issue,
                product,
                transaction,
                Utils.getCurrentDateString()
        )
        FileWriter(File(path + File.separator + "master.json")).use { write ->
                val gson = GsonBuilder().create()
                gson.toJson(masterData,write)
        }

        val masterContact = Model.MasterData(
                contactUser = contact,
                create_date = Utils.getCurrentDateString())
        FileWriter(File(path+ File.separator + "contacts.json")).use {
            val gson = GsonBuilder().create()
            gson.toJson(masterContact,it)
        }

        val masterProduct = Model.MasterData(
                productUser = product,
                create_date = Utils.getCurrentDateString())
        FileWriter(File(path+ File.separator + "products.json")).use {
            val gson = GsonBuilder().create()
            gson.toJson(masterProduct,it)
        }

    }
    fun importDB(context: Context){
        val path = getPath(context)
        Paper.book().destroy()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val mData = gson.fromJson(FileReader(File(path+File.separator+"master.json")),
                Model.MasterData::class.java)
        println(gson.toJson(mData))
        if (mData != null) {
            Paper.book().write(ROOT.CONTACTS, mData.contactUser)
            Paper.book().write(ROOT.ISSUE, mData.issueUser)
            Paper.book().write(ROOT.PRODUCTS, mData.productUser)
            Paper.book().write(ROOT.TRANSACTIONS, mData.transactionUser)

            val firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
            val childUpdates = HashMap<String,Any>()
            mData.contactUser.contacts.forEach {
                childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.CONTACTS}/${it.id}"] = it
            }
            mData.issueUser.issues.forEach {
                childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.ISSUE}/${it.id}"] = it
            }
            mData.productUser.products.forEach {
                childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.PRODUCTS}/${it.id}"] = it
            }
            mData.transactionUser.transactions.forEach {
                childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.TRANSACTIONS}/${it.id}"] = it
            }

            if (mData.contactUser.contacts.isEmpty()){
                childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.CONTACTS}"] = mData.contactUser
            }
            if (mData.issueUser.issues.isEmpty()){
                childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.ISSUE}"] = mData.issueUser
            }
            if (mData.productUser.products.isEmpty()) {
                childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.PRODUCTS}"] = mData.productUser
            }
            if (mData.transactionUser.transactions.isEmpty()){
                childUpdates["${ROOT.USERS}/${Prefer.getUUID(context)}/${ROOT.TRANSACTIONS}"] = mData.transactionUser
            }

            firebase.updateChildren(childUpdates) { databaseError, _ ->
                if (databaseError != null) {
                    Crashlytics.log(databaseError.message)
                    System.out.println("Data could not be saved " + databaseError.message)
                } else {
                    System.out.println("Restore data successfully.")
                }
            }
        }
    }

    fun createFileDB(context: Context) {
        val stor = Storage(context)
        val newDir = getPath(context)
        stor.createDirectory(newDir)
    }

    fun checkTypePrice(typePrice:String):String{
        return when(typePrice){
            ROOT.NOVAT->{
                "(B)"
            }
            ROOT.VAT ->{
                "(A)"
            }
            ROOT.CASH->{
                "(C)"
            }
            else ->{
                ""
            }
        }
    }

    fun encodeImage(path: String,context: Context): String {
        //val bm = getBitmap(path,context)
        val bm =  BitmapFactory.decodeFile(path)
        return if (bm != null) {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            byteArrayToBase64(baos.toByteArray())
        } else {
            ""
        }
    }

    private fun getBitmap(path: String,context: Context): Bitmap? {

        val uri = Uri.fromFile(File(path))
        var `in`: InputStream? = null
        val cr = context.contentResolver
        try {
            val IMAGE_MAX_SIZE = 450000 // 1.2 MP
            `in` = cr.openInputStream(uri)

            // Decode image size
            var o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`in`, null, o)
            `in`!!.close()


            var scale = 1
            while (o.outWidth * o.outHeight * (1 / Math.pow(scale.toDouble(), 2.0)) > IMAGE_MAX_SIZE) {
                scale++
            }

            var b: Bitmap? = null
            `in` = cr.openInputStream(uri)
            if (scale > 1) {
                scale--
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = BitmapFactory.Options()
                o.inSampleSize = scale
                b = BitmapFactory.decodeStream(`in`, null, o)

                // resize to desired dimensions
                val height = b!!.height
                val width = b.width
                //val y = Math.sqrt(IMAGE_MAX_SIZE / (width.toDouble() / height))
                //val x = y / height * width

                val scaledBitmap = Bitmap.createScaledBitmap(b, height,
                        width, true)
                b.recycle()
                b = scaledBitmap
                b = checkExif(path, b)
                System.gc()
            } else {
                b = BitmapFactory.decodeStream(`in`)
            }
            `in`!!.close()
            return b
        } catch (e: IOException) {
            return null
        }

    }

    private fun checkExif(path: String, bitmap: Bitmap?): Bitmap? {
        var bm = bitmap
        val exif: ExifInterface
        return try {
            exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                bm = rotateBitmap(bm, 90)
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                bm = rotateBitmap(bm, 180)
            }
            bm
        } catch (e: IOException) {
            e.printStackTrace()
            bm
        }

    }

    fun rotateBitmap(bitmap: Bitmap?, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun byteArrayToBase64(byteArrayImage: ByteArray): String {
        //"data:image/jpeg;base64,"
        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT)
    }





}