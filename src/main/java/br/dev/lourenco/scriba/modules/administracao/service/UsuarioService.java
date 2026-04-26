package br.dev.lourenco.scriba.modules.administracao.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import br.dev.lourenco.scriba.modules.administracao.dto.UsuarioForm;
import br.dev.lourenco.scriba.modules.administracao.repository.BibliotecaRepository;
import br.dev.lourenco.scriba.modules.administracao.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final TenantContext tenantContext;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
        UsuarioRepository usuarioRepository,
        BibliotecaRepository bibliotecaRepository,
        TenantContext tenantContext,
        PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.bibliotecaRepository = bibliotecaRepository;
        this.tenantContext = tenantContext;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarUsuariosDaInstituicaoAtual() {
        return usuarioRepository.findAllByInstituicaoIdOrderByNomeAsc(tenantContext.requireInstituicaoId());
    }

    public Usuario buscarDaInstituicaoAtual(UUID id) {
        return usuarioRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    public UsuarioForm paraFormulario(Usuario usuario) {
        UsuarioForm form = new UsuarioForm();
        form.setNome(usuario.getNome());
        form.setEmail(usuario.getEmail());
        form.setRole(usuario.getRole());
        form.setAtivo(usuario.isAtivo());
        if (usuario.getBiblioteca() != null) {
            form.setBibliotecaId(usuario.getBiblioteca().getId());
        }
        return form;
    }

    @Transactional
    public Usuario criar(UsuarioForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (!StringUtils.hasText(form.getSenha())) {
            throw new BusinessException("Senha é obrigatória para novo usuário.");
        }
        if (usuarioRepository.existsByEmailIgnoreCaseAndInstituicaoId(form.getEmail(), instituicaoId)) {
            throw new BusinessException("Já existe usuário com este e-mail nesta instituição.");
        }

        Usuario usuario = new Usuario();
        usuario.setInstituicaoId(instituicaoId);
        aplicarFormulario(usuario, form, instituicaoId, true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(UUID id, UsuarioForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        Usuario usuario = buscarDaInstituicaoAtual(id);
        if (usuarioRepository.existsByEmailIgnoreCaseAndInstituicaoIdAndIdNot(form.getEmail(), instituicaoId, id)) {
            throw new BusinessException("Já existe usuário com este e-mail nesta instituição.");
        }
        aplicarFormulario(usuario, form, instituicaoId, false);
        return usuario;
    }

    @Transactional
    public void alterarAtivo(UUID id, boolean ativo) {
        Usuario usuario = buscarDaInstituicaoAtual(id);
        usuario.setAtivo(ativo);
    }

    private void aplicarFormulario(Usuario usuario, UsuarioForm form, UUID instituicaoId, boolean senhaObrigatoria) {
        usuario.setNome(form.getNome());
        usuario.setEmail(form.getEmail());
        usuario.setRole(form.getRole());
        usuario.setAtivo(form.isAtivo());
        usuario.setBiblioteca(resolverBiblioteca(form.getBibliotecaId(), instituicaoId));

        if (StringUtils.hasText(form.getSenha())) {
            usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        } else if (senhaObrigatoria) {
            throw new BusinessException("Senha é obrigatória para novo usuário.");
        }
    }

    private Biblioteca resolverBiblioteca(UUID bibliotecaId, UUID instituicaoId) {
        if (bibliotecaId == null) {
            return null;
        }
        return bibliotecaRepository.findByIdAndInstituicaoId(bibliotecaId, instituicaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Biblioteca não encontrada"));
    }
}
