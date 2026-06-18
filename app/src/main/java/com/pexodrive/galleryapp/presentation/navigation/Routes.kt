package com.pexodrive.galleryapp.presentation.navigation

object Routes {
    const val PERMISSION = "permission"
    const val GRID = "grid"
    const val VIEWER = "viewer/{startIndex}"
    const val VIEWER_ARG_INDEX = "startIndex"

    fun viewerRoute(startIndex: Int) = "viewer/$startIndex"
}
