package com.example.appportfolio

import com.example.appportfolio.other.Resource


inline fun<T> safeCall(action:()-> Resource<T>): Resource<T> {
    return try{
        action()//성공시 action람다가 리턴하는 값(마지막줄)리턴
    }
    catch(e:Exception){//실패시 밑줄 리턴
        //Resource.Error(e.localizedMessage?:"An unknown error occured")
        Resource.Error("예상하지 못한 오류가 발생했습니다")
    }

}