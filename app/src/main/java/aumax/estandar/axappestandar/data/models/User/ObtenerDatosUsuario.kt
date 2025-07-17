package aumax.estandar.axappestandar.data.models.User

data class CompanyDTO(
    val id: Int,
    val name: String,
    val status: String
)

data class UsuarioDTO(
    val id: Int,
    val name: String,
    val email: String,
    val rol: String,
    val companyNavigation: CompanyDTO
)

/*

{
  "id": 1014,
  "name": "Ignacio Buffaz",
  "email": "ignaciobuffaz73@gmail.com",
  "rol": "superadmin",
  "companyNavigation": {
    "id": 1,
    "name": "Aumax",
    "status": "actived"
  }
}

 */
