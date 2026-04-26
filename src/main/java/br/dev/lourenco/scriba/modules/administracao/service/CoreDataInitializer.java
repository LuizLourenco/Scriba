package br.dev.lourenco.scriba.modules.administracao.service;

import java.util.List;

import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import br.dev.lourenco.scriba.modules.administracao.domain.Instituicao;
import br.dev.lourenco.scriba.modules.administracao.domain.Role;
import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import br.dev.lourenco.scriba.modules.administracao.repository.BibliotecaRepository;
import br.dev.lourenco.scriba.modules.administracao.repository.InstituicaoRepository;
import br.dev.lourenco.scriba.modules.administracao.repository.UsuarioRepository;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoLeitor;
import br.dev.lourenco.scriba.modules.pessoas.repository.LeitorRepository;
import br.dev.lourenco.scriba.modules.pessoas.repository.TipoLeitorRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CoreDataInitializer {

    @Bean
    ApplicationRunner seedCoreData(
        InstituicaoRepository instituicaoRepository,
        BibliotecaRepository bibliotecaRepository,
        UsuarioRepository usuarioRepository,
        TipoLeitorRepository tipoLeitorRepository,
        LeitorRepository leitorRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (usuarioRepository.count() > 0) {
                return;
            }

            Instituicao instituicao = new Instituicao();
            instituicao.setNome("Scriba Instituição");
            instituicao.setCodigo("SCRIBA");
            instituicaoRepository.save(instituicao);

            Biblioteca biblioteca = new Biblioteca();
            biblioteca.setInstituicaoId(instituicao.getId());
            biblioteca.setNome("Biblioteca Central");
            biblioteca.setCodigo("CENTRAL");
            bibliotecaRepository.save(biblioteca);

            usuarioRepository.saveAll(List.of(
                novoUsuario("Administrador", "admin@scriba.dev", Role.ADMIN, biblioteca, passwordEncoder, instituicao.getId()),
                novoUsuario("Bibliotecária", "bibliotecario@scriba.dev", Role.BIBLIOTECARIO, biblioteca, passwordEncoder, instituicao.getId()),
                novoUsuario("Leitora", "leitor@scriba.dev", Role.LEITOR, biblioteca, passwordEncoder, instituicao.getId())
            ));

            TipoLeitor tipoLeitor = new TipoLeitor();
            tipoLeitor.setInstituicaoId(instituicao.getId());
            tipoLeitor.setNome("Leitor padrao");
            tipoLeitorRepository.save(tipoLeitor);

            Leitor leitor = new Leitor();
            leitor.setInstituicaoId(instituicao.getId());
            leitor.setTipoLeitor(tipoLeitor);
            leitor.setNome("Leitora");
            leitor.setCpf("00000000000");
            leitor.setEmail("leitor@scriba.dev");
            leitorRepository.save(leitor);
        };
    }

    private Usuario novoUsuario(
        String nome,
        String email,
        Role role,
        Biblioteca biblioteca,
        PasswordEncoder passwordEncoder,
        java.util.UUID instituicaoId
    ) {
        Usuario usuario = new Usuario();
        usuario.setInstituicaoId(instituicaoId);
        usuario.setBiblioteca(biblioteca);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setRole(role);
        usuario.setSenha(passwordEncoder.encode(role == Role.ADMIN ? "admin123" : "senha123"));
        return usuario;
    }
}
