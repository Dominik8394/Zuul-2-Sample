/*
 * Copyright 2018 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.zuul;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.config.DynamicIntProperty;
import com.netflix.discovery.EurekaClient;
import com.netflix.netty.common.accesslog.AccessLogPublisher;
import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.netty.common.channel.config.CommonChannelConfigKeys;
import com.netflix.netty.common.metrics.EventLoopGroupMetrics;
import com.netflix.netty.common.proxyprotocol.StripUntrustedProxyHeadersHandler;
import com.netflix.netty.common.ssl.ServerSslConfig;
import com.netflix.netty.common.status.ServerStatusManager;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;
import com.netflix.zuul.netty.server.BaseServerStartup;
import com.netflix.zuul.netty.server.DirectMemoryMonitor;
import com.netflix.zuul.netty.server.Http1MutualSslChannelInitializer;
import com.netflix.zuul.netty.server.ZuulServerChannelInitializer;
import com.netflix.zuul.netty.server.http2.Http2SslChannelInitializer;
import com.netflix.zuul.netty.ssl.BaseSslContextFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.ssl.ClientAuth;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample Server Startup - class that configures the Netty server startup settings
 * <p>
 * Author: Arthur Gonigberg
 * Date: November 20, 2017
 */
@Singleton
public class SampleServerStartup extends BaseServerStartup {

    enum ServerType {
        HTTP,
        HTTP2,
        HTTP_MUTUAL_TLS,
    }

    private static final String[] WWW_PROTOCOLS = new String[]{"TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3"};
    private static final ServerType SERVER_TYPE = ServerType.HTTP;

    @Inject
    public SampleServerStartup(ServerStatusManager serverStatusManager, FilterLoader filterLoader,
                               SessionContextDecorator sessionCtxDecorator, FilterUsageNotifier usageNotifier,
                               RequestCompleteHandler reqCompleteHandler, Registry registry,
                               DirectMemoryMonitor directMemoryMonitor, EventLoopGroupMetrics eventLoopGroupMetrics,
                               EurekaClient discoveryClient, ApplicationInfoManager applicationInfoManager,
                               AccessLogPublisher accessLogPublisher) {
        super(serverStatusManager, filterLoader, sessionCtxDecorator, usageNotifier, reqCompleteHandler, registry,
                directMemoryMonitor, eventLoopGroupMetrics, discoveryClient, applicationInfoManager, accessLogPublisher);
    }

    @Override
    protected Map<Integer, ChannelInitializer> choosePortsAndChannels(
            ChannelGroup clientChannels,
            ChannelConfig channelDependencies) {
        Map<Integer, ChannelInitializer> portsToChannels = new HashMap<>();

        int port = new DynamicIntProperty("zuul.server.port.main", 8085).get();

        ChannelConfig channelConfig = BaseServerStartup.defaultChannelConfig();
        ServerSslConfig sslConfig;
        /* These settings may need to be tweaked depending if you're running behind an ELB HTTP listener, TCP listener,
         * or directly on the internet.
         */
        switch (SERVER_TYPE) {
            /* The below settings can be used when running behind an ELB HTTP listener that terminates SSL for you
             * and passes XFF headers.
             */
            case HTTP:
                channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.ALWAYS);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, false);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, false);

                portsToChannels.put(port, new ZuulServerChannelInitializer(port, channelConfig, channelDependencies, clientChannels));
                logPortConfigured(port, null);
                break;

            /* The below settings can be used when running behind an ELB TCP listener with proxy protocol, terminating
             * SSL in Zuul.
             */
            case HTTP2:
                sslConfig = ServerSslConfig.withDefaultCiphers(
                        loadFromResources("server.cert"),
                        loadFromResources("server.key"),
                        WWW_PROTOCOLS);

                channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.serverSslConfig, sslConfig);
                channelConfig.set(CommonChannelConfigKeys.sslContextFactory, new BaseSslContextFactory(registry, sslConfig));

                addHttp2DefaultConfig(channelConfig);

                portsToChannels.put(port, new Http2SslChannelInitializer(port, channelConfig, channelDependencies, clientChannels));
                logPortConfigured(port, sslConfig);
                break;

            /* The below settings can be used when running behind an ELB TCP listener with proxy protocol, terminating
             * SSL in Zuul.
             *
             * Can be tested using certs in resources directory:
             *  curl https://localhost:7001/test -vk --cert src/main/resources/ssl/client.cert:zuul123 --key src/main/resources/ssl/client.key
             */
            case HTTP_MUTUAL_TLS:
                sslConfig = new ServerSslConfig(
                        WWW_PROTOCOLS,
                        ServerSslConfig.getDefaultCiphers(),
                        loadFromResources("server.cert"),
                        loadFromResources("server.key"),
                        null,
                        ClientAuth.REQUIRE,
                        loadFromResources("truststore.jks"),
                        loadFromResources("truststore.key"),
                        false);

                channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.NEVER);
                channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, true);
                channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
                channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, true);
                channelConfig.set(CommonChannelConfigKeys.serverSslConfig, sslConfig);
                channelConfig.set(CommonChannelConfigKeys.sslContextFactory, new BaseSslContextFactory(registry, sslConfig));

                portsToChannels.put(port, new Http1MutualSslChannelInitializer(port, channelConfig, channelDependencies, clientChannels));
                logPortConfigured(port, sslConfig);
                break;
        }

        return portsToChannels;
    }

    private File loadFromResources(String s) {
        return new File(ClassLoader.getSystemResource("ssl/" + s).getFile());
    }
}
