package com.example.appportfolio

import com.example.appportfolio.other.Constants.unexpectedError
import com.example.appportfolio.other.Resource


inline fun<T> safeCall(action:()-> Resource<T>): Resource<T> {
    return try{
        action()//성공시 action람다가 리턴하는 값(마지막줄)리턴
    }
    catch(e:Exception){
        Resource.Error(unexpectedError)
    }
}