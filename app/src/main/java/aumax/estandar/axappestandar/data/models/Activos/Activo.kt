package aumax.estandar.axappestandar.data.models.Activos

data class Activo(
    val id: Int,
    val idSubsector: Int,
    val name: String,
    val brand: String,
    val model: String,
    val seriaNumber: String,
    val tagRfid: String?, // <- aquí le agregás el ?
    val idEmpresa: Int,
    val version: Int,
    val status: Boolean,
    var encontrado: String?
)

/*

  {
    "id": 37,
    "idSubsector": 6,
    "name": "Silla ergonómica",
    "brand": "ErgoPlus",
    "model": "E400",
    "seriaNumber": "SN-ERG-6001",
    "tagRfid": "1",
    "idActiveType": 1,
    "detailControls": [],
    "idActiveTypeNavigation": null,
    "idSubsectorNavigation": null,
    "idEmpresa": 1,
    "company": null,
    "version": 1,
    "status": true
  },


eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcklkIjoiMTAxNSIsImp0aSI6ImRmNGZiYzZmLWZhNmMtNGNjNC04ZTNhLTFmMTY5NmVjZmRhNiIsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6ImFkbWluIiwiZXhwIjoxNzUzOTY4MzM3LCJpc3MiOiJheEFzc2V0Q29udHJvbEFQSSIsImF1ZCI6ImF4QXNzZXRDb250cm9sQ2xpZW50In0.wT2PF3Pc30T5TF-DSr4pWYl8FVfilRjU8eE-ChrXsOA


 */
