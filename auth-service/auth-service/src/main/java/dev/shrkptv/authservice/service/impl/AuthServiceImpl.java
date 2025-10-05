package dev.shrkptv.authservice.service.impl;

import dev.shrkptv.authservice.database.entity.AuthUser;
import dev.shrkptv.authservice.database.repository.AuthUserRepository;
import dev.shrkptv.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException{
        return authUserRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User with login '" + login + "' not found"));
    }

    @Override
    public boolean existsByLogin(String login){
        AuthUser authUser = authUserRepository.findByLogin(login).orElse(null);
        return authUser != null;
    }

    @Override
    public AuthUser save(AuthUser authUser) {
        return authUserRepository.save(authUser);
    }
}
