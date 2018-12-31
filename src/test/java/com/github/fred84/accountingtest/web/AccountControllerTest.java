package com.github.fred84.accountingtest.web;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.fred84.accountingtest.service.Account;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;
import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class AccountControllerTest {

    @Inject
    @Client("/")
    private RxHttpClient client;

    @Test
    void findById_notFound() {
        HttpClientResponseException e = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(HttpRequest.GET("/9999"))
        );

        assertThat(e.getStatus().getCode(), equalTo(404));
    }

    @Test
    void findById_invalidId() {
        HttpClientResponseException e = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(HttpRequest.GET("/0"))
        );

        assertThat(e.getStatus().getCode(), equalTo(400));
    }

    @Test
    void createAndfindById() {
        Account account = client.toBlocking().retrieve(HttpRequest.POST("/create", ""), Account.class);
        Account foundAccount = client.toBlocking().retrieve(HttpRequest.GET("/" + account.getId()), Account.class);
        assertThat(foundAccount.getBalance(), equalTo(BigDecimal.ZERO));
    }

    @Test
    void adjustment_emptyRequest() {
        HttpClientResponseException e = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(HttpRequest.POST("/adjustment", ""))
        );

        assertThat(e.getStatus().getCode(), equalTo(400));
    }

    @Test
    void adjustment_negativeAmount() {
        Account account = createAccount();

        HttpClientResponseException e = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(
                        HttpRequest.POST(
                                "/adjustment",
                                new AdjustmentRequest(account.getId(), BigDecimal.valueOf(-1), "desrc")
                        )
                )
        );

        assertThat(e.getStatus().getCode(), equalTo(400));
        assertThat(e.getMessage(), containsString("request.amount: must be greater than 0"));
    }

    @Test
    void transfer_sameAccount() {
        Account account = createAccount();

        HttpClientResponseException e = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(
                        HttpRequest.POST(
                                "/transfer",
                                new TransferRequest(account.getId(), account.getId(), BigDecimal.ONE, "desrc")
                        )
                )
        );

        assertThat(e.getStatus().getCode(), equalTo(400));
        assertThat(e.getMessage(), containsString("'To' and 'From' account ids must be different"));
    }

    @Test
    void balanceHistory() {
        Account from = createAccount();
        Account to = createAccount();

        client.toBlocking().exchange(
                HttpRequest.POST("/adjustment", new AdjustmentRequest(from.getId(), BigDecimal.ONE, "abc"))
        );

        client.toBlocking().exchange(
                HttpRequest.POST("/transfer", new TransferRequest(from.getId(), to.getId(), BigDecimal.ONE, "def"))
        );

        List<?> history = client.toBlocking().retrieve(HttpRequest.GET(from.getId() + "/history/"), List.class);

        assertThat(history, hasSize(2));
    }

    private Account createAccount() {
        return client.toBlocking().retrieve(HttpRequest.POST("/create", ""), Account.class);
    }
}