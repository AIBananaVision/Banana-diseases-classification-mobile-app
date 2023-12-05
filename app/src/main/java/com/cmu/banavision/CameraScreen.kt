package com.cmu.banavision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cmu.banavision.common.UiText
import com.cmu.banavision.ui.theme.LocalSpacing
import com.cmu.banavision.util.Report
import com.cmu.banavision.util.getImageNameFromUri
import com.cmu.banavision.util.getImageSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    cameraController: LifecycleCameraController,
    permissionGranted: Boolean = rememberSaveable { false },
    expandBottomSheet: () -> Unit,
    showSnackbar: (UiText) -> MutableStateFlow<SnackbarResult?>,
    showMessage: (String) -> Unit,
    returnUri: (Uri?) -> Unit
) {
    val spacing = LocalSpacing.current
    val showCamera = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    //val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val screeHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    // var previewView: PreviewView

    val state by viewModel.imageUri.collectAsStateWithLifecycle()
    val deleleteImageState by viewModel.pendingDeleteImage.collectAsStateWithLifecycle()
    val locationData by viewModel.locationData.collectAsStateWithLifecycle()
    val responseState by viewModel.responseState.collectAsStateWithLifecycle()

    val viewModelScope = rememberCoroutineScope()


    LaunchedEffect(key1 = locationData, block = {
        if (state.uri != null)
            if (locationData != null) {
                showMessage("Location data is Country: ${locationData?.countryName}, State: ${locationData?.address}, City: ${locationData?.locality}")
                locationData?.longitude?.let {

                }
            }
    })


    LaunchedEffect(key1 = deleleteImageState) {
        if (deleleteImageState != null) {
            val snackbarResultStateFlow =
                showSnackbar(UiText.DynamicString("Image will be deleted in few seconds. Tap to undo."))

            // Use a CoroutineScope to launch a coroutine for collecting the StateFlow
            viewModelScope.launch {
                snackbarResultStateFlow.collect { snackbarResult ->
                    Log.i("CameraScreen", "SnackbarResult is $snackbarResult")
                    when (snackbarResult) {
                        SnackbarResult.ActionPerformed -> {
                            Log.d("CameraScreen", "Undo clicked")
                            viewModel.undoDeleteImage(deleleteImageState!!)
                        }

                        SnackbarResult.Dismissed -> {
                            Log.d("CameraScreen", "Snackbar dismissed")
                        }

                        else -> {
                            Log.d("CameraScreen", "Snackbar dismissed")
                        }
                    }
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.chooseImageFromGallery(uri)
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
                    CameraPreview(
                        controller = cameraController,
                        modifier = Modifier
                            .fillMaxSize()
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
                                takePhoto(
                                    controller = cameraController,
                                    context = context,
                                    onPhotoTaken = {
                                        viewModel.onTakePhoto(
                                            uri = viewModel.bitmapToUri(context, it)
                                        )
                                        showCamera.value = false
                                    }
                                )
                            }
                        )
                    }

                }
            }
        }
        returnUri(state.uri)
        if (state.uri == null) {
            Text(
                text = "No images yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            ImagePreview(
                uri = state.uri,
                modifier = Modifier
                    .padding(spacing.spaceSmall)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiary)
                    .clip(RoundedCornerShape(spacing.spaceLarge)),
                context = context,
                showMessage = showMessage,
                viewModel = viewModel
            )
            if (responseState.response != null && responseState.uri == state.uri) {
                responseState.response?.modelResults?.let {
                    ReportScreen(report = Report(predictedClass = it.predictedClass))

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

    }

}

@Composable
fun ImagePreview(
    uri: Uri?,
    modifier: Modifier = Modifier,
    context: Context,
    showMessage: (String) -> Unit,
    viewModel: CameraViewModel,
) {
    val spacing = LocalSpacing.current
    val responseState by viewModel.responseState.collectAsStateWithLifecycle()

    // Display an error toast if there's an error in responseState
    LaunchedEffect(key1 = responseState, block = {
        if (responseState.error != null && responseState.uri == uri) {
            showMessage(responseState.error!!)
            Toast.makeText(context, responseState.error, Toast.LENGTH_LONG).show()
        }
    })

    if (uri != null) {
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data = uri)
                .build()
        )
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image with a frame
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(spacing.spaceSmall))
                        .padding(start = spacing.spaceSmall)
                ) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Loading indicator over the image
                    if (responseState.loading == true && responseState.uri == uri) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(spacing.spaceSmall))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Display the predicted class
                    Spacer(modifier = Modifier.width(spacing.spaceSmall))
                }

                // Action buttons
                Column(
                    modifier = Modifier
                        .padding(spacing.spaceSmall)
                        .width(60.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // Send button
                    if (responseState.uri != uri || responseState.response == null)
                        if (responseState.loading == false) {
                            Box(
                                modifier = Modifier
                                    .padding(spacing.spaceSmall)
                                    .size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.sendImageAndLocationToModel(uri = uri, context)
                                    },
                                    enabled = responseState.response != null || responseState.loading == false
                                ) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = Color.White
                                    )
                                }

                            }

                        }

                    // Delete button
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.error, shape = CircleShape)
                            .padding(spacing.spaceSmall)
                            .size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.deleteImage(uri = uri, context = context)
                            }
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

        }
    }
}


private fun takePhoto(
    controller: LifecycleCameraController,
    context: android.content.Context,
    onPhotoTaken: (Bitmap) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )

                onPhotoTaken(rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
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
                cameraController.cameraSelector =
                    if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else CameraSelector.DEFAULT_BACK_CAMERA
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

@Composable
fun ReportScreen(report: Report) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Classification Result:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (report.predictedClass == "HEALTHY") report.predictedClass else "DISEASED: ${report.predictedClass}",
                style = MaterialTheme.typography.bodySmall,
                color = if (report.predictedClass == "HEALTHY") MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (report.recommendations.isNotEmpty()) {
                Text(
                    text = "Recommendations:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn {
                    items(report.recommendations) { recommendation ->
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}




