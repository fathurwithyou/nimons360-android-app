package com.eggheadengineers.nimons360.data.network

import com.eggheadengineers.nimons360.core.network.UnauthorizedEvent
import okhttp3.Interceptor
import okhttp3.Response

class UnauthorizedInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 409) {
            UnauthorizedEvent.emit()
        }
        return response
    }
}
