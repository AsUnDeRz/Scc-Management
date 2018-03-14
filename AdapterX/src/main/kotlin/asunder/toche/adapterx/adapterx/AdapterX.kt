package asunder.toche.adapterx.adapterx

import android.databinding.DataBindingUtil
import android.databinding.ObservableList
import android.databinding.OnRebindCallback
import android.databinding.ViewDataBinding
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup


open class AdapterX(private var list: List<Any>,
                    private val variable: Int? = null,
                    stableIds: Boolean = false) : RecyclerView.Adapter<HolderX<ViewDataBinding>>(){


    constructor(list: List<Any>) : this(list, null, false)
    constructor(list: List<Any>, variable: Int) : this(list, variable, false)
    constructor(list: List<Any>, stableIds: Boolean) : this(list, null, stableIds)

    private val DATA_INVALIDATION = Any()
    private val callback = ObservableListCallback(this)
    private var recyclerView: RecyclerView? = null
    private var inflater: LayoutInflater? = null

    private val map = mutableMapOf<Class<*>, BaseTypeX>()
    private var layoutHandler: LayoutHandler? = null
    private var typeHandler: TypeHandler? = null


    init {
        setHasStableIds(stableIds)
    }





    @JvmOverloads
    fun <T : Any> map(clazz: Class<T>, layout: Int, variable: Int? = null)
            = apply { map[clazz] = BaseTypeX(layout, variable) }

    inline fun <reified T : Any> map(layout: Int, variable: Int? = null)
            = map(T::class.java, layout, variable)

    fun <T : Any> map(clazz: Class<T>, type: AbsTypeX<*>)
            = apply { map[clazz] = type }

    inline fun <reified T : Any> map(type: AbsTypeX<*>)
            = map(T::class.java, type)

    inline fun <reified T : Any, B : ViewDataBinding> map(layout: Int,
                                                          variable: Int? = null,
                                                          noinline f: (TypeX<B>.() -> Unit)? = null)
            = map(T::class.java, TypeX<B>(layout, variable).apply { f?.invoke(this) })

    fun handler(handler: Handler) = apply {
        when (handler) {
            is LayoutHandler -> {
                if (variable == null) {
                    throw IllegalStateException("No variable specified in AdapterX constructor")
                }
                layoutHandler = handler
            }
            is TypeHandler -> typeHandler = handler
        }
    }

    inline fun layout(crossinline f: (Any, Int) -> Int) = handler(object : LayoutHandler {
        override fun getItemLayout(item: Any, position: Int) = f(item, position)
    })

    inline fun type(crossinline f: (Any, Int) -> AbsTypeX<*>?) = handler(object : TypeHandler {
        override fun getItemType(item: Any, position: Int) = f(item, position)
    })

    fun into(recyclerView: RecyclerView) = apply { recyclerView.adapter = this }



    override fun onCreateViewHolder(view: ViewGroup, viewType: Int): HolderX<ViewDataBinding> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, viewType, view, false)
        val holder = HolderX(binding)
        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding) = recyclerView?.isComputingLayout ?: false
            override fun onCanceled(binding: ViewDataBinding) {
                if (recyclerView?.isComputingLayout ?: true) {
                    return
                }
                val position = holder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position, DATA_INVALIDATION)
                }
            }
        })
        return holder
    }

    override fun onBindViewHolder(holder: HolderX<ViewDataBinding>, position: Int) {
        val type = getType(position)!!

        holder.binding.setVariable(getVariable(type), list[position])
        holder.binding.executePendingBindings()
        @Suppress("UNCHECKED_CAST")
        if (type is AbsTypeX<*>) {
            if (!holder.created) {
                notifyCreate(holder, type as AbsTypeX<ViewDataBinding>)
            }
            notifyBind(holder, type as AbsTypeX<ViewDataBinding>)
        }
    }

    override fun onBindViewHolder(holder: HolderX<ViewDataBinding>, position: Int, payloads: List<Any>) {
        if (isForDataBinding(payloads)) {
            holder.binding.executePendingBindings()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onViewRecycled(holder: HolderX<ViewDataBinding>) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val type = getType(position)!!
            if (type is AbsTypeX<*>) {
                @Suppress("UNCHECKED_CAST")
                notifyRecycle(holder, type as AbsTypeX<ViewDataBinding>)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        if (hasStableIds()) {
            val item = list[position]
            if (item is StableId) {
                return item.stableId
            } else {
                throw IllegalStateException("${item.javaClass.simpleName} must implement StableId interface.")
            }
        } else {
            return super.getItemId(position)
        }
    }

    override fun getItemCount() = list.size

    override fun onAttachedToRecyclerView(rv: RecyclerView) {
        if (recyclerView == null && list is ObservableList) {
            (list as ObservableList<Any>).addOnListChangedCallback(callback)
        }
        recyclerView = rv
        inflater = LayoutInflater.from(rv.context)
    }

    override fun onDetachedFromRecyclerView(rv: RecyclerView) {
        if (recyclerView != null && list is ObservableList) {
            (list as ObservableList<Any>).removeOnListChangedCallback(callback)
        }
        recyclerView = null
    }

    override fun getItemViewType(position: Int)
            = layoutHandler?.getItemLayout(list[position], position)
            ?: typeHandler?.getItemType(list[position], position)?.layout
            ?: getType(position)?.layout
            ?: throw RuntimeException("Invalid object at position $position: ${list[position].javaClass}")

    private fun getType(position: Int)
            = typeHandler?.getItemType(list[position], position)
            ?: map[list[position].javaClass]

    private fun getVariable(type: BaseTypeX)
            = type.variable
            ?: variable
            ?: throw IllegalStateException("No variable specified for type ${type.javaClass.simpleName}")

    private fun isForDataBinding(payloads: List<Any>): Boolean {
        if (payloads.isEmpty()) {
            return false
        }
        payloads.forEach {
            if (it != DATA_INVALIDATION) {
                return false
            }
        }
        return true
    }

    private fun notifyCreate(holder: HolderX<ViewDataBinding>, type: AbsTypeX<ViewDataBinding>) {
        when (type) {
            is TypeX -> {
                setClickListeners(holder, type)
                type.onCreate?.invoke(holder)
            }
            is ItemTypeX -> type.onCreate(holder)
        }
        holder.created = true
    }

    private fun notifyBind(holder: HolderX<ViewDataBinding>, type: AbsTypeX<ViewDataBinding>) {
        when (type) {
            is TypeX -> type.onBind?.invoke(holder)
            is ItemTypeX -> type.onBind(holder)
        }
    }

    private fun notifyRecycle(holder: HolderX<ViewDataBinding>, type: AbsTypeX<ViewDataBinding>) {
        when (type) {
            is TypeX -> type.onRecycle?.invoke(holder)
            is ItemTypeX -> type.onRecycle(holder)
        }
    }

    private fun setClickListeners(holder: HolderX<ViewDataBinding>, type: TypeX<ViewDataBinding>) {
        val onClick = type.onClick
        if (onClick != null) {
            holder.itemView.setOnClickListener {
                onClick(holder)
            }
        }
        val onLongClick = type.onLongClick
        if (onLongClick != null) {
            holder.itemView.setOnLongClickListener {
                onLongClick(holder)
                true
            }
        }
    }





    /*
    override fun getFilter(): Filter {
        return MyFilter()
    }

    inner class MyFilter : Filter(){
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = Filter.FilterResults()
            val dataList = masterData
            if (constraint == null || constraint.length === 0) {
                d{"on constraint"}
                // No filter implemented we return all the list
                    results.values = dataList
                    results.count = dataList.size

                d{"Check size dataList  =["+dataList.size+"]"}
            } else {
                d{"have constraint"}
                val content = dataList.filter { it is Model.PlaceTypeModel && it.name.contains(constraint,true) }
                results.values = content
                results.count = content.size
                d{"Check size dataList  =["+dataList.size+"]"}

            }
            return results
        }

        @SuppressWarnings("unchecked")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val i =  results?.values as ArrayList<*>
            notifyItemRangeRemoved(0,list.size)
            list = listOf()
            list = i
            notifyDataSetChanged()


        }

    }
    */

}
