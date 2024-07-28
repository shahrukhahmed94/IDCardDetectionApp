package com.shahrukh.idcarddetectionapp.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.shahrukh.idcarddetectionapp.navGraph.NavGraph
import com.shahrukh.idcarddetectionapp.presentation.utils.Routes
import com.shahrukh.idcarddetectionapp.ui.theme.IDCardDetectionAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private val TAG: String? = MainActivity::class.simpleName
    }

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {




        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            IDCardDetectionAppTheme {
                Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
                    // Retrieving startDestination from viewModel & redirecting to appropriate screen
                    val startDestination = Routes.ROUTE_HOME_NAVIGATION
                    Log.d(TAG, "setContent() called with startDestination = $startDestination ")

                    NavGraph(startDestination = startDestination)
                }
            }

        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IDCardDetectionAppTheme {
        Greeting("Android")
    }
}