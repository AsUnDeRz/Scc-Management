package asunder.toche.adapterx.adapterx

interface Handler

interface TypeHandler : Handler {
    fun getItemType(item: Any, position: Int): BaseTypeX?
}

interface LayoutHandler : Handler {
    fun getItemLayout(item: Any, position: Int): Int
}

interface StableId {
    val stableId: Long
}
