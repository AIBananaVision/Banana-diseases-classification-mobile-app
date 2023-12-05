package com.cmu.banavision.di


import android.app.Application
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.cmu.banavision.common.Constants.BASE_URL
import com.cmu.banavision.network.APIService
import com.cmu.banavision.network.RetrofitClient
import com.cmu.banavision.network.SoilService
import com.cmu.banavision.repository.CustomCameraRepo
import com.cmu.banavision.repository.CustomCameraRepoImpl
import com.cmu.banavision.repository.LocationClient
import com.cmu.banavision.repository.UploadDataImpl
import com.cmu.banavision.repository.UploadDataRepo
import com.cmu.banavision.usecases.CaptureAndSaveImage
import com.cmu.banavision.usecases.PictureUseCase
import com.cmu.banavision.usecases.UploadUseCase
import com.cmu.banavision.util.DefaultLocationClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideApiService(): APIService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(APIService::class.java)
    }

    @Provides
    @Singleton
    fun provideUploadDataRepo(
        apiService: APIService
    ): UploadDataRepo {
        return UploadDataImpl(
            apiService
        )
    }

    @Provides
    @Singleton
    fun provideUploadUseCase(
        repository: UploadDataRepo
    ): UploadUseCase {
        return UploadUseCase(
            repository
        )
    }

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