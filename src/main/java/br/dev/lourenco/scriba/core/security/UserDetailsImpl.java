package br.dev.lourenco.scriba.core.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsImpl implements UserDetails {

    private final Usuario usuario;

    public UserDetailsImpl(Usuario usuario) {
        this.usuario = usuario;
    }

    public UUID getId() {
        return usuario.getId();
    }

    public UUID getInstituicaoId() {
        return usuario.getInstituicaoId();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return usuario.isAtivo();
    }

    @Override
    public boolean isAccountNonLocked() {
        return usuario.isAtivo();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return usuario.isAtivo();
    }

    @Override
    public boolean isEnabled() {
        return usuario.isAtivo();
    }
}
