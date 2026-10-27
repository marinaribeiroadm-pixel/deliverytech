package com.deliverytech.delivery_api.service;

import com.deliverytech.delivery_api.dto.req.ClienteReqDTO;
import com.deliverytech.delivery_api.dto.res.ClienteResDTO;
import com.deliverytech.delivery_api.model.Cliente;

import java.util.List;

public interface ClienteService {

    ClienteResDTO cadastrarCliente(ClienteReqDTO dto);

    ClienteResDTO buscarClientePorId(Long id);

    ClienteResDTO buscarClientePorEmail(String email);

    ClienteResDTO atualizarCliente(Long id, ClienteReqDTO dto);

    ClienteResDTO ativarDesativarCliente(Long id);

    List<ClienteResDTO> listarClientesAtivos();

}