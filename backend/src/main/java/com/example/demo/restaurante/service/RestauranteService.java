package com.example.demo.restaurante.service;

import com.example.demo.comun.dto.PaginaResponse;
import com.example.demo.comun.excepcion.NoEncontradoException;
import com.example.demo.comun.excepcion.PeticionInvalidaException;
import com.example.demo.restaurante.dto.RestauranteRequest;
import com.example.demo.restaurante.dto.RestauranteResponse;
import com.example.demo.restaurante.entity.Restaurante;
import com.example.demo.restaurante.repository.RestauranteRepository;
import com.example.demo.seguridad.entity.Usuario;
import com.example.demo.seguridad.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RestauranteService {

    /** Campos por los que se permite ordenar el listado (evita errores con campos inexistentes). */
    private static final Set<String> CAMPOS_ORDENABLES = Set.of("id", "nombre", "direccion", "activo", "creadoAt");

    private final RestauranteRepository restauranteRepository;
    private final UsuarioRepository usuarioRepository;

    // ---------- Operaciones del dueño (siempre sobre SU restaurante) ----------

    @Transactional(readOnly = true)
    public RestauranteResponse obtenerMiRestaurante(String emailUsuario) {
        return RestauranteResponse.desde(restauranteDe(emailUsuario));
    }

    @Transactional
    public RestauranteResponse actualizarMiRestaurante(String emailUsuario, RestauranteRequest request) {
        Restaurante restaurante = restauranteDe(emailUsuario);
        restaurante.setNombre(request.nombre().trim());
        restaurante.setDireccion(request.direccion().trim());
        restaurante.setTelefono(request.telefono());
        // No hace falta save(): al terminar la transacción JPA detecta el cambio (dirty checking)
        return RestauranteResponse.desde(restaurante);
    }

    // ---------- Operaciones de administración ----------

    @Transactional(readOnly = true)
    public PaginaResponse<RestauranteResponse> listar(Pageable pageable) {
        for (Sort.Order orden : pageable.getSort()) {
            if (!CAMPOS_ORDENABLES.contains(orden.getProperty())) {
                throw new PeticionInvalidaException("No se puede ordenar por '" + orden.getProperty()
                        + "'. Campos permitidos: " + CAMPOS_ORDENABLES);
            }
        }
        return PaginaResponse.de(restauranteRepository.findAll(pageable).map(RestauranteResponse::desde));
    }

    @Transactional(readOnly = true)
    public RestauranteResponse obtener(Long id) {
        return RestauranteResponse.desde(buscar(id));
    }

    @Transactional
    public RestauranteResponse cambiarEstado(Long id, boolean activo) {
        Restaurante restaurante = buscar(id);
        restaurante.setActivo(activo);
        return RestauranteResponse.desde(restaurante);
    }

    // ---------- Internos ----------

    private Restaurante buscar(Long id) {
        return restauranteRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Restaurante no encontrado"));
    }

    /**
     * El restaurante se obtiene del USUARIO AUTENTICADO, nunca de un id enviado por el cliente.
     * Así un dueño no puede tocar el restaurante de otro.
     */
    private Restaurante restauranteDe(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
        if (usuario.getRestaurante() == null) {
            throw new NoEncontradoException("Tu usuario no tiene un restaurante asociado");
        }
        return usuario.getRestaurante();
    }
}
