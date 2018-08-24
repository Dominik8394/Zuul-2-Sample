package com.netflix.zuul.sample.filters.endpoint


import com.netflix.zuul.filters.http.HttpSyncEndpoint
import com.netflix.zuul.message.http.HttpRequestMessage
import com.netflix.zuul.message.http.HttpResponseMessage
import com.netflix.zuul.message.http.HttpResponseMessageImpl
import com.netflix.zuul.stats.status.StatusCategoryUtils
import com.netflix.zuul.stats.status.ZuulStatusCategory

class User extends HttpSyncEndpoint {
    @Override
    HttpResponseMessage apply(HttpRequestMessage request) {

        HttpResponseMessage resp = new HttpResponseMessageImpl(request.getContext(), request, 200)
        //SessionContext context = request.getContext()
        //Headers responseHeaders = request.getHeaders().get("Cookie")


        resp.setBodyAsText("Successful!")

        StatusCategoryUtils.setStatusCategory(request.getContext(), ZuulStatusCategory.SUCCESS)

        return resp
    }
}
