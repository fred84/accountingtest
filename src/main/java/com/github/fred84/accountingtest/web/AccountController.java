package com.github.fred84.accountingtest.web;

import com.github.fred84.accountingtest.service.Account;
import com.github.fred84.accountingtest.service.AccountNotFoundException;
import com.github.fred84.accountingtest.service.AccountService;
import com.github.fred84.accountingtest.service.BalanceChange;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateos.JsonError;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.validation.Validated;
import java.util.List;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@Controller
@RequiredArgsConstructor
public class AccountController {

    @Singleton
    public static class AccountNotFoundExceptionHandler implements ExceptionHandler<AccountNotFoundException, HttpResponse> {

        @Override
        public HttpResponse handle(HttpRequest request, AccountNotFoundException e) {
            return HttpResponse.badRequest(new JsonError(e.getMessage()));
        }
    }

    private final AccountService accountService;

    @Get(uri = "/{id}")
    public Account findById(@Min(1) Long id) {
        return accountService.findById(id);
    }

    @Get(uri = "/{id}/history")
    public List<BalanceChange> history(@Min(1) Long id) {
        Account account = accountService.getById(id);

        return accountService.getBalanceChanges(account);
    }

    @Post(uri = "/create")
    public Account create() {
        return accountService.create();
    }

    @Post(uri = "/adjustment")
    public boolean adjustment(@Valid AdjustmentRequest request) {
        Account account = accountService.getById(request.getAccountId());

        return accountService.adjustment(account, request.getAmount(), request.getDescription());
    }

    @Post(uri = "/transfer")
    public boolean transfer(@Valid TransferRequest request) {
        Account to = accountService.getById(request.getToId());
        Account from = accountService.getById(request.getFromId());

        return accountService.transfer(from, to, request.getAmount(), request.getDescription());
    }
}