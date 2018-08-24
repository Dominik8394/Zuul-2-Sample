package com.netflix.zuul.sample.filters.inbound

import com.netflix.zuul.filters.http.HttpInboundSyncFilter
import com.netflix.zuul.message.Headers
import com.netflix.zuul.message.http.HttpRequestMessage
import com.netflix.zuul.message.http.HttpResponseMessage
import com.netflix.zuul.message.http.HttpResponseMessageImpl
import com.netflix.zuul.sample.filters.endpoint.BadRequestEndpoint
import io.netty.handler.codec.http.cookie.Cookie
import org.apache.http.HttpHeaders

class CookieFilter extends HttpInboundSyncFilter{

    @Override
    HttpRequestMessage apply(HttpRequestMessage requestMessage) {

        Cookie cookie = requestMessage.parseCookies().getFirst("customer-id")
        if (cookie == null || cookie.value() == null || cookie.value().isEmpty()) {
            requestMessage.getContext().setEndpoint(BadRequestEndpoint.class.getCanonicalName())
        } else {
            requestMessage.getHeaders().add(HttpHeaders.AUTHORIZATION, cookie.value())
            return requestMessage
        }
    }

    @Override
    int filterOrder() {
        return 1
    }

    @Override
    boolean shouldFilter(HttpRequestMessage msg) {
        return true
    }
}
