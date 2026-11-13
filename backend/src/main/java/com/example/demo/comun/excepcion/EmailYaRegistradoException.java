package com.example.demo.comun.excepcion;

public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException() {
        super("El email ya está registrado");
    }
}
