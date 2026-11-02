package com.deliverytech.delivery_api.service.impl;

import com.deliverytech.delivery_api.dto.req.RestauranteReqDTO;
import com.deliverytech.delivery_api.dto.res.RestauranteResDTO;
import com.deliverytech.delivery_api.model.Restaurante;
import com.deliverytech.delivery_api.repository.RestauranteRepository;
import com.deliverytech.delivery_api.service.RestauranteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RestauranteServiceImpl implements RestauranteService {

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Override
    public RestauranteResDTO cadastrar(RestauranteReqDTO dto) {
        // validação de nome único
        if (restauranteRepository.findByNome(dto.getNome()).isPresent()) {
            throw new IllegalArgumentException("Restaurante já cadastrado: " + dto.getNome());
        }

        Restaurante restaurante = toEntity(dto);
        restaurante.setAtivo(true);

        Restaurante salvo = restauranteRepository.save(restaurante);
        return toResponseDTO(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestauranteResDTO> listarRestaurantes(String categoria, Boolean ativo, Pageable pageable) {
        Page<Restaurante> pagina;

        if (categoria != null && ativo != null) {
            pagina = restauranteRepository.findByCategoriaAndAtivo(categoria, ativo, pageable);
        } else if (categoria != null) {
            pagina = restauranteRepository.findByCategoria(categoria, pageable);
        } else if (ativo != null) {
            pagina = restauranteRepository.findByAtivo(ativo, pageable);
        } else {
            pagina = restauranteRepository.findAll(pageable);
        }

        return pagina.map(this::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public RestauranteResDTO buscarRestaurantePorId(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + id));
        return toResponseDTO(restaurante);
    }

    @Override
    public RestauranteResDTO atualizarRestaurante(Long id, RestauranteReqDTO dto) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + id));

        // verifica se o nome mudou e se já existe outro restaurante com o novo nome
        if (!restaurante.getNome().equals(dto.getNome()) &&
                restauranteRepository.findByNome(dto.getNome()).isPresent()) {
            throw new IllegalArgumentException("Nome já cadastrado: " + dto.getNome());
        }

        restaurante.setNome(dto.getNome());
        restaurante.setCategoria(dto.getCategoria());
        restaurante.setEndereco(dto.getEndereco());
        restaurante.setTelefone(dto.getTelefone());
        restaurante.setTaxaEntrega(dto.getTaxaEntrega());
        restaurante.setTempoEntrega(dto.getTempoEntrega());
        restaurante.setHorarioFuncionamento(dto.getHorarioFuncionamento());

        Restaurante atualizado = restauranteRepository.save(restaurante);
        return toResponseDTO(atualizado);
    }

    @Override
    public RestauranteResDTO alterarStatusRestaurante(Long id) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + id));

        restaurante.setAtivo(!restaurante.isAtivo());

        Restaurante salvo = restauranteRepository.save(restaurante);
        return toResponseDTO(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestauranteResDTO> buscarRestaurantesPorCategoria(String categoria) {
        return restauranteRepository.findByCategoriaAndAtivoTrue(categoria)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTaxaEntrega(Long id, String cep) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado: " + id));

        // TODO: substituir por cálculo real baseado em distância/CEP
        return restaurante.getTaxaEntrega();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestauranteResDTO> buscarRestaurantesProximos(String cep, Integer raio) {
        // TODO: substituir por lógica real de geolocalização
        return restauranteRepository.findByAtivoTrue()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ---------- conversões DTO <-> Entity ----------

    private Restaurante toEntity(RestauranteReqDTO dto) {
        Restaurante restaurante = new Restaurante();
        restaurante.setNome(dto.getNome());
        restaurante.setCategoria(dto.getCategoria());
        restaurante.setEndereco(dto.getEndereco());
        restaurante.setTelefone(dto.getTelefone());
        restaurante.setTaxaEntrega(dto.getTaxaEntrega());
        restaurante.setTempoEntrega(dto.getTempoEntrega());
        restaurante.setHorarioFuncionamento(dto.getHorarioFuncionamento());
        return restaurante;
    }

    private RestauranteResDTO toResponseDTO(Restaurante restaurante) {
        RestauranteResDTO dto = new RestauranteResDTO();
        dto.setId(restaurante.getId());
        dto.setNome(restaurante.getNome());
        dto.setCategoria(restaurante.getCategoria());
        dto.setEndereco(restaurante.getEndereco());
        dto.setTelefone(restaurante.getTelefone());
        dto.setTaxaEntrega(restaurante.getTaxaEntrega());
        dto.setTempoEntrega(restaurante.getTempoEntrega());
        dto.setHorarioFuncionamento(restaurante.getHorarioFuncionamento());
        dto.setAtivo(restaurante.isAtivo());
        return dto;
    }
}
