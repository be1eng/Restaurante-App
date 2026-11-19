package com.example.demo.comun.excepcion;

/** La petición es sintácticamente válida pero pide algo no permitido (ej. ordenar por un campo inexistente). -> 400 */
public class PeticionInvalidaException extends RuntimeException {
    public PeticionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
