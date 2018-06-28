package asunder.toche.sccmanagement.issue.adapter

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.issue.ComponentListener
import asunder.toche.sccmanagement.custom.textview.TxtMedium
import com.bumptech.glide.Glide
import java.io.File

/**
 *Created by ToCHe on 4/5/2018 AD.
 */
class FileAdapter(var listener: ComponentListener): RecyclerView.Adapter<FileAdapter.FileHolder>(){

    val files:MutableList<Model.Content> =  mutableListOf()

    fun updateFiles(data:MutableList<Model.Content>){
        files.clear()
        files.addAll(data)
        notifyDataSetChanged()
    }
    fun addFiles(data:MutableList<Model.Content>){
        files.addAll(data)
        notifyDataSetChanged()
    }

    fun addFile(data:Model.Content){
        files.add(data)
        notifyDataSetChanged()
    }

    fun remove(position: Int){
        files.removeAt(position)
        notifyDataSetChanged()
    }

    fun clear(){
        files.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file,parent,false)
        return FileHolder(view)
    }

    override fun getItemCount(): Int {
        return files.size
    }

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        holder.bind(files[position],listener)
    }


    inner class FileHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val edtContent = itemView?.findViewById<TxtMedium>(R.id.txtFile)
        val imageStateDelete = itemView?.findViewById<ImageView>(R.id.imageStateDelete)

        fun bind(content:Model.Content,listener: ComponentListener){
            val file = Uri.fromFile(File(content.local_path))
            edtContent?.text = file?.lastPathSegment
            imageStateDelete?.isSelected = true
            imageStateDelete?.setOnClickListener {
                listener.OnFileClick(files[adapterPosition],true,adapterPosition)
            }
            edtContent?.setOnClickListener {
                listener.OnFileClick(files[adapterPosition],false,adapterPosition)
            }
            imageStateDelete?.let {
                Glide.with(itemView.context)
                        .load(R.drawable.ic_remove_white_24dp)
                        .into(it)
            }
        }
    }
}