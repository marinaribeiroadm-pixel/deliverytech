package com.deliverytech.delivery_api.service.impl;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
import org.modelmapper.ModelMapper;
import com.deliverytech.delivery_api.dto.req.ClienteReqDTO;
import com.deliverytech.delivery_api.dto.res.ClienteResDTO;
import com.deliverytech.delivery_api.model.Cliente;
import com.deliverytech.delivery_api.repository.ClienteRepository;
import com.deliverytech.delivery_api.service.ClienteService;
 
import jakarta.transaction.Transactional;
 
@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {
 
    @Autowired
    private ClienteRepository clienteRepository;
 
    @Autowired
    private ModelMapper modelMapper;
 
 
    @Override
    public ClienteResDTO cadastrarCliente(ClienteReqDTO dto) {
        // regra de negocios validação de email unico
        if (clienteRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrato: " + dto.getEmail());
        }
 
        // convertendo o DTO em Entidade
        Cliente cliente = modelMapper.map(dto, Cliente.class);
        cliente.setAtivo(true);
 
        //Salvamos cliente no banco
 
        Cliente clienteSalvo = clienteRepository.save(cliente);
 
        // retornar o nosso DTO de resposta
        return modelMapper.map(clienteSalvo, ClienteResDTO.class);
    }
}