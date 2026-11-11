package com.deliverytech.delivery_api.controller;

import com.deliverytech.delivery_api.dto.req.LoginReqDTO;
import com.deliverytech.delivery_api.dto.req.RegisterReqDTO;
import com.deliverytech.delivery_api.dto.res.LoginResDTO;
import com.deliverytech.delivery_api.dto.res.UserResDTO;
import com.deliverytech.delivery_api.model.Usuario;
import com.deliverytech.delivery_api.security.JwtUtil;
import com.deliverytech.delivery_api.security.SecurityUtils;
import com.deliverytech.delivery_api.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Autenticação", description = "Endpoints para login, registro e gestão de sessão de usuários")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("/login")
    @Operation(summary = "Realizar login", 
               description = "Autentica o usuário e retorna o token JWT de acesso")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginReqDTO loginRequest) {
        try {
            // Autenticar usuário
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getSenha()
                )
            );

            // Carregar detalhes do usuário
            UserDetails userDetails = authService.loadUserByUsername(loginRequest.getEmail());

            // Gerar token JWT
            String token = jwtUtil.generateToken(userDetails);

            // Criar resposta
            Usuario usuario = (Usuario) userDetails;
            UserResDTO userResponse = new UserResDTO(usuario);
            LoginResDTO loginResponse = new LoginResDTO(token, jwtExpiration, userResponse);

            return ResponseEntity.ok(loginResponse);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", 
               description = "Cria uma nova conta de usuário no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou Email já em uso")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReqDTO registerRequest) {
        try {
            // Verificar se email já existe
            if (authService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Email já está em uso");
            }

            // Criar novo usuário
            Usuario novoUsuario = authService.criarUsuario(registerRequest);

            // Retornar dados do usuário (sem token - usuário deve fazer login)
            UserResDTO userResponse = new UserResDTO(novoUsuario);
            return ResponseEntity.status(201).body(userResponse);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao criar usuário: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar usuário logado", 
               description = "Retorna os dados do usuário atualmente autenticado com base no token JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado ou token inválido")
    })
    public ResponseEntity<?> getCurrentUser() {
        try {
            Usuario usuarioLogado = SecurityUtils.getCurrentUser();
            UserResDTO userResponse = new UserResDTO(usuarioLogado);
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Usuário não autenticado");
        }
    } 
}
