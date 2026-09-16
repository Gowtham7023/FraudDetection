package com.fraud.service.client;

import com.fraud.service.dto.CustomerTransaction;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class TransactionHistoryClient {

    private final RestTemplate restTemplate;

    public TransactionHistoryClient() {
        this.restTemplate = new RestTemplate();
    }

    public List<CustomerTransaction> getCustomerTransactions(
            String customerId) {

        try {

            String url =
                    "http://localhost:8083/api/transactions/customer/"
                    + customerId;

            ResponseEntity<List<CustomerTransaction>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<CustomerTransaction>>() {}
                    );

            if (response.getBody() != null) {
                return response.getBody();
            }

        } catch (Exception e) {

            System.out.println(
                    "Unable to retrieve customer transaction history: "
                    + e.getMessage()
            );
        }

        return Collections.emptyList();
    }
}