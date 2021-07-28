package com.example.composesigninsample

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AppRouter()
                    }
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
            val homeViewModel: HomeViewModel = hiltViewModel()

            HomeScreen(
                navigateToSignIn = {
                    navController.navigate(Screen.SignInScreen.route) {
                        popUpTo(Screen.HomeScreen.route) {
                            inclusive = true
                        }
                    }
                },
                signOut = homeViewModel::signOut,
                isSignedIn = homeViewModel.loggedInState,
            )
        }
        composable(Screen.SignInScreen.route) {
            val signInViewModel: SignInViewModel = hiltViewModel()

            SignInScreen(
                navigateToHome = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.SignInScreen.route) {
                            inclusive = true
                        }
                    }
                },
                setLoggedIn = signInViewModel::signIn,
                isSignedIn = signInViewModel.loggedInState,
            )
        }
    }
}

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home")
    object SignInScreen : Screen("signin")
}

@Composable
fun SignInScreen(
    navigateToHome: () -> Unit,
    setLoggedIn: () -> Unit,
    isSignedIn: Boolean,
) {
    if(!isSignedIn) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            var text by rememberSaveable { mutableStateOf("") }

            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Email") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            var text2 by rememberSaveable { mutableStateOf("") }

            TextField(
                value = text2,
                onValueChange = { text2 = it },
                label = { Text("Password") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Welcome to the app. Sign in to begin")

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = setLoggedIn) {
                Text(text = "Sign In")
            }
        }
    }

    DisposableEffect(key1 = isSignedIn, effect = {
        if (isSignedIn) {
            navigateToHome()
        }

        onDispose {
        }
    })
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

@Composable
fun HomeScreen(navigateToSignIn: () -> Unit, signOut: () -> Unit, isSignedIn: Boolean) {
    if (isSignedIn) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text(style = Typography.h1, text = "You are on the home screen!", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(style = Typography.body1, text = "Only for super secret signed in users only!")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = signOut) {
                Text(text = "Sign Out")
            }
        }
    }

    DisposableEffect(isSignedIn) {
        if (!isSignedIn) {
            navigateToSignIn()
        }

        onDispose {
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