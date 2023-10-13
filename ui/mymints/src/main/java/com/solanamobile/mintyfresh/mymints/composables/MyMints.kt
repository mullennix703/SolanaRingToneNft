package com.solanamobile.mintyfresh.mymints.composables

import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.composable.simplecomposables.*
import com.solanamobile.mintyfresh.mymints.R
import com.solanamobile.mintyfresh.mymints.ktx.hiltActivityViewModel
import com.solanamobile.mintyfresh.mymints.viewmodels.MyMintsViewModel
import com.solanamobile.mintyfresh.mymints.viewmodels.viewstate.MyMintsViewState
import com.solanamobile.mintyfresh.networkinterface.data.MintedMedia
import com.solanamobile.mintyfresh.walletconnectbutton.composables.ConnectWalletButton

const val myMintsRoute = "myMints"

fun NavController.navigateToMyMints(forceRefresh: Boolean = false, navOptions: NavOptions? = null) {
    this.navigate("$myMintsRoute?forceRefresh=$forceRefresh", navOptions)
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
fun NavGraphBuilder.myMintsScreen(
    navigateToDetails: (Int) -> Unit,
    navigateToSettings: () -> Unit = { },
    navController: NavHostController,
    activityResultSender: ActivityResultSender,
    navigationItems: List<NavigationItem>,
    identityUri: Uri,
    iconUri: Uri,
    appName: String
) {
    composable(
        route = "$myMintsRoute?forceRefresh={forceRefresh}",
        arguments = listOf(navArgument("forceRefresh") {
            type = NavType.BoolType
            defaultValue = false
        }),
    ) { backStackEntry ->
        val forceRefresh = backStackEntry.arguments?.getBoolean("forceRefresh")
            ?: throw IllegalStateException("$myMintsRoute requires an \"forceRefresh\" argument to be launched")

        // Remove forceRefresh param from backstack once used.
        backStackEntry.arguments?.remove("forceRefresh")

        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ConnectWalletButton(
                        identityUri = identityUri,
                        iconUri = iconUri,
                        identityName = appName,
                        activityResultSender = activityResultSender
                    )
                    SettingsButton(
                        modifier = Modifier.padding(start = 16.dp),
                        onClick = navigateToSettings
                    )
                }
            },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    navigationItems = navigationItems,
                )
            },
            content = { padding ->
                Box(
                    modifier = Modifier.padding(padding)
                ) {
                    MyMintPage(
                        forceRefresh = forceRefresh,
                        navigateToDetails = navigateToDetails
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

@Composable
fun AudioList(musicFiles: List<MintedMedia>, navigateToDetails: (Int) -> Unit,) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        itemsIndexed(musicFiles) {index, musicFile ->
            AudioFileItem(musicFile,
                onClick = {
                    navigateToDetails(index)
                })
        }
    }
}


@Composable
fun AudioFileItem(musicFile: MintedMedia, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = musicFile.name,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = musicFile.description,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = musicFile.mediaUrl,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterialApi::class)
@Composable
fun MyMintPage(
    forceRefresh: Boolean,
    myMintsViewModel: MyMintsViewModel = hiltActivityViewModel(),
    navigateToDetails: (Int) -> Unit,
) {
    val uiState = myMintsViewModel.viewState.collectAsState().value
    val isRefreshing = myMintsViewModel.isRefreshing.collectAsState().value

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            myMintsViewModel.refresh()
        })

    LaunchedEffect(
        key1 = Unit,
        block = {
            if (forceRefresh) {
                myMintsViewModel.refresh()
            }
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = AnnotatedString(
                stringResource(R.string.my),
                spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurfaceVariant)
            ).plus(
                AnnotatedString(
                    stringResource(R.string.mints),
                    spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurface)
                )
            ),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.Start)
        )

        Box(
            Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when (uiState) {
                is MyMintsViewState.Error -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        ErrorView(
                            text = uiState.error.message
                                ?: stringResource(R.string.error_fetching_mints),
                            buttonText = stringResource(R.string.retry),
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        ) {
                            myMintsViewModel.refresh()
                        }
                    }
                }
                is MyMintsViewState.Empty -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        EmptyView(
                            text = stringResource(R.string.no_mints_yet),
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                        )
                    }
                }
                is MyMintsViewState.NoConnection -> {
                    EmptyView(
                        text = stringResource(R.string.connect_to_see_mints),
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                    )
                }
                else -> {
                    AudioList(uiState.myMints,navigateToDetails)
                }
            }
            PullRefreshIndicator(
                isRefreshing,
                pullRefreshState,
                Modifier.align(Alignment.TopCenter)
            )
        }
    }
}