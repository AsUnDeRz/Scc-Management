package asunder.toche.sccmanagement.service

import android.net.Uri
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.preference.Utils
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.google.gson.internal.bind.util.ISO8601Utils
import io.paperdb.Paper
import java.io.File
import java.util.*
import kotlin.collections.HashMap


/**
 *Created by ToCHe on 27/2/2018 AD.
 */
class FirebaseManager{

    private var firebase : DatabaseReference = FirebaseDatabase.getInstance().reference
    private var storage = FirebaseStorage.getInstance()
    val context = FirebaseApp.getInstance()?.applicationContext

    private val TAG = " FIREBASEMANAGER"



    fun fetchData(uid:String){
        firebase.child("users/$uid").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(data: DataSnapshot?) {
                val menu: MutableList<Model.Contact> = mutableListOf()
                Log.d(TAG,"Data before decode"+data?.value.toString())
                /*
                data?.child("contact")?.children?.mapNotNullTo(menu)
                {
                    it.getValue<Model.Contact>(Model.Contact::class.java)
                }

                menu.forEach {
                    Log.d(TAG,"Contact = "+it.toString())
                }
                */
                for(d in data?.child("contact")?.children!!){
                    //Log.d(TAG,"Data children"+d.value+" key =${d.key}")
                    val contact = d.getValue(Model.Contact::class.java)
                    Log.d(TAG,"Data after encode"+contact+ "Key = ${d.key}")
                }
            }
        })

    }

    fun pushNewIssue(issue: Model.Issue){

    }

    fun updateIssue(issue: Model.Issue){

    }

    fun pushNewProduct(product: Model.Product){

    }

    fun updateProduct(product: Model.Product){

    }


    fun pushTransaction(transaction : Model.Transaction){

    }

    fun updateTransaction(transaction : Model.Transaction){

    }

    fun pushFileToFirebase(path:String,session: String){
        storage.maxUploadRetryTimeMillis = 2000L
        val storageRef = storage.reference
        val file = Uri.fromFile(File(path))
        val riversRef = storageRef.child("images/${Prefer.getUUID(this.context!!)}/" + file.lastPathSegment)
        val uploadTask = if(session != ""){
            riversRef.putFile(Uri.parse(Prefer.getFile(context)), StorageMetadata.Builder().build(),Uri.parse(session))
        }else{
            riversRef.putFile(file)
        }
        uploadTask.addOnFailureListener({
            val filePath = Uri.fromFile(File(path))
            //val uploadsession = uploadTask.snapshot.uploadSessionUri
            //uploadsession?.let { it1 -> Prefer.saveSession(it1,filePath, context) }
            System.out.println("Upload fail cause "+it.cause)
            System.out.println("Upload fail message"+it.message)
            System.out.println("Upload Fail")
        }).addOnSuccessListener({
            System.out.println("Upload Success ${file.lastPathSegment}")
            pushPathImageToDb(path)
        }).addOnProgressListener({
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            System.out.println("Upload is $progress% done")
        }).addOnPausedListener( {
            System.out.println("Upload is paused")
        })
    }

    fun pushPathImageToDb(path: String){
        Paper.book().write(ROOT.IMAGES,path)
        System.out.println("Upload Success $path")
    }

    fun deleteFile(path: String){
        val storageRef = storage.reference
        val desertRef = storageRef.child(path)
        desertRef
                .delete()
                .addOnCompleteListener {
                    System.out.println("Delete Complete ")
                }
                .addOnFailureListener {
                    val errorCode = (it as StorageException).errorCode
                    val errorMessage = it.message
                    System.out.println("Delete $errorCode  $errorMessage")
                }
    }



}