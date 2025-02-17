package com.ntloc.demo.customer;

import com.ntloc.demo.exception.CustomerEmailUnavailableException;
import com.ntloc.demo.exception.CustomerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private CustomerService underTest;

    @Mock
    private CustomerRepository customerRepository;

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerRepository);
    }

    @Test
    void TestThat_getCustomers_ShouldGetAllCustomers() {
        //given
        //when
        underTest.getCustomers();

        //then
        verify(customerRepository).findAll();
    }

    @Test
    void TestThat_getCustomerById_ShouldReturnFoundCustomer() {
        //given
        Long id = 1L;

        Customer foundCustomer = Customer.create(id, "Alice", "alice@gmail.com", "US");

        when(customerRepository.findById(id)).thenReturn(Optional.of(foundCustomer));

        //when
        Customer result = underTest.getCustomerById(id);

        //then
        assertThat(result).isNotNull();
    }

    @Test
    void TestThat_getCustomerById_ShouldThrowCustomerNotFoundException_WhenCustomerIdDoesNotExist() {
        //given
        Long id = 1L;

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> underTest.getCustomerById(id))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer with id " + id + " doesn't found");
    }

    @Test
    void TestThat_createCustomer_ShouldCreateCustomer() {
        //given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "Alice",
                "alice@gmail.com",
                "US"
        );

        //when
        underTest.createCustomer(createCustomerRequest);

        //then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer result = customerArgumentCaptor.getValue();

        assertThat(result.getName()).isEqualTo(createCustomerRequest.name());
        assertThat(result.getEmail()).isEqualTo(createCustomerRequest.email());
        assertThat(result.getAddress()).isEqualTo(createCustomerRequest.address());
    }

    @Test
    void TestThat_createCustomer_ShouldNotCreateCustomerAndThrowCustomerEmailUnavailableException_WhenEmailIsUnavailable() {
        //given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "Alice",
                "alice@gmail.com",
                "US"
        );

        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(new Customer()));

        //when
        //then
        assertThatThrownBy(() -> underTest.createCustomer(createCustomerRequest))
                .isInstanceOf(CustomerEmailUnavailableException.class)
                .hasMessageContaining("The email " + createCustomerRequest.email() + " unavailable.");

    }

    @Test
    void TestThat_updateCustomer_ShouldThrowCustomerNotFoundException_WhenCustomerIdDoesNotFound() {
        //given
        Long id = 1L;
        String name = "alice";
        String email = "alice@gmail.com";
        String address = "US";

        when(customerRepository.findById(id))
                .thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> underTest.updateCustomer(id, name, email, address))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer with id " + id + " doesn't found");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void TestThat_updateCustomer_ShouldOnlyUpdateCustomerName() {
        //given
        Long id = 1L;
        String newName = "Alice Trump";

        Customer foundCustomer = Customer.create(id, "Alice", "alice@gmail.com", "US");

        Customer expected = Customer.create(id, newName, "alice@gmail.com", "US");

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(foundCustomer));

        //when
        underTest.updateCustomer(id, newName, null, null);

        //then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer result = customerArgumentCaptor.getValue();

        assertThat(result.getName()).isEqualTo(newName);

        assertThat(result.getEmail()).isEqualTo(expected.getEmail());
        assertThat(result.getAddress()).isEqualTo(expected.getAddress());
    }

    @Test
    void TestThat_updateCustomer_ShouldThrowCustomerEmailUnavailableException_WhenEmailIsUnavailableToUpdate() {
        //given
        Long id = 1L;
        String newEmail = "alicetrump@gmail.com";

        Customer foundCustomer = Customer.create(id, "Alice", "alice@gmail.com", "US");

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(foundCustomer));

        when(customerRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new Customer()));

        //when
        //then
        assertThatThrownBy(() -> underTest.updateCustomer(id, null, newEmail, null))
                .isInstanceOf(CustomerEmailUnavailableException.class)
                .hasMessageContaining("The email \"" + newEmail + "\" unavailable to update");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void TestThat_updateCustomer_ShouldOnlyUpdateCustomerEmail() {
        //given
        Long id = 1L;
        String newEmail = "alicetrump@gmail.com";

        Customer foundCustomer = Customer.create(id, "Alice", "alice@gmail.com", "US");

        Customer expected = Customer.create(id, "Alice", newEmail, "US");

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(foundCustomer));

        when(customerRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        //when
        underTest.updateCustomer(id, null, newEmail, null);

        //then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer result = customerArgumentCaptor.getValue();

        assertThat(result.getEmail()).isEqualTo(expected.getEmail());

        assertThat(result.getName()).isEqualTo(expected.getName());
        assertThat(result.getAddress()).isEqualTo(expected.getAddress());
    }

    @Test
    void TestThat_updateCustomer_ShouldOnlyUpdateCustomerAddress() {
        //given
        Long id = 1L;

        Customer foundCustomer = Customer.create(id, "Alice", "alice@gmail.com", "US");

        String newAddress = "UK";

        Customer expected = Customer.create(id, "Alice", "alice@gmail.com", newAddress);

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(foundCustomer));

        //when
        underTest.updateCustomer(id, null, null, newAddress);

        //then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer result = customerArgumentCaptor.getValue();

        assertThat(result.getAddress()).isEqualTo(expected.getAddress());

        assertThat(result.getName()).isEqualTo(expected.getName());
        assertThat(result.getEmail()).isEqualTo(expected.getEmail());
    }

    @Test
    void TestThat_updateCustomer_ShouldFullUpdateCustomer() {
        //given
        Long id = 1L;

        Customer foundCustomer = Customer.create(id, "Alice", "alice@gmail.com", "US");

        String newName = "Alice Trump";
        String newEmail = "alicetrump@gmail.com";
        String newAddress = "UK";

        Customer expected = Customer.create(id, newName, newEmail, newAddress);

        when(customerRepository.findById(id))
                .thenReturn(Optional.of(foundCustomer));

        when(customerRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        //when
        underTest.updateCustomer(id, newName, newEmail, newAddress);

        //then
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer result = customerArgumentCaptor.getValue();

        assertThat(result.getName()).isEqualTo(expected.getName());
        assertThat(result.getEmail()).isEqualTo(expected.getEmail());
        assertThat(result.getAddress()).isEqualTo(expected.getAddress());
    }

    @Test
    void TestThat_deleteCustomer_ShouldThrowCustomerNotFoundException_WhenCustomerIdDoesNotExit() {
        //given
        Long id = 1L;

        when(customerRepository.existsById(id)).thenReturn(false);

        //when
        //then
        assertThatThrownBy(() -> underTest.deleteCustomer(id))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer with id " + id + " doesn't exist.");

        verify(customerRepository, never()).deleteById(anyLong());
    }

    @Test
    void TestThat_deleteCustomer_ShouldDeleteCustomer() {
        //given
        Long id = 1L;

        when(customerRepository.existsById(id)).thenReturn(true);

        //when
        underTest.deleteCustomer(id);

        //then
        verify(customerRepository).deleteById(id);
    }
}