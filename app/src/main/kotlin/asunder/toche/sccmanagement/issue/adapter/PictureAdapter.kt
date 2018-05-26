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
            imageAction?.let {
                Glide.with(itemView.context)
                        .load(File(picture.local_path))
                        .into(it)
            }

        }
    }
}