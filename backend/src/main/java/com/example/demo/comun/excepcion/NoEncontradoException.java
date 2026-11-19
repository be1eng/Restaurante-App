package com.example.demo.comun.excepcion;

/** Recurso inexistente. -> HTTP 404 */
public class NoEncontradoException extends RuntimeException {
    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}