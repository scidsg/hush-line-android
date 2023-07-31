package org.scidsg.hushline.android.server

data class MessagePage(
    val pgpOwner: String,
    private val pgpKeyId: String,
    private val pgpExp: String,
    private val title: String = "Hush Line"
) {
    //private val files: ArrayList<SendFile> = ArrayList()
    val model: Map<String, Any>
        get() {
            return mapOf(
                "pgpowner" to pgpOwner,
                "pgpkeyid" to pgpKeyId,
                "pgpexp" to pgpExp,
                "title" to title
            )
        }

    fun saveMessage(message: String) {
    }
}
