package com.pexodrive.galleryapp.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pexodrive.galleryapp.presentation.grid.GridScreen
import com.pexodrive.galleryapp.presentation.permission.PermissionScreen
import com.pexodrive.galleryapp.presentation.viewer.ViewerScreen
import com.pexodrive.galleryapp.utils.PermissionManager
import dagger.hilt.android.EntryPointAccessors

@Composable
fun GalleryNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val permissionManager = EntryPointAccessors.fromApplication(
        context.applicationContext,
        PermissionEntryPoint::class.java
    ).permissionManager()

    val startDestination = if (permissionManager.hasAnyMediaAccess()) {
        Routes.GRID
    } else {
        Routes.PERMISSION
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Routes.PERMISSION,
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None }
        ) {
            PermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Routes.GRID) {
                        popUpTo(Routes.PERMISSION) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.GRID,
            enterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            GridScreen(
                onNavigateToViewer = { index ->
                    navController.navigate(Routes.viewerRoute(index))
                }
            )
        }

        composable(
            route = Routes.VIEWER,
            arguments = listOf(
                navArgument(Routes.VIEWER_ARG_INDEX) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val startIndex = backStackEntry.arguments?.getInt(Routes.VIEWER_ARG_INDEX) ?: 0
            val gridBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.GRID)
            }
            ViewerScreen(
                startIndex = startIndex,
                onBack = { navController.popBackStack() },
                gridBackStackEntry = gridBackStackEntry
            )
        }
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface PermissionEntryPoint {
    fun permissionManager(): PermissionManager
}
