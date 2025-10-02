package br.com.gabryel.waes.rdc.banking.it.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpEntity.EMPTY;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

/**
 * Calls the current Application from the root
 *
 * Note: Could also have been a real 1 to 1, but for simplicity sake, this will only be a facade for a TestRestTemplate
 */
@Service
public class ApiClient {
    @Autowired
    private TestRestTemplate restTemplate;

    public <T> ResponseEntity<T> get(String path, Class<T> responseType) {
        return exchange(GET, path, EMPTY, responseType);
    }

    public <T> ResponseEntity<T> put(String path, Object body, Class<T> responseType) {
        return exchange(PUT, path, body, responseType);
    }

    private <T> ResponseEntity<T> exchange(HttpMethod method, String path, Object body, Class<T> responseType) {
        return restTemplate.exchange(path, method, new HttpEntity<>(body), responseType);
    }
}
