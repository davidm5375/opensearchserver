/*
 * Copyright 2017-2020 Emmanuel Keller / Jaeksoft
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaeksoft.opensearchserver;

import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.CorsFilter;
import com.qwazr.server.GenericServer;
import com.qwazr.server.GenericServerBuilder;
import com.qwazr.server.JsonExceptionMapper;
import com.qwazr.server.RestApplication;
import com.qwazr.server.WebappBuilder;
import com.qwazr.server.configuration.ServerConfiguration;
import com.qwazr.utils.StringUtils;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;

public class Server extends Components {

    private final GenericServer server;

    private Server(final ServerConfiguration configuration) throws IOException {
        super(configuration.dataDirectory);

        final GenericServerBuilder serverBuilder = GenericServer.of(configuration)
            .webAppAccessLogger(Logger.getLogger("com.qwazr.AccessLogs"))
            .sessionPersistenceManager(getSessionPersistenceManager());

        final WebappBuilder webappBuilder = serverBuilder.getWebAppContext().getWebappBuilder();

        webappBuilder
            .registerCustomFaviconServlet("/com/jaeksoft/opensearchserver/front/favicon.ico")
            .registerStaticServlet("/static/*", "/com/jaeksoft/opensearchserver/front/static")
            .registerStaticServlet("/", "/com/jaeksoft/opensearchserver/front/index.html")
            .registerStaticServlet("/manifest.json", "/com/jaeksoft/opensearchserver/front/manifest.json")
            .registerJaxRsResources(
                ApplicationBuilder.of("/ws/*")
                    .classes(RestApplication.WithAuth.JSON_CLASSES)
                    .singletons(
                        getIndexService(),
                        getWebCrawlerService(),
                        getFileCrawlerService(),
                        new CorsFilter()))
            .registerJaxRsResources(
                ApplicationBuilder.of("/graphql/*")
                    .classes(
                        JsonExceptionMapper.WebApplication.class,
                        JsonExceptionMapper.Generic.class)
                    .singletons(
                        new GraphQLResource(getGraphQLService()),
                        new CorsFilter()
                    ));

        final String keycloakFile = System.getenv(KeycloakOIDCFilter.CONFIG_FILE_PARAM);
        if (!StringUtils.isBlank(keycloakFile)) {
            webappBuilder
                .registerFilter("/keycloak/* /*",
                    KeycloakOIDCFilter.class,
                    Map.of(KeycloakOIDCFilter.CONFIG_FILE_PARAM, keycloakFile))
                .registerFilter("/*", UserInfoFilter.class);
        }

        server = serverBuilder.build();
    }

    @Override
    public void close() {
        server.close();
        super.close();
    }

    private static volatile Server instance;

    public static Server getInstance() {
        return instance;
    }

    public static synchronized void main(final String... args) throws Exception {
        if (instance != null)
            throw new Exception("The instance has already be started");
        final ServerConfiguration serverConfiguration = ServerConfiguration.of()
            .applyEnvironmentVariables()
            .applySystemProperties()
            .applyCommandLineArgs(args)
            .build();
        instance = new Server(serverConfiguration);
        instance.server.start(true);
    }

    public static synchronized void stop() {
        if (instance == null)
            return;
        instance.close();
        instance = null;
    }

}
