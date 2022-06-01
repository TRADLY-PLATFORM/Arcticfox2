package tradly.social.common

interface FragmentListener {
    fun callNextFragment(any: Any? = null, tag: String?)
    fun popFragment(tag:String)
}