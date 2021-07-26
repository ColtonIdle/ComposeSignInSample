package com.example.composesigninsample

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.TextStyle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composesigninsample.ui.theme.ComposeSignInSampleTheme
import com.example.composesigninsample.ui.theme.Typography
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeSignInSampleTheme {
                Surface(color = MaterialTheme.colors.background) {
                    AppRouter()
                }
            }
        }
    }
}

@Composable
fun AppRouter() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(Screen.HomeScreen.route) {
            HomeScreen({
                navController.navigate(Screen.SignInScreen.route)
            })
        }
        composable(Screen.SignInScreen.route) { SignInScreen({ navController.popBackStack() }) }
    }
}

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home")
    object SignInScreen : Screen("signin")
}

/**
 * SignIn Screen and ViewModel
 */
@Composable
fun SignInScreen(loggedOutEvent: () -> Unit, viewModel: SignInViewModel = hiltViewModel()) {
    if (viewModel.loggedInState) {
        LaunchedEffect(Unit) {
            loggedOutEvent()
        }
    } else {
        Column {
            var text by rememberSaveable { mutableStateOf("") }

            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Email") },
                singleLine = true
            )
            var text2 by rememberSaveable { mutableStateOf("") }

            TextField(
                value = text2,
                onValueChange = { text2 = it },
                label = { Text("Password") },
                singleLine = true
            )
            Text(text = "Welcome to the app. Sign in to begin")
            Button(onClick = viewModel::signIn) {
                Text(text = "Sign In")
            }
        }
    }
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val prefs: SharedPreferences
) : ViewModel() {
    var loggedInState by mutableStateOf(prefs.getBoolean("authenticated", false))

    fun signIn() {
        prefs.edit().putBoolean("authenticated", true).commit()
        loggedInState = true
    }
}

/**
 * Home Screen and ViewModel
 */
@Composable
fun HomeScreen(navigateHome: () -> Unit, homeViewModel: HomeViewModel = hiltViewModel()) {
    if (homeViewModel.loggedInState) {
        Column {
            Text(style = Typography.h1, text = "You are on the home screen!")
            Text(style = Typography.body1, text = "Only for super secret signed in users only!")
            Button(onClick = homeViewModel::signOut) {
                Text(text = "Sign Out")
            }
        }
    } else {
        LaunchedEffect(Unit) {
            navigateHome()
        }
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefs: SharedPreferences
) : ViewModel() {
    var loggedInState by mutableStateOf(prefs.getBoolean("authenticated", false))

    fun signOut() {
        prefs.edit().putBoolean("authenticated", false).commit()
        loggedInState = false
    }

}

/**
 * Application and Hilt provider
 */
@HiltAndroidApp
class MyApplication : Application()

@InstallIn(SingletonComponent::class)
@Module
class AppModule {
    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}