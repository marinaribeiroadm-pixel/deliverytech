package com.deliverytech.delivery_api.service;
 
import com.deliverytech.delivery_api.dto.req.ClienteReqDTO;
import com.deliverytech.delivery_api.dto.res.ClienteResDTO;
import com.deliverytech.delivery_api.model.Cliente;
 
 
public interface ClienteService {
 
    ClienteResDTO cadastrarCliente(ClienteReqDTO dto);
}