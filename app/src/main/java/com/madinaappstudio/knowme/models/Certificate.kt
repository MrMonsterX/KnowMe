package com.madinaappstudio.knowme.models

data class Certificate (
    var certName: String = "",
    var certUrl: String = "",
    val certType: String = ""
)

data class UserCertificate(
    var certificates: List<Certificate> = emptyList()
)