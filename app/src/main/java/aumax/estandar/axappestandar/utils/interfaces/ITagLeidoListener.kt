package aumax.estandar.axappestandar.utils.interfaces

import aumax.estandar.axappestandar.utils.TagRFID

interface ITagLeidoListener {
    fun tagsLeidos(listTagsLeidos: MutableList<TagRFID>)
    fun tagLeido(tagRFID: TagRFID)
    fun error(mensaje: String)
    fun estado(estado: Int)
}