package team.nexters.kida.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import team.nexters.kida.data.keyword.Keyword
import team.nexters.kida.ui.keyword.KeywordCard
import team.nexters.kida.ui.keyword.KeywordConfirmScreen
import team.nexters.kida.ui.keyword.KeywordSelectScreen
import team.nexters.kida.ui.list.ListScreen
import team.nexters.kida.ui.list.dialog.ListDialog
import team.nexters.kida.ui.popup.PopupErrorContent
import team.nexters.kida.ui.popup.PopupInfoContent
import team.nexters.kida.ui.splash.SplashScreen
import team.nexters.kida.ui.write.WriteScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Keyword : Screen("keyword")
    object KeywordConfirm : Screen("keyword-confirm")
    object List : Screen("list")
    object Write : Screen("write")
    object PopupInfo : Screen("popup-info")
    object PopupError : Screen("popup-error")
    object EditDialog : Screen("edit")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        addSplash(navController)
        addKeyword(navController)
        addList(navController)
        addWrite(navController)
        addPopup(navController)
        addListDialog(navController)
    }
}

private fun NavGraphBuilder.addSplash(
    navController: NavController
) {
    composable(Screen.Splash.route) {
        SplashScreen(
            onNavigate = {
                navController.navigate(
                    it.route,
                    navOptions {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                )
            }
        )
    }
}

private fun NavGraphBuilder.addKeyword(
    navController: NavController
) {
    composable(Screen.Keyword.route) {
        KeywordSelectScreen(
            onNavigate = { keyword, card ->
                navController.navigate(
                    "${Screen.KeywordConfirm.route}?keyword=${keyword.encodedData()}&card=${card.encodedData()}",
                    navOptions {
                        popUpTo(Screen.Keyword.route) {
                            saveState = true
                        }
                        restoreState = true
                    }
                )
            },
            onInfoClick = { navController.navigate(Screen.PopupInfo.route) }
        )
    }

    composable(
        route = "${Screen.KeywordConfirm.route}?keyword={keyword}&card={card}",
        arguments = listOf(
            navArgument("keyword") {
                type = createParcelableNavType<Keyword>()
            },
            navArgument("card") {
                type = createParcelableNavType<KeywordCard>()
            }
        )
    ) {
        val arguments = requireNotNull(it.arguments)
        val keyword = requireNotNull(arguments.getParcelable<Keyword>("keyword"))
        val card = requireNotNull(arguments.getParcelable<KeywordCard>("card"))
        KeywordConfirmScreen(
            keyword = keyword,
            card = card,
            upPress = { navController.popBackStack() },
            onConfirm = { newKeyword ->
                navController.navigate(
                    Screen.Write.route + "?diaryId={diaryId}&keyword=${newKeyword.encodedData()}",
                    navOptions {
                        popUpTo(Screen.KeywordConfirm.route) {
                            saveState = true
                        }
                        restoreState = true
                    }
                )
            },
            onInfoClick = {
                navController.navigate(Screen.PopupInfo.route)
            }
        )
    }
}

private fun NavGraphBuilder.addList(
    navController: NavController
) {
    composable(Screen.List.route) {
        ListScreen(
            onNavigate = { destination ->
                if (destination.route == Screen.Keyword.route) {
                    navController.navigate(
                        destination.route,
                        navOptions {
                            popUpTo(Screen.List.route) {
                                inclusive = true
                            }
                        }
                    )
                } else {
                    navController.navigate(destination.route)
                }
            },
            onIconClick = {
                navController.navigate(Screen.PopupError.route)
            }
        )
    }
}

private fun NavGraphBuilder.addWrite(
    navController: NavController
) {
    composable(
        route = Screen.Write.route + "?diaryId={diaryId}&keyword={keyword}",
        arguments = listOf(
            diaryIdArgument(),
            keywordArgument()
        )
    ) {
        val keyword = requireNotNull(it.arguments?.getParcelable<Keyword>("keyword"))
        BackHandler(onBack = { navController.popBackStack() })
        WriteScreen(
            onPopBackStack = {
                navController.popBackStack()
            },
            onNavigateToList = {
                navController.navigate(
                    Screen.List.route,
                    navOptions {
                        popUpTo(Screen.Keyword.route) {
                            inclusive = true
                        }
                    }
                )
            },
            keyword = keyword
        )
    }
}

private fun NavGraphBuilder.addPopup(
    navController: NavController
) {
    dialog(Screen.PopupInfo.route) {
        PopupInfoContent(
            popUpTo = { navController.popBackStack() }
        )
    }

    dialog(Screen.PopupError.route) {
        PopupErrorContent(
            popUpTo = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.addListDialog(
    navController: NavController
) {
    dialog(
        route = Screen.EditDialog.route + "?diaryId={diaryId}",
        arguments = listOf(
            diaryIdArgument()
        )
    ) {
        ListDialog(
            onNavigate = { destination ->
                navController.navigate(destination.route)
            },
            onPopBackStack = {
                navController.popBackStack()
            }
        )
    }
}

private fun diaryIdArgument() = navArgument(name = "diaryId") {
    type = NavType.LongType
    defaultValue = -1
}

private fun keywordArgument() = navArgument(name = "keyword") {
    type = createParcelableNavType<Keyword>()
    defaultValue = Keyword("")
}

// nav graph 에서 data 변환용
private inline fun <reified T : Parcelable> T.encodedData(): String {
    return Json.encodeToString(this)
}

inline fun <reified T : Parcelable> createParcelableNavType(
    isNullableAllowed: Boolean = false
): NavType<T> {
    return object : NavType<T>(isNullableAllowed) {
        override val name: String
            get() = "SupportParcelable"

        override fun put(bundle: Bundle, key: String, value: T) {
            bundle.putParcelable(key, value)
        }

        override fun get(bundle: Bundle, key: String): T? {
            return bundle.getParcelable(key)
        }

        override fun parseValue(value: String): T {
            return Json.decodeFromString(value)
        }
    }
}
