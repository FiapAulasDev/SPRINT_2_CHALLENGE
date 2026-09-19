package com.aguia_branca.app

import android.app.Application
import com.aguia_branca.app.data.local.TokenStore

class AguiaBrancaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }
}
