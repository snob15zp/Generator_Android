package com.inhealion.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import com.inhealion.compose.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainActivityComposable()
        }
    }
}

@Composable
fun MainActivityComposable() {
    AppTheme {
        ConstraintLayout {
            val toolbar = createRef()
            TopAppBar(
                title = { Text("Hello") },
                modifier = Modifier.constrainAs(toolbar) {
                    top.linkTo(parent.top)
                }
            )
        }
    }
}

@Preview
@Composable
fun ComposablePreview() {
    MainActivityComposable()
}
