package br.dev.lourenco.scriba.core.security;

import br.dev.lourenco.scriba.modules.administracao.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return usuarioRepository.findByEmailIgnoreCase(username)
            .filter(user -> user.isAtivo())
            .map(UserDetailsImpl::new)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
