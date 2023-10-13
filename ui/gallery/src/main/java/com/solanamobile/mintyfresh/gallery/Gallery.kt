package com.solanamobile.mintyfresh.gallery

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solanamobile.mintyfresh.composable.simplecomposables.*
import com.solanamobile.mintyfresh.gallery.viewmodel.Media
import com.solanamobile.mintyfresh.gallery.viewmodel.MediaViewModel
import com.solanamobile.mintyfresh.walletconnectbutton.composables.ConnectWalletButton

const val galleryRoute = "photos"
private const val TAG = "Gallery"
@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.galleryScreen(
    navigateToDetails: (String) -> Unit = { },
    navigateToCamera: () -> Unit = { },
    navigateToSettings: () -> Unit = { },
    navController: NavHostController,
    activityResultSender: ActivityResultSender,
    navigationItems: List<NavigationItem>,
    identityUri: Uri,
    iconUri: Uri,
    appName: String
) {
    composable(route = galleryRoute) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    shape = RoundedCornerShape(corner = CornerSize(16.dp)),
                    backgroundColor = MaterialTheme.colorScheme.onBackground,
                    onClick = navigateToCamera
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = stringResource(R.string.record_song_content_desc),
                        tint = MaterialTheme.colorScheme.background
                    )
                }
            },
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
                    Gallery(
                        navigateToDetails = navigateToDetails
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

@Composable
fun AudioList(musicFiles: List<Media>, navigateToDetails: (String) -> Unit = { }) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(musicFiles) { musicFile ->
            AudioFileItem(musicFile,
                onClick = {
                    navigateToDetails(musicFile.path)
                })
        }
    }
}


@Composable
fun AudioFileItem(musicFile: Media, onClick: () -> Unit) {
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
                text = musicFile.title,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = musicFile.path,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun Gallery(
    mediaViewModel: MediaViewModel = hiltViewModel(),
    navigateToDetails: (String) -> Unit = { },
) {
    val permissionsRequired = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }

    Column(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp)
            .fillMaxHeight()
    ) {
        Text(
            style = MaterialTheme.typography.headlineSmall,
            text = AnnotatedString(
                stringResource(R.string.lets_get),
                spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurfaceVariant)
            ).plus(
                AnnotatedString(
                    stringResource(R.string.mint_ringtone),
                    spanStyle = SpanStyle(MaterialTheme.colorScheme.onSurface)
                )
            )
        )
        PermissionView(
            permissionsRequired,
            content = {
                val uiState = mediaViewModel.getMediaList().collectAsState().value

                LaunchedEffect(
                    key1 = Unit,
                    block = {
                        mediaViewModel.loadAllMediaFiles()
                    }
                )
                DisposableEffect(key1 = mediaViewModel) {
                    mediaViewModel.registerContentObserver()
                    onDispose { mediaViewModel.unregisterContentObserver() }
                }

                Column {
                    Text(
                        modifier = Modifier.padding(
                            top = 15.dp
                        ),
                        text = stringResource(R.string.select_ringtone),
                        style = MaterialTheme.typography.labelLarge
                    )
                    AudioList(uiState,navigateToDetails)
                }
            },
            emptyView = {
                EmptyView(
                    it, stringResource(id = R.string.gallery_permission_body), stringResource(
                        id = R.string.gallery_permission_button
                    )
                )
            }
        )
    }
}

