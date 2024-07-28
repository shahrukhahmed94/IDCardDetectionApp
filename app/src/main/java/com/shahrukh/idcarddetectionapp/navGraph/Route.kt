package com.shahrukh.idcarddetectionapp.navGraph

import com.shahrukh.idcarddetectionapp.presentation.utils.Routes

sealed class Route(
    val route: String
) {

    // Starting point
    object HomeNavigation: Route(route = Routes.ROUTE_HOME_NAVIGATION)
    object HomeScreen: Route(route = Routes.ROUTE_HOME_SCREEN)

    //Preview Detected Object Route

    object PreviewDetectedObjectScreen : Route(route = Routes.ROUTE_PREVIEW_DETECTED_OBJECT_SCREEN)


}
