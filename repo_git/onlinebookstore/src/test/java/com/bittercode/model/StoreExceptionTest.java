package com.bittercode.model;

import com.bittercode.constant.ResponseCode;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StoreExceptionTest {

    @Test
    void testStoreExceptionConstructors() {
        StoreException e1 = new StoreException("Errore");
        assertEquals("BAD_REQUEST", e1.getErrorCode());
        assertEquals(400, e1.getStatusCode());
        assertEquals("Errore", e1.getErrorMessage());

        StoreException e2 = new StoreException(ResponseCode.BAD_REQUEST);
        assertEquals("BAD_REQUEST", e2.getErrorCode());

        StoreException e3 = new StoreException("AUTH_FAIL", "Autenticazione fallita");
        assertEquals(422, e3.getStatusCode());
        assertEquals("AUTH_FAIL", e3.getErrorCode());
        assertEquals("Autenticazione fallita", e3.getErrorMessage());

        StoreException e4 = new StoreException(500, "SERVER_ERR", "Errore interno");
        assertEquals("SERVER_ERR", e4.getErrorCode());
        assertEquals(500, e4.getStatusCode());
        assertEquals("Errore interno", e4.getErrorMessage());
    }
}