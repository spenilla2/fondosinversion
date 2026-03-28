package com.btg.fondos.infrastructure.config;

import com.btg.fondos.application.service.AuthService;
import com.btg.fondos.application.service.ClientService;
import com.btg.fondos.application.service.FundService;
import com.btg.fondos.domain.port.in.AuthUseCase;
import com.btg.fondos.domain.port.in.ClientUseCase;
import com.btg.fondos.domain.port.in.FundUseCase;
import com.btg.fondos.domain.port.out.ClientRepository;
import com.btg.fondos.domain.port.out.FundRepository;
import com.btg.fondos.domain.port.out.NotificationService;
import com.btg.fondos.domain.port.out.TransactionRepository;
import com.btg.fondos.infrastructure.security.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeanConfig {

    @Bean
    public FundUseCase fundUseCase(ClientRepository clientRepository,
                                    FundRepository fundRepository,
                                    TransactionRepository transactionRepository,
                                    NotificationService notificationService) {
        return new FundService(clientRepository, fundRepository, transactionRepository, notificationService);
    }

    @Bean
    public AuthUseCase authUseCase(ClientRepository clientRepository,
                                    JwtProvider jwtProvider,
                                    PasswordEncoder passwordEncoder) {
        return new AuthService(clientRepository, jwtProvider, passwordEncoder);
    }

    @Bean
    public ClientUseCase clientUseCase(ClientRepository clientRepository,
                                       PasswordEncoder passwordEncoder) {
        return new ClientService(clientRepository, passwordEncoder);
    }
}
