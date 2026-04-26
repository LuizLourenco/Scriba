package br.dev.lourenco.scriba.core;

import java.util.UUID;

import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import br.dev.lourenco.scriba.modules.administracao.domain.Instituicao;
import br.dev.lourenco.scriba.modules.administracao.domain.Role;
import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import br.dev.lourenco.scriba.modules.administracao.repository.BibliotecaRepository;
import br.dev.lourenco.scriba.modules.administracao.repository.InstituicaoRepository;
import br.dev.lourenco.scriba.modules.administracao.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractCoreIntegrationTest {

    @Autowired
    protected InstituicaoRepository instituicaoRepository;

    @Autowired
    protected BibliotecaRepository bibliotecaRepository;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Usuario usuarioOutroTenant;

    @BeforeEach
    void ensureCrossTenantFixtures() {
        reativarUsuarioPadrao("admin@scriba.dev");
        reativarUsuarioPadrao("bibliotecario@scriba.dev");
        reativarUsuarioPadrao("leitor@scriba.dev");
        usuarioOutroTenant = usuarioRepository.findByEmailIgnoreCase("outro-tenant@scriba.dev")
            .orElseGet(this::criarUsuarioDeOutroTenant);
    }

    private void reativarUsuarioPadrao(String email) {
        usuarioRepository.findByEmailIgnoreCase(email).ifPresent(usuario -> {
            if (!usuario.isAtivo()) {
                usuario.setAtivo(true);
                usuarioRepository.save(usuario);
            }
        });
    }

    private Usuario criarUsuarioDeOutroTenant() {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Instituição Externa");
        instituicao.setCodigo("EXT-" + UUID.randomUUID());
        instituicaoRepository.save(instituicao);

        Biblioteca biblioteca = new Biblioteca();
        biblioteca.setInstituicaoId(instituicao.getId());
        biblioteca.setNome("Biblioteca Externa");
        biblioteca.setCodigo("BIB-" + UUID.randomUUID());
        bibliotecaRepository.save(biblioteca);

        Usuario usuario = new Usuario();
        usuario.setInstituicaoId(instituicao.getId());
        usuario.setBiblioteca(biblioteca);
        usuario.setNome("Usuário Externo");
        usuario.setEmail("outro-tenant@scriba.dev");
        usuario.setSenha(passwordEncoder.encode("senha123"));
        usuario.setRole(Role.BIBLIOTECARIO);
        return usuarioRepository.save(usuario);
    }
}
