package com.ntloc.demo.customer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer
            = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @Autowired
    private CustomerRepository underTest;

    @BeforeEach
    void setUp() {
        String email = "alice@gmail.com";
        Customer customer = Customer.create(
                "Alice",
                email,
                "US"
        );
        underTest.save(customer);
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void TestThat_PostSQLContainer_CanEstablishConnection() {
        assertThat(postgreSQLContainer.isCreated()).isTrue();
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    @Test
    void TestThat_findByEmail_ReturnsCustomer_WhenEmailExists() {
        //given
        String email = "alice@gmail.com";

        //when
        Optional<Customer> result = underTest.findByEmail(email);

        //then
        assertThat(result).isPresent();
    }

    @Test
    void TestThat_findByEmail_ReturnsNoCustomer_WhenEmailDoesNotExist() {
        //given
        String email = "alex@gmail.com";

        //when
        Optional<Customer> result = underTest.findByEmail(email);

        //then
        assertThat(result).isNotPresent();
    }
}