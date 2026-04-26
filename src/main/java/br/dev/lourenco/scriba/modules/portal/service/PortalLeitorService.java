package br.dev.lourenco.scriba.modules.portal.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.security.SecurityUtils;
import br.dev.lourenco.scriba.core.security.UserDetailsImpl;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import br.dev.lourenco.scriba.modules.circulacao.domain.Emprestimo;
import br.dev.lourenco.scriba.modules.circulacao.domain.Multa;
import br.dev.lourenco.scriba.modules.circulacao.domain.Reserva;
import br.dev.lourenco.scriba.modules.circulacao.repository.EmprestimoRepository;
import br.dev.lourenco.scriba.modules.circulacao.repository.MultaRepository;
import br.dev.lourenco.scriba.modules.circulacao.repository.ReservaRepository;
import br.dev.lourenco.scriba.modules.circulacao.service.CirculacaoService;
import br.dev.lourenco.scriba.modules.notificacao.service.NotificacaoService;
import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import br.dev.lourenco.scriba.modules.pessoas.repository.LeitorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class PortalLeitorService {

    private static final List<StatusAcervo> STATUS_CONSULTAVEIS = List.of(StatusAcervo.DISPONIVEL, StatusAcervo.RESERVADO);

    private final AcervoItemRepository acervoItemRepository;
    private final LeitorRepository leitorRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ReservaRepository reservaRepository;
    private final MultaRepository multaRepository;
    private final CirculacaoService circulacaoService;
    private final NotificacaoService notificacaoService;
    private final TenantContext tenantContext;

    public PortalLeitorService(
        AcervoItemRepository acervoItemRepository,
        LeitorRepository leitorRepository,
        EmprestimoRepository emprestimoRepository,
        ReservaRepository reservaRepository,
        MultaRepository multaRepository,
        CirculacaoService circulacaoService,
        NotificacaoService notificacaoService,
        TenantContext tenantContext
    ) {
        this.acervoItemRepository = acervoItemRepository;
        this.leitorRepository = leitorRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
        this.multaRepository = multaRepository;
        this.circulacaoService = circulacaoService;
        this.notificacaoService = notificacaoService;
        this.tenantContext = tenantContext;
    }

    public List<AcervoItem> consultarAcervo(String busca) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (StringUtils.hasText(busca)) {
            return acervoItemRepository.findAllByInstituicaoIdAndStatusInAndTituloContainingIgnoreCaseOrderByTituloAsc(
                instituicaoId,
                STATUS_CONSULTAVEIS,
                busca
            );
        }
        return acervoItemRepository.findAllByInstituicaoIdAndStatusInOrderByTituloAsc(instituicaoId, STATUS_CONSULTAVEIS);
    }

    public Leitor leitorAtual() {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        UserDetailsImpl userDetails = SecurityUtils.currentUserDetails();
        if (userDetails == null) {
            throw new ResourceNotFoundException("Leitor da sessão não encontrado");
        }
        return leitorRepository.findByEmailIgnoreCaseAndInstituicaoId(userDetails.getUsername(), instituicaoId)
            .orElseThrow(() -> new ResourceNotFoundException("Leitor da sessão não encontrado"));
    }

    public List<Emprestimo> emprestimosDoLeitorAtual() {
        Leitor leitor = leitorAtual();
        return emprestimoRepository.findAllByLeitorIdAndInstituicaoIdOrderByDataEmprestimoDesc(
            leitor.getId(),
            leitor.getInstituicaoId()
        );
    }

    public List<Reserva> reservasDoLeitorAtual() {
        Leitor leitor = leitorAtual();
        return reservaRepository.findAllByLeitorIdAndInstituicaoIdOrderByDataReservaDesc(leitor.getId(), leitor.getInstituicaoId());
    }

    public List<Multa> multasDoLeitorAtual() {
        Leitor leitor = leitorAtual();
        return multaRepository.findAllByLeitorIdAndInstituicaoIdOrderByDataGeracaoDesc(leitor.getId(), leitor.getInstituicaoId());
    }

    @Transactional
    public Reserva reservar(UUID acervoItemId) {
        Leitor leitor = leitorAtual();
        Reserva reserva = circulacaoService.reservar(acervoItemId, leitor.getId());
        notificacaoService.notificarReservaConfirmada(reserva);
        return reserva;
    }
}
