package com.example.appportfolio.other

import androidx.lifecycle.Observer

class Event<out T>(private val content:T) {

    private var hasBeenHandled=false
        private set

    fun getContentIfNotHandled():T?{

        return if(!hasBeenHandled){
            hasBeenHandled=true
            content
        }else null
    }

    fun peekContent()=content


    class EventObserver<T>(
        private inline val onError:((String)->Unit)?=null,
        private inline val onLoading:(()->Unit)?=null,
        private inline val onSuccess:(T)->Unit
    ): Observer<Event<Resource<T>>> {
        override fun onChanged(t: Event<Resource<T>>?) {
            when(val content=t?.peekContent()){
                is Resource.Success ->{
                    t.getContentIfNotHandled()?.let{
                        onSuccess(it.data!!)
                    }
                }
                //에러라는 값이 들어오면
                is Resource.Error ->{
                    t.getContentIfNotHandled()?.let{
                        onError?.let{error->
                            error(it.message!!)
                        }
                    }
                }
                //로딩상태라는 값이 들어오면
                is Resource.Loading ->{
                    onLoading?.let{loading->
                        loading()//observer의 로딩실행

                    }
                }
            }
        }
    }

}