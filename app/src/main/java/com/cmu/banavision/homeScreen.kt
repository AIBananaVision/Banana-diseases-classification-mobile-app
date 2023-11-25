package com.cmu.banavision

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cmu.banavision.common.UiText
import com.cmu.banavision.ui.theme.LocalSpacing
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen() {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    val deleteImage = remember { mutableStateOf(false) }
    val uiTextState = remember {
        mutableStateOf<UiText?>(null)
    }

    val permissions = if (Build.VERSION.SDK_INT <= 28) {
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    } else listOf(Manifest.permission.CAMERA)

    val permissionState = rememberMultiplePermissionsState(
        permissions = permissions
    )
    val scaffoldState = rememberScaffoldState()
    LaunchedEffect(permissionState) {

        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
            if (permissionState.allPermissionsGranted) {

                scaffoldState.snackbarHostState.showSnackbar("Camera permission granted")
            } else {
                scaffoldState.snackbarHostState.showSnackbar("Permission denied")
            }
        }
    }

    val cancel = remember { mutableStateOf(false) }
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or
                        CameraController.VIDEO_CAPTURE
            )
        }
    }
    // Check the state of the cancel mutable state
    DisposableEffect(cancel.value) {
        onDispose {
            if (cancel.value) {
                deleteImage.value = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {

                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.surface
                )
            }
        },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = spacing.spaceMedium, top = spacing.spaceMedium)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.spaceMedium),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "BanaVision",
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.padding(start = 0.dp)
                    )
                }
            }
        }

    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(spacing.spaceMedium),
            contentAlignment = Alignment.TopCenter

        ) {
            Text(
                text = "Welcome to BanaVision",
                style = TextStyle(
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.padding(start = 0.dp)
            )
            //Show Camera screen
            CameraScreen(
                cameraController = controller,
                permissionGranted = permissionState.allPermissionsGranted,
                expandBottomSheet = {

                },
                showMessage = {
                    uiTextState.value = UiText.DynamicString(it)
                    print("Message is $it")
                },

                ) {

                if (it != null) {

                    print("Image uris are $it")
                }

            }
        }
    }

}

@Composable
fun ImagePreview(
    imageUri: Uri,
    deleteImage: MutableState<Boolean>,
    cancel: MutableState<Boolean>,
    uiTextState: MutableState<UiText?>
) {
    val context = LocalContext.current
    val painter = rememberAsyncImagePainter(
        ImageRequest
            .Builder(context)
            .data(imageUri)
            .build()
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = {
                deleteImage.value = true
                cancel.value = true
                uiTextState.value = null
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Image"
            )
        }
    }
}

