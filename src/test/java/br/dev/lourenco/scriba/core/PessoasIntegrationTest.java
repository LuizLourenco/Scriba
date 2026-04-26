package br.dev.lourenco.scriba.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.security.UserDetailsServiceImpl;
import br.dev.lourenco.scriba.modules.pessoas.domain.Fornecedor;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoFornecedor;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoLeitor;
import br.dev.lourenco.scriba.modules.pessoas.repository.FornecedorRepository;
import br.dev.lourenco.scriba.modules.pessoas.repository.LeitorRepository;
import br.dev.lourenco.scriba.modules.pessoas.repository.TipoLeitorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class PessoasIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private TipoLeitorRepository tipoLeitorRepository;

    @Autowired
    private LeitorRepository leitorRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Test
    void bibliotecarioCriaTipoLeitorELeitorNoTenantAtual() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        TipoLeitor tipoLeitor = criarTipoLeitor(bibliotecario.getInstituicaoId(), "Comunidade " + java.util.UUID.randomUUID());
        String email = "joao-" + java.util.UUID.randomUUID() + "@email.com";

        mvc.perform(post("/pessoas/leitores")
                .with(csrf())
                .with(user(bibliotecario))
                .param("nome", "Joao Silva")
                .param("cpf", String.valueOf(System.nanoTime()).substring(0, 11))
                .param("email", email)
                .param("tipoLeitorId", tipoLeitor.getId().toString())
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/pessoas/leitores"));

        Leitor leitor = leitorRepository.findAllByInstituicaoIdOrderByNomeAsc(bibliotecario.getInstituicaoId())
            .stream()
            .filter(item -> email.equals(item.getEmail()))
            .findFirst()
            .orElseThrow();

        assertThat(leitor.getInstituicaoId()).isEqualTo(bibliotecario.getInstituicaoId());
        assertThat(leitor.getTipoLeitor().getId()).isEqualTo(tipoLeitor.getId());
    }

    @Test
    void bloqueiaLeitorComTipoLeitorDeOutraInstituicao() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        TipoLeitor tipoOutroTenant = criarTipoLeitor(usuarioOutroTenant.getInstituicaoId(), "Externo " + java.util.UUID.randomUUID());

        mvc.perform(post("/pessoas/leitores")
                .with(csrf())
                .with(user(bibliotecario))
                .param("nome", "Leitor Invalido")
                .param("cpf", String.valueOf(System.nanoTime()).substring(0, 11))
                .param("email", "invalido-" + java.util.UUID.randomUUID() + "@email.com")
                .param("tipoLeitorId", tipoOutroTenant.getId().toString())
                .param("ativo", "true"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(content().string(containsString("não pertence à instituição")));
    }

    @Test
    void listagemDeLeitoresRespeitaTenant() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        TipoLeitor tipoAtual = criarTipoLeitor(bibliotecario.getInstituicaoId(), "Atual " + java.util.UUID.randomUUID());
        TipoLeitor tipoOutro = criarTipoLeitor(usuarioOutroTenant.getInstituicaoId(), "Outro " + java.util.UUID.randomUUID());
        Leitor leitorAtual = criarLeitor(bibliotecario.getInstituicaoId(), tipoAtual, "atual");
        Leitor leitorOutro = criarLeitor(usuarioOutroTenant.getInstituicaoId(), tipoOutro, "outro");

        mvc.perform(get("/pessoas/leitores").with(user(bibliotecario)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(leitorAtual.getEmail())))
            .andExpect(content().string(not(containsString(leitorOutro.getEmail()))));
    }

    @Test
    void criaFornecedorPfEPj() throws Exception {
        UserDetailsImpl bibliotecario = (UserDetailsImpl) userDetailsService.loadUserByUsername("bibliotecario@scriba.dev");
        String cpf = "1" + String.valueOf(System.nanoTime()).substring(0, 10);
        String cnpj = "12" + String.valueOf(System.nanoTime()).substring(0, 12);

        mvc.perform(post("/pessoas/fornecedores")
                .with(csrf())
                .with(user(bibliotecario))
                .param("nome", "Ana Fornecedora")
                .param("tipo", TipoFornecedor.PF.name())
                .param("cpfCnpj", cpf)
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/pessoas/fornecedores"));

        mvc.perform(post("/pessoas/fornecedores")
                .with(csrf())
                .with(user(bibliotecario))
                .param("nome", "Editora ABC")
                .param("tipo", TipoFornecedor.PJ.name())
                .param("cpfCnpj", cnpj)
                .param("ativo", "true"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/pessoas/fornecedores"));

        assertThat(buscarFornecedor(cpf, bibliotecario).getTipo()).isEqualTo(TipoFornecedor.PF);
        assertThat(buscarFornecedor(cnpj, bibliotecario).getTipo()).isEqualTo(TipoFornecedor.PJ);
    }

    private TipoLeitor criarTipoLeitor(java.util.UUID instituicaoId, String nome) {
        TipoLeitor tipoLeitor = new TipoLeitor();
        tipoLeitor.setInstituicaoId(instituicaoId);
        tipoLeitor.setNome(nome);
        tipoLeitor.setPrazoPadraoDias(14);
        tipoLeitor.setLimiteEmprestimos(3);
        return tipoLeitorRepository.save(tipoLeitor);
    }

    private Leitor criarLeitor(java.util.UUID instituicaoId, TipoLeitor tipoLeitor, String prefixo) {
        Leitor leitor = new Leitor();
        leitor.setInstituicaoId(instituicaoId);
        leitor.setTipoLeitor(tipoLeitor);
        leitor.setNome("Leitor " + prefixo);
        leitor.setCpf(String.valueOf(System.nanoTime()).substring(0, 11));
        leitor.setEmail(prefixo + "-" + java.util.UUID.randomUUID() + "@email.com");
        return leitorRepository.save(leitor);
    }

    private Fornecedor buscarFornecedor(String cpfCnpj, UserDetailsImpl usuario) {
        return fornecedorRepository.findAllByInstituicaoIdOrderByNomeAsc(usuario.getInstituicaoId())
            .stream()
            .filter(item -> cpfCnpj.equals(item.getCpfCnpj()))
            .findFirst()
            .orElseThrow();
    }
}
