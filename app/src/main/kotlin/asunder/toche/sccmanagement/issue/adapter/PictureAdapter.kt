package asunder.toche.sccmanagement.issue.adapter

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.issue.ComponentListener
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_image_viewer.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import java.io.File

/**
 *Created by ToCHe on 4/5/2018 AD.
 */
class PictureAdapter(var listener: ComponentListener): RecyclerView.Adapter<PictureAdapter.PictureHolder>(){

    val pictures:MutableList<Model.Content> =  mutableListOf()

    fun updatePictures(data:MutableList<Model.Content>){
        pictures.clear()
        pictures.addAll(data)
        notifyDataSetChanged()
    }

    fun addPictures(data:MutableList<Model.Content>){
        pictures.addAll(data)
        notifyDataSetChanged()
    }

    fun addPicture(data:Model.Content){
        pictures.add(data)
        notifyDataSetChanged()
    }

    fun remove(position: Int){
        pictures.removeAt(position)
        notifyDataSetChanged()
    }

    fun clear(){
        pictures.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_picture,parent,false)
        return PictureHolder(view)
    }

    override fun getItemCount(): Int {
        return pictures.size
    }

    override fun onBindViewHolder(holder: PictureHolder, position: Int) {
        holder.bind(pictures[position],listener)
    }


    inner class PictureHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val imageStateDelete = itemView?.findViewById<ImageView>(R.id.imageStateDelete)
        val imageAction = itemView?.findViewById<AppCompatImageView>(R.id.picture)

        fun bind(picture: Model.Content,listener: ComponentListener){
            imageStateDelete?.isSelected = true
            imageStateDelete?.setOnClickListener {
                listener.OnPictureClick(pictures[adapterPosition],true,adapterPosition)
            }
            imageAction?.setOnClickListener {
                listener.OnPictureClick(pictures[adapterPosition],false,adapterPosition)
            }
            imageStateDelete?.let {
                Glide.with(itemView.context)
                        .load(R.drawable.ic_remove_white_24dp)
                        .into(it)
            }
            imageAction?.let {image ->
                val f = File(picture.local_path)
                if (f.exists()) {
                    Glide.with(itemView.context)
                            .load(File(picture.local_path))
                            .into(image)
                    println()
                }else{
                        FirebaseStorage.getInstance().reference.child(picture.cloud_url)
                                .downloadUrl
                                .addOnSuccessListener { result ->
                                    Glide.with(itemView.context)
                                            .load(result)
                                            .into(image)
                                }.addOnFailureListener {
                                    Glide.with(itemView.context)
                                            .load(R.drawable.mock_picture)
                                            .into(image)
                                }
                    println()
                }
            }
        }
    }
}