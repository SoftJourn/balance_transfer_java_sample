package com.softjourn.balance.transfer.client;

import com.softjourn.balance.transfer.client.exception.AdminNotFoundException;
import com.softjourn.balance.transfer.client.exception.ChaincodeNotFoundException;
import com.softjourn.balance.transfer.client.exception.ChannelAlreadyExistsException;
import com.softjourn.balance.transfer.client.exception.ChannelNotFoundException;
import com.softjourn.balance.transfer.client.exception.OrganizationNotFoundException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {AdminNotFoundException.class,
            ChannelAlreadyExistsException.class,
            TransactionException.class
    })
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getLocalizedMessage(),
                new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(value = {ChannelNotFoundException.class,
            OrganizationNotFoundException.class,
            ChaincodeNotFoundException.class
    })
    protected ResponseEntity<Object> handleBadRequest(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getLocalizedMessage(),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

}
