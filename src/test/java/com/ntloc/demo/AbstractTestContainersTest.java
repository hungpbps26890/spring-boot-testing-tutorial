package com.ntloc.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public abstract class AbstractTestContainersTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer
            = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @Test
    void TestThat_PostSQLContainer_CanEstablishConnection() {
        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }
}
