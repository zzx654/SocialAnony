package com.example.appportfolio.api.build

import com.example.appportfolio.BuildConfig
import com.example.appportfolio.other.Constants.BASE_URL
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RemoteDataSource {

    fun<Api> buildApi(
        api:Class<Api>,
        authToken:String?=null
    ):Api{
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                .addInterceptor { chain->
                    chain.proceed(chain.request().newBuilder().also{
                        it.addHeader("Authorization","Bearer $authToken")
                    }.build())
                }
                .also{client->
                    if(BuildConfig.DEBUG){
                        val logging= HttpLoggingInterceptor()
                        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                        client.addInterceptor(logging)
                    }
                }.build()
            )
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(api)
    }
}