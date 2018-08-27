package asunder.toche.sccmanagement.service

import android.util.Base64
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.preference.ROOT
import io.paperdb.Paper
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.util.*

/**
 *Created by ToCHe on 24/8/2018 AD.
 */
object ImagesService{

/*
    var imagesList : MutableList<Model.ImageScc>? = null

    fun initService(){
        Paper.book().read<Model.MasterImage>(ROOT.IMAGESCC)?.let {
            imagesList = it.images
        }

        if (imagesList == null){
            imagesList = mutableListOf()
        }
        println(imagesList)

    }

    fun addImage(base64:String) : String {
        val uid = UUID.randomUUID().toString()
        ImagesService.addImage(Model.ImageScc(uid,base64))
        return uid
    }


    fun getImageScc(id: String) : ByteArray? {
        if (imagesList!!.isEmpty()){
            return null
        }
        val data = imagesList?.find { it.id ==id }

        return if (data != null ){
            Base64.decode(data.base64, Base64.DEFAULT)
        }else{
            null
        }
    }

    fun addImage(data:Model.ImageScc){
        imagesList?.add(data)
        println("Add image scc ${data.id}")
    }

    fun deleteImage(id:String){
        imagesList?.let { images ->
            images.filter { it.id == id }
                    .forEach {
                        imagesList?.remove(it)
                        println("Delete image at $it")
                    }
        }
        println("Delete image")
        saveToDB()
    }

    fun saveToDB() = async(UI) {
        try {
            val addContact = async {
                Paper.book().write(ROOT.IMAGESCC,Model.MasterImage(imagesList!!))
            }

            addContact.await()
            println("Save complete")
        }

        catch (e: Exception) {
        }
        finally {
        }
    }
*/
}