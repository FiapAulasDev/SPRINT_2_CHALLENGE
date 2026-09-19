package com.aguia_branca.app.data.remote

class ApiException(val status: Int, message: String) : Exception(message)
