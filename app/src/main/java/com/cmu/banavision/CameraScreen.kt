package com.cmu.banavision

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cmu.banavision.common.UiText
import com.cmu.banavision.ui.theme.LocalSpacing
import com.cmu.banavision.util.getImageNameFromUri
import com.cmu.banavision.util.getImageSize


@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    soilViewModel: SoilViewModel = hiltViewModel(),
    cameraController: LifecycleCameraController,
    permissionGranted: Boolean = rememberSaveable { false },
    expandBottomSheet: () -> Unit,
    showMessage: (String) -> Unit,
    returnUris: (List<Uri?>) -> Unit
) {
    val spacing = LocalSpacing.current
    val showCamera = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val screeHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    var previewView: PreviewView

    var message by remember {
        mutableStateOf("")
    }
    val state by viewModel.imageUris.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val soilPropertiesState by soilViewModel.soilProperties.collectAsStateWithLifecycle()
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.chooseImageFromGallery(uri, context)
        }
    }

    // Observe locationState
    LaunchedEffect(locationState) {
        print("Location state is $locationState")
        message = message + "Location state is $locationState"
        showMessage(message)
        locationState.longitude?.let {
            locationState.latitude?.let { it1 ->
                soilViewModel.getSoilProperties(
                    longitude = it,
                    latitude = it1,
                    properties = listOf("clay", "sand"),
                    depth = "0-5cm",
                    values = listOf("mean", "uncertainty")
                )
            }
        }
    }
    LaunchedEffect(key1 = soilPropertiesState, block = {
        print("Soil properties state is $soilPropertiesState")
        if (soilPropertiesState?.error?.isNotBlank() == true) {
            message = message + "Soil properties state is ${soilPropertiesState?.error}"
            showMessage(message)
        }
        message = message + "Soil properties state is ${soilPropertiesState?.soilPropertState}"
        showMessage(message)
    })

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (showCamera.value) {
            if (permissionGranted) {
                Box(
                    modifier = Modifier
                        .height(screeHeight * 0.85f)
                        .width(screenWidth)
                ) {
                    AndroidView(
                        factory = {
                            previewView = PreviewView(it)
                            viewModel.showCameraPreview(previewView, lifecycleOwner)
                            previewView
                        },
                        modifier = Modifier
                            .height(screeHeight * 0.85f)
                            .width(screenWidth)
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {

                        CameraControl(
                            Icons.Sharp.Lens,
                            R.string.icn_camera_view_camera_shutter_content_description,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(1.dp)
                                .border(1.dp, Color.White, CircleShape),
                            cameraController = cameraController,
                            onClick = {
                                viewModel.captureAndSave(context)
                                showCamera.value = false
                            }
                        )
                    }

                }
            }
        }
        returnUris(state.uris)
        if (state.uris.isEmpty()) {
            Text(
                text = "No images yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .height(screeHeight * 0.45f)
                    .width(screenWidth)
            ) {
                items(state.uris.distinct()) { uri ->
                    ImagePrevew(
                        uri = uri,
                        modifier = Modifier
                            .padding(spacing.spaceSmall)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.tertiary)
                            .clip(RoundedCornerShape(spacing.spaceLarge)),
                        context = context,
                        viewModel = viewModel
                    )
                }
            }
        }


        Box(
            modifier = Modifier
                .height(screeHeight * 0.15f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = spacing.spaceMedium, horizontal = spacing.spaceSmall)
                    .width(120.dp)
                    .clip(RoundedCornerShape(size = spacing.spaceExtraSmall))
                    .background(Color.Transparent)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.secondary,
                        RoundedCornerShape(size = spacing.spaceSmall)
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically

            ) {

                IconButton(onClick = {
                    expandBottomSheet()
                    if (permissionGranted) {
                        showCamera.value = true
                    } else {
                        Toast.makeText(
                            context,
                            "Please accept permission in app settings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }) {

                    Icon(
                        painter =
                        painterResource(id = R.drawable.camera),
                        contentDescription = "",
                        modifier = Modifier.size(45.dp),
                        tint = Color.Magenta
                    )

                }
                IconButton(onClick = {
                    galleryLauncher.launch("image/*")
                }) {

                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Open gallery",
                        modifier = Modifier.size(45.dp),
                        tint = Color.Magenta
                    )
                }
            }

        }
        if (soilPropertiesState?.isLoading == true) {
            // Show loading indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(spacing.spaceSmall)
                    .size(20.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

}


@Composable
fun ImagePrevew(
    uri: Uri?,
    modifier: Modifier = Modifier,
    context: android.content.Context,
    viewModel: CameraViewModel,
) {
    val spacing = LocalSpacing.current
    if (uri != null) {
        val painter = rememberAsyncImagePainter(
            ImageRequest
                .Builder(LocalContext.current)
                .data(data = uri)
                .build()
        )
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(spacing.spaceExtraSmall))
            Card(
                modifier = Modifier
                    .padding(spacing.spaceExtraSmall)
                    .clickable(onClick = { /* Handle image click */ }),
                shape = RoundedCornerShape(spacing.spaceExtraSmall),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp,
                    focusedElevation = 8.dp,
                    hoveredElevation = 8.dp,
                    draggedElevation = 8.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(60.dp)
                        .clip(RoundedCornerShape(spacing.spaceExtraSmall))
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(spacing.spaceSmall)
                        .width(120.dp)
                ) {
                    getImageNameFromUri(uri)?.let {
                        Text(
                            text = it,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(spacing.spaceSmall)
                        .width(120.dp)
                ) {
                    Text(
                        text = getImageSize(context.contentResolver, uri),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }


            Box(
                modifier = Modifier
                    .size(spacing.spaceExtraLarge)
                    .background(MaterialTheme.colorScheme.error, shape = CircleShape)
                    .padding(spacing.spaceSmall),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(onClick = {
                    viewModel.deleteImage(uri)
                }) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(spacing.spaceExtraSmall))
        }


    }
}

@Composable
fun CameraControl(
    imageVector: ImageVector,
    contentDescId: Int,
    modifier: Modifier = Modifier,
    cameraController: LifecycleCameraController,
    onClick: () -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically

    ) {
        IconButton(
            onClick = {
                // Switch camera logic
                if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                }
            },
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Switch camera",
                modifier = modifier,
                tint = Color.White
            )
        }
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Icon(
                imageVector,
                contentDescription = stringResource(id = contentDescId),
                modifier = modifier,
                tint = Color.White
            )
        }
    }


}


