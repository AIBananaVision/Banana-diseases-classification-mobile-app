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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.cmu.banavision.ui.theme.LocalSpacing
import com.cmu.banavision.util.getImageNameFromUri
import com.cmu.banavision.util.getImageSize



@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    deleteImage: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    cameraController: LifecycleCameraController,
    permissionGranted: Boolean = rememberSaveable { false },
    expandBottomSheet: () -> Unit,
    returnUri: (Uri?) -> Unit
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

    val state by viewModel.imageUri.collectAsStateWithLifecycle()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.chooseImageFromGallery(uri)
        }
    }

    LaunchedEffect(deleteImage.value) {
        if (deleteImage.value) {
            println("Delete image is called")
            viewModel.deleteImage()
        }
    }

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
        if (state.uri != null) {
            returnUri(state.uri)
            val painter = rememberAsyncImagePainter(
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(data = state.uri)
                    .build()
            )
            Row(
                modifier = Modifier
                    .padding(spacing.spaceSmall)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiary)
                    .clip(RoundedCornerShape(spacing.spaceLarge)),
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
                        getImageNameFromUri(state.uri!!)?.let {
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
                            text = getImageSize(context.contentResolver, state.uri!!),
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
                        viewModel.deleteImage()
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
    val spacing = LocalSpacing.current
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


