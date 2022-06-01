package com.example.appportfolio.other

sealed class Resource<T>(val data:T?=null,val message:String?=null) {
    class Success<T>(data:T): Resource<T>(data)
    class Error<T>(message:String,data:T?=null): Resource<T>(data,message)
    class Loading<T>(data:T?=null): Resource<T>(data)
    class ENDOFLIST<T>(data:T?=null,message:String="전부 로드했습니다"):Resource<T>(data,message)
}

