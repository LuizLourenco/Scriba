package br.dev.lourenco.scriba.modules.curadoria.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.security.SecurityUtils;
import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import br.dev.lourenco.scriba.modules.administracao.domain.Role;
import br.dev.lourenco.scriba.modules.administracao.domain.Usuario;
import br.dev.lourenco.scriba.modules.administracao.repository.BibliotecaRepository;
import br.dev.lourenco.scriba.modules.administracao.repository.UsuarioRepository;
import br.dev.lourenco.scriba.modules.curadoria.domain.Desbastamento;
import br.dev.lourenco.scriba.modules.curadoria.domain.TipoDesbastamento;
import br.dev.lourenco.scriba.modules.curadoria.dto.DesbastamentoForm;
import br.dev.lourenco.scriba.modules.curadoria.repository.DesbastamentoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class CuradoriaService {

    private final DesbastamentoRepository desbastamentoRepository;
    private final AcervoItemRepository acervoItemRepository;
    private final UsuarioRepository usuarioRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final TenantContext tenantContext;

    public CuradoriaService(
        DesbastamentoRepository desbastamentoRepository,
        AcervoItemRepository acervoItemRepository,
        UsuarioRepository usuarioRepository,
        BibliotecaRepository bibliotecaRepository,
        TenantContext tenantContext
    ) {
        this.desbastamentoRepository = desbastamentoRepository;
        this.acervoItemRepository = acervoItemRepository;
        this.usuarioRepository = usuarioRepository;
        this.bibliotecaRepository = bibliotecaRepository;
        this.tenantContext = tenantContext;
    }

    public List<Desbastamento> listarDaInstituicaoAtual() {
        return desbastamentoRepository.findAllByInstituicaoIdOrderByCriadoEmDesc(tenantContext.requireInstituicaoId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Desbastamento registrar(DesbastamentoForm form) {
        validarAdminProgramaticamente();
        validarJustificativa(form.getJustificativa());

        UUID instituicaoId = tenantContext.requireInstituicaoId();
        AcervoItem item = acervoItemRepository.findByIdAndInstituicaoId(form.getAcervoItemId(), instituicaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Item do acervo não encontrado"));
        Usuario usuario = usuarioRepository.findByIdAndInstituicaoId(SecurityUtils.currentUserId(), instituicaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário auditor não encontrado"));

        validarItemOperavel(item);

        Desbastamento desbastamento = new Desbastamento();
        desbastamento.setInstituicaoId(instituicaoId);
        desbastamento.setAcervoItem(item);
        desbastamento.setUsuario(usuario);
        desbastamento.setTipo(form.getTipo());
        desbastamento.setJustificativa(form.getJustificativa().trim());

        if (form.getTipo() == TipoDesbastamento.DESCARTE) {
            item.mudarStatus(StatusAcervo.DESCARTADO);
        } else if (form.getTipo() == TipoDesbastamento.REMANEJAMENTO) {
            Biblioteca destino = resolverDestino(form.getDestinoBibliotecaId(), instituicaoId);
            desbastamento.setDestinoBiblioteca(destino);
            desbastamento.setDestinoInstituicaoId(destino.getInstituicaoId());
            item.mudarStatus(StatusAcervo.REMANEJADO);
        } else {
            throw new BusinessException("Tipo de desbastamento inválido.");
        }

        return desbastamentoRepository.save(desbastamento);
    }

    private void validarAdminProgramaticamente() {
        UserDetailsImpl userDetails = SecurityUtils.currentUserDetails();
        if (userDetails == null || userDetails.getUsuario().getRole() != Role.ADMIN) {
            throw new BusinessException("Apenas administradores podem executar curadoria.");
        }
    }

    private void validarJustificativa(String justificativa) {
        if (!StringUtils.hasText(justificativa)) {
            throw new BusinessException("Justificativa é obrigatória");
        }
    }

    private void validarItemOperavel(AcervoItem item) {
        if (item.getStatus() == StatusAcervo.EMPRESTADO || item.getStatus() == StatusAcervo.RESERVADO) {
            throw new BusinessException("Item em status " + item.getStatus() + " não pode ser descartado ou remanejado");
        }
    }

    private Biblioteca resolverDestino(UUID destinoBibliotecaId, UUID instituicaoId) {
        if (destinoBibliotecaId == null) {
            throw new BusinessException("Biblioteca de destino é obrigatória para remanejamento.");
        }
        return bibliotecaRepository.findByIdAndInstituicaoId(destinoBibliotecaId, instituicaoId)
            .orElseThrow(() -> new BusinessException("Biblioteca de destino não pertence à instituição atual."));
    }
}
