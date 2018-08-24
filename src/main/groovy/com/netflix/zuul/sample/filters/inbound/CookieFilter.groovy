package com.netflix.zuul.sample.filters.inbound

import com.netflix.zuul.filters.http.HttpInboundSyncFilter
import com.netflix.zuul.message.Headers
import com.netflix.zuul.message.http.HttpRequestMessage
import com.netflix.zuul.message.http.HttpResponseMessage
import io.netty.handler.codec.http.cookie.Cookie

class CookieFilter extends HttpInboundSyncFilter{

    @Override
    HttpRequestMessage apply(HttpRequestMessage requestMessage) {

        Headers headers = requestMessage.getHeaders()


        // responseMessage.setStatus(400)
        //SessionContext context = responseMessage.getContext()

        // read out customer-id from cookie
        Cookie cookie = requestMessage.parseCookies().get("customer-id").get(0)

        /**
         * Request returns an empty response as soon as the subsequent code is
         * integrated. I have no idea why..
         */
       /* if(!(headers.contains("Cookie"))) {
            System.out.println("There is no cookie in the request")
            HttpResponseMessage responseMessage = HttpResponseMessageImpl(requestMessage.getContext(),
                    requestMessage, 400)
            responseMessage.setBodyAsText("Please pass a cookie within the request")
        } else {
            System.out.println(cookie)
        }*/

        System.out.println(headers)

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
