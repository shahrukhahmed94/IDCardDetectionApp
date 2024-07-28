package com.shahrukh.idcarddetectionapp.navGraph

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.shahrukh.idcarddetectionapp.presentation.home.HomeScreen
import com.shahrukh.idcarddetectionapp.presentation.home.PreviewDetectedObjectScreen
import com.shahrukh.idcarddetectionapp.presentation.utils.loadBitmapFromUri

@Composable
fun NavGraph(
    startDestination: String
) {
    val navController: NavHostController = rememberNavController()

    // NavHost for allowing self contained navigation to occur
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // Home Screen Navigation
        navigation(
            route = Route.HomeNavigation.route,
            startDestination = Route.HomeScreen.route
        ) {
            composable(
                route = Route.HomeScreen.route
            ) {
                HomeScreen(navController = navController)
            }

            composable(route = Route.PreviewDetectedObjectScreen.route) {
                PreviewDetectedObjectScreen()
            }



        }







        }
    }




