package com.ntloc.demo.customer;

import com.ntloc.demo.AbstractTestContainersTest;
import com.ntloc.demo.exception.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerIntegrationTest extends AbstractTestContainersTest {

    private final String BASED_URL = "/api/v1/customers";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void TestThat_createCustomer_ShouldCreateCustomer() {
        //given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "Alice",
                "alice" + UUID.randomUUID() + "@gmail.com",
                "US"
        );

        //when
        ResponseEntity<Void> createCustomerResponse = testRestTemplate.exchange(
                BASED_URL,
                HttpMethod.POST,
                new HttpEntity<>(createCustomerRequest),
                Void.class
        );

        //then
        assertThat(createCustomerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        //getAllCustomers
        ResponseEntity<List<Customer>> getAllCustomersResponse = testRestTemplate.exchange(
                BASED_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(getAllCustomersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        //get the createdCustomer
        Customer createdCustomer = getAllCustomersResponse.getBody()
                .stream()
                .filter(customer -> customer.getEmail().equals(createCustomerRequest.email()))
                .findFirst()
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email " + createCustomerRequest.email()));

        //compare createdCustomer with createCustomerRequest
        assertThat(createdCustomer.getName()).isEqualTo(createCustomerRequest.name());
        assertThat(createdCustomer.getEmail()).isEqualTo(createCustomerRequest.email());
        assertThat(createdCustomer.getAddress()).isEqualTo(createCustomerRequest.address());
    }

    @Test
    void TestThat_updateCustomer_ShouldUpdateCustomer() {
        //given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "Alice",
                "alice" + UUID.randomUUID() + "@gmail.com",
                "US"
        );

        ResponseEntity<Void> createCustomerResponse = testRestTemplate.exchange(
                BASED_URL,
                HttpMethod.POST,
                new HttpEntity<>(createCustomerRequest),
                Void.class
        );

        assertThat(createCustomerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        //getAllCustomers
        ResponseEntity<List<Customer>> getAllCustomersResponse = testRestTemplate.exchange(
                BASED_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(getAllCustomersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllCustomersResponse.getBody()).isNotNull();

        //get the createdCustomer
        Customer createdCustomer = getAllCustomersResponse.getBody()
                .stream()
                .filter(customer -> customer.getEmail().equals(createCustomerRequest.email()))
                .findFirst()
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email " + createCustomerRequest.email()));

        //get id of the createdCustomer
        Long id = createdCustomer.getId();

        String newName = "Alice Trump";
        String newEmail = "alicetrump" + UUID.randomUUID() + "@gmail.com";
        String newAddress = "UK";

        //when
        testRestTemplate.exchange(
                BASED_URL + "/" + id + "?name=" + newName + "&email=" + newEmail + "&address=" + newAddress,
                HttpMethod.PUT,
                null,
                Void.class
        ).getStatusCode().is2xxSuccessful();

        //getCustomerById
        ResponseEntity<Customer> getCustomerByIdResponse = testRestTemplate.exchange(
                BASED_URL + "/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Customer>() {
                }
        );

        assertThat(getCustomerByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        Customer updatedCustomer = getCustomerByIdResponse.getBody();

        assertThat(updatedCustomer).isNotNull();

        assertThat(updatedCustomer.getName()).isEqualTo(newName);
        assertThat(updatedCustomer.getEmail()).isEqualTo(newEmail);
        assertThat(updatedCustomer.getAddress()).isEqualTo(newAddress);
    }

    @Test
    void TestThat_deleteCustomer_ShouldDeleteCustomer() {
        //given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "Alice",
                "alice" + UUID.randomUUID() + "@gmail.com",
                "US"
        );

        ResponseEntity<Void> createCustomerResponse = testRestTemplate.exchange(
                BASED_URL,
                HttpMethod.POST,
                new HttpEntity<>(createCustomerRequest),
                Void.class
        );

        assertThat(createCustomerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        //getAllCustomers
        ResponseEntity<List<Customer>> getAllCustomersResponse = testRestTemplate.exchange(
                BASED_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(getAllCustomersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllCustomersResponse.getBody()).isNotNull();

        //get the createdCustomer
        Customer createdCustomer = getAllCustomersResponse.getBody()
                .stream()
                .filter(customer -> customer.getEmail().equals(createCustomerRequest.email()))
                .findFirst()
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email " + createCustomerRequest.email()));

        //get id of the createdCustomer
        Long id = createdCustomer.getId();

        //when
        testRestTemplate.exchange(
                BASED_URL + "/" + id,
                HttpMethod.DELETE,
                null,
                Void.class
        ).getStatusCode().is2xxSuccessful();

        //then
        ResponseEntity<Customer> getCustomerByIdResponse = testRestTemplate.exchange(
                BASED_URL + "/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(getCustomerByIdResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}