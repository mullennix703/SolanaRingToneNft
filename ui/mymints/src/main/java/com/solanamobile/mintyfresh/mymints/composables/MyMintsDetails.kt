package com.solanamobile.mintyfresh.mymints.composables

import android.content.Intent
import android.media.MediaPlayer
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.solanamobile.mintyfresh.composable.simplecomposables.BackButton
import com.solanamobile.mintyfresh.mymints.R
import com.solanamobile.mintyfresh.mymints.ktx.hiltActivityViewModel
import com.solanamobile.mintyfresh.mymints.viewmodels.MyMintsViewModel

private const val MyMintsDetailsRoute = "MyMintsDetails"
private var mediaPlayer: MediaPlayer? = null
private const val TAG = "myMintsDetailsScreen"
fun NavController.navigateToMyMintsDetails(index: Int, navOptions: NavOptions? = null) {
    this.navigate("$MyMintsDetailsRoute?index=$index", navOptions)
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.myMintsDetailsScreen(
    navigateUp: () -> Boolean = { true },
) {
    composable(
        route = "$MyMintsDetailsRoute?index={index}",
        arguments = listOf(navArgument("index") { type = NavType.IntType }),
    ) { backStackEntry ->
        MyMintsDetails(
            index = backStackEntry.arguments?.getInt("index")
                ?: throw IllegalStateException("$MyMintsDetailsRoute requires an \"index\" argument to be launched"),
            navigateUp = navigateUp,
        )
    }
}

fun playAudio(audioFileName: String) {
    if (mediaPlayer == null) {
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(audioFileName)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }
}

fun stopAudio() {
    mediaPlayer?.stop()
    mediaPlayer?.release()
    mediaPlayer = null
}

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalGlideComposeApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun MyMintsDetails(
    index: Int,
    navigateUp: () -> Boolean = { true },
    myMintsViewModel: MyMintsViewModel = hiltActivityViewModel(),
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val uiState = myMintsViewModel.viewState.collectAsState().value

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton(navigateUp)
                },
                title = {},
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior,
                actions = {
                    Icon(
                        modifier = Modifier
                            .padding(
                                end = 16.dp
                            )
                            .clickable {
                                myMintsViewModel.shareMyMint(index)
                            },
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.share)
                    )
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            count = uiState.myMints.size,
            state = PagerState(index),
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val configuration = LocalConfiguration.current
                val imageHeight = configuration.screenHeightDp.dp * 0.4f
//                GlideImage(
//                    modifier = Modifier
//                        .height(imageHeight)
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(color = MaterialTheme.colorScheme.background),
//                    model = uiState.myMints[page].mediaUrl,
//                    contentDescription = uiState.myMints[page].name,
//                    contentScale = ContentScale.Fit,
//                ) {
//                    it.thumbnail()
//                }
                val context = LocalContext.current
                val isPlayState = rememberSaveable { mutableStateOf(false) }
                val buttonText = if (isPlayState.value) {
                    "Stop Play"
                } else {
                    "Start Play"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                        onClick = {
                            val mp3File = uiState.myMints[page].mediaUrl
                            if (!isPlayState.value) {
                                playAudio(mp3File)
                            } else {
                                stopAudio()
                            }
                            isPlayState.value = !isPlayState.value
                        }
                    ) {
                        Text(text = buttonText)
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_SOUND_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        content = {
                            Text(text = stringResource(R.string.set_ringtone))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.titleLarge,
                    text = uiState.myMints[page].name,
                )
                Text(
                    modifier = Modifier.padding(top = 36.dp),
                    style = MaterialTheme.typography.labelMedium,
                    text = stringResource(id = R.string.description),
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    text = uiState.myMints[page].description,
                )
                Text(
                    modifier = Modifier.padding(top = 36.dp),
                    style = MaterialTheme.typography.labelMedium,
                    text = stringResource(R.string.metadata),
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        text = stringResource(R.string.mint_address),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        modifier = Modifier.size(96.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        text = uiState.myMints[page].id,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}