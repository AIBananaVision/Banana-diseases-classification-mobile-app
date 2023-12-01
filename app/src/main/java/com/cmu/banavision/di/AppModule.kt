package com.cmu.banavision.di


import android.app.Application
import android.location.LocationProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.cmu.banavision.network.RetrofitClient
import com.cmu.banavision.network.SoilService
import com.cmu.banavision.repository.CustomCameraRepo
import com.cmu.banavision.repository.CustomCameraRepoImpl
import com.cmu.banavision.repository.LocationClient
import com.cmu.banavision.usecases.CaptureAndSaveImage
import com.cmu.banavision.usecases.PictureUseCase
import com.cmu.banavision.util.DefaultLocationClient
import com.cmu.banavision.util.LocationService
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideSoilService(): SoilService {
        return RetrofitClient.soilService
    }

    @Provides
    @Singleton
    fun provideCameraSelector(): CameraSelector {
        return CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }

    @Provides
    @Singleton
    fun provideCameraProvider(application: Application)
            : ProcessCameraProvider {
        return ProcessCameraProvider.getInstance(application).get()

    }

    @Provides
    @Singleton
    fun provideCameraPreview(): Preview {
        return Preview.Builder().build()
    }


    @Provides
    @Singleton
    fun provideCustomCameraRepo(

    ): CustomCameraRepo {
        return CustomCameraRepoImpl()
    }

    @Provides
    @Singleton
    fun providePictureUseCase(
        repository: CustomCameraRepo
    ): PictureUseCase {
        return PictureUseCase(
            captureAndSaveImageUseCase = CaptureAndSaveImage(repository)
        )
    }


    @Provides
    @Singleton
    fun provideLocationClient(
        application: Application
    ): LocationClient {
        return DefaultLocationClient(
            application,
            LocationServices.getFusedLocationProviderClient(application)
        )
    }
}