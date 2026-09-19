package com.aguia_branca.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aguia_branca.app.navigation.AppNavigation
import com.aguia_branca.app.ui.theme.Aguia_brancaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            Aguia_brancaTheme {

                AppNavigation()
            }
        }
    }
}