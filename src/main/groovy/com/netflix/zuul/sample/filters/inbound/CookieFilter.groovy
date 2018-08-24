package com.netflix.zuul.sample.filters.inbound

import com.netflix.zuul.context.SessionContext
import com.netflix.zuul.filters.http.HttpInboundSyncFilter
import com.netflix.zuul.message.Headers
import com.netflix.zuul.message.http.HttpRequestMessage
import com.netflix.zuul.message.http.HttpResponseMessage
import io.netty.handler.codec.http.cookie.Cookie

class CookieFilter extends HttpInboundSyncFilter{

    @Override
    HttpRequestMessage apply(HttpRequestMessage requestMessage) {

        HttpResponseMessage responseMessage
        SessionContext context = responseMessage.getContext()

        // read out all headers of incoming request
        Headers headers = requestMessage.getHeaders()

        // read out customer-id from cookie
        Cookie cookie = requestMessage.parseCookies().get("customer-id").get(0)

        responseMessage.setStatus(400)

        System.out.println(responseMessage.getHeaders())

        /*if(!(headers.contains("Cookie"))) {
            responseMessage.setStatus(400)
        } else {
            System.out.println(cookie)
        }*/

        return requestMessage
    }

    @Override
    int filterOrder() {
        return 0
    }

    @Override
    boolean shouldFilter(HttpRequestMessage msg) {
        return true
    }
}
