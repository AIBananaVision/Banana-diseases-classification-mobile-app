package com.cmu.banavision.util

sealed class Screen(val route:String){


    object Home : Screen("home_screen")


}