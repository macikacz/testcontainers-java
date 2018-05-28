package org.testcontainers.dockerclient;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.utility.ComparableVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class NpipeSocketClientProviderStrategy extends DockerClientProviderStrategy {

    protected static final String DOCKER_SOCK_PATH = "//./pipe/docker_engine";
    private static final String SOCKET_LOCATION = "npipe://" + DOCKER_SOCK_PATH;

    private static final String PING_TIMEOUT_DEFAULT = "10";
    private static final String PING_TIMEOUT_PROPERTY_NAME = "testcontainers.npipesocketprovider.timeout";

    public static final int PRIORITY = EnvironmentAndSystemPropertyClientProviderStrategy.PRIORITY - 20;

    @Override
    protected boolean isApplicable() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    @Override
    public void test() throws InvalidConfigurationException {
        try {
            config = tryConfiguration(SOCKET_LOCATION);
            log.info("Accessing docker with local Unix socket");
        } catch (Exception | UnsatisfiedLinkError e) {
            throw new InvalidConfigurationException("ping failed", e);
        }
    }

    @NotNull
    protected DockerClientConfig tryConfiguration(String dockerHost) {
        config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .withDockerTlsVerify(false)
                .build();
        client = getClientForConfig(config);

        final int timeout = Integer.parseInt(System.getProperty(PING_TIMEOUT_PROPERTY_NAME, PING_TIMEOUT_DEFAULT));
        ping(client, timeout);

        return config;
    }

    @Override
    public String getDescription() {
        return "local Npipe socket (" + SOCKET_LOCATION + ")";
    }

    @Override
    protected int getPriority() {
        return PRIORITY;
    }
}