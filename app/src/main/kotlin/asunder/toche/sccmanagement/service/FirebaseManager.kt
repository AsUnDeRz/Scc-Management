package asunder.toche.sccmanagement.service

import android.net.Uri
import android.util.Log
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.Prefer
import asunder.toche.sccmanagement.preference.ROOT
import com.crashlytics.android.Crashlytics
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.snatik.storage.Storage
import io.paperdb.Paper
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File
import java.io.IOException


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
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(data: DataSnapshot) {
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

    fun updateTransaction(transaction : Model.Transaction){

    }

    fun getPathClone(path: String):String{
        val stor = Storage(storage.app.applicationContext)
        val rawFile = File(path)
        val externalPath = stor.externalStorageDirectory
        val newDir = externalPath + File.separator + Prefer.getUUID(this.context!!)
        return  newDir + File.separator + rawFile.absolutePath.substring(
                rawFile.absolutePath.lastIndexOf(File.separator)+1)
    }

    fun pushFileToFirebase(path:String,session: String){
        saveImage(path)
        /*
        storage.maxUploadRetryTimeMillis = 2000L
        val storageRef = storage.reference
        val file = Uri.fromFile(File(path))
        val riversRef = storageRef.child("images/${Prefer.getUUID(this.context!!)}/" + file.lastPathSegment)
        val uploadTask = if(session != ""){
            riversRef.putFile(Uri.parse(Prefer.getFile(context)), StorageMetadata.Builder().build(),Uri.parse(session))
        }else{
            riversRef.putFile(file)
        }
        saveImage(path)
        uploadTask.addOnFailureListener {
            System.out.println("Upload fail cause "+it.cause)
            System.out.println("Upload fail message"+it.message)
            System.out.println("Upload Fail")
        }.addOnSuccessListener {
            System.out.println("Upload Success ${file.lastPathSegment}")
            pushPathImageToDb(path)
        }.addOnProgressListener {
            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
            System.out.println("Upload is $progress% done")
        }.addOnPausedListener {
            System.out.println("Upload is paused")
        }
        */
    }

    fun pushPathImageToDb(path: String){
        Paper.book().write(ROOT.IMAGES,path)
        System.out.println("Upload Success $path")
    }

    fun deleteFile(path: String,pathInDevice:String){
        val stor = Storage(storage.app.applicationContext)
        launch(UI) {
            val result = async {
                stor.deleteFile(pathInDevice)
            }
            result.await()
            println("Delete File $pathInDevice")
        }
        /*
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
                    Crashlytics.log(it.message)
                }
                */
    }

    fun saveImage(path:String) {
        val stor = Storage(storage.app.applicationContext)
        val externalPath = stor.externalStorageDirectory
        val newDir = externalPath + File.separator + Prefer.getUUID(this.context!!)
        stor.createDirectory(newDir)
        val toPath = newDir+File.separator+File(path).absolutePath.substring(File(path).absolutePath.lastIndexOf(File.separator)+1)
        val rawFile = File(getPathClone(path))
        if (!rawFile.exists() && !rawFile.isDirectory){
            println("copy file $path \n to $toPath")
            stor.copy(path, toPath)
        }else{
            println("File already")
        }
    }

    fun downloadInLocalFile(path: String) {
        val stor = Storage(storage.app.applicationContext)
        val externalPath = stor.externalStorageDirectory
        val newDir = externalPath + File.separator + Prefer.getUUID(this.context!!)
        stor.createDirectory(newDir)

        val storageRef = storage.reference
        val content = storageRef.child(path)

        val file = File(newDir, content.name)
        try {
            stor.createDirectory(newDir)
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val fileDownloadTask = content.getFile(file)

        fileDownloadTask.addOnSuccessListener {
            println("DownloadFileSuccess"+file.absolutePath)

        }.addOnFailureListener { exception ->
            Crashlytics.log(exception.message)
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            println("onDownloadProgress $progress")
        }
    }

}