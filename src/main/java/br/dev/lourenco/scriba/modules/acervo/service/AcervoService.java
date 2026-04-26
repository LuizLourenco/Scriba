package br.dev.lourenco.scriba.modules.acervo.service;

import java.util.List;
import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.core.exception.ResourceNotFoundException;
import br.dev.lourenco.scriba.core.tenant.TenantContext;
import br.dev.lourenco.scriba.modules.acervo.domain.AcervoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.Carta;
import br.dev.lourenco.scriba.modules.acervo.domain.Foto;
import br.dev.lourenco.scriba.modules.acervo.domain.Livro;
import br.dev.lourenco.scriba.modules.acervo.domain.Midia;
import br.dev.lourenco.scriba.modules.acervo.domain.Periodico;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.dto.AcervoItemForm;
import br.dev.lourenco.scriba.modules.acervo.repository.AcervoItemRepository;
import br.dev.lourenco.scriba.modules.administracao.domain.Biblioteca;
import br.dev.lourenco.scriba.modules.administracao.repository.BibliotecaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AcervoService {

    private final AcervoItemRepository acervoItemRepository;
    private final BibliotecaRepository bibliotecaRepository;
    private final TenantContext tenantContext;

    public AcervoService(
        AcervoItemRepository acervoItemRepository,
        BibliotecaRepository bibliotecaRepository,
        TenantContext tenantContext
    ) {
        this.acervoItemRepository = acervoItemRepository;
        this.bibliotecaRepository = bibliotecaRepository;
        this.tenantContext = tenantContext;
    }

    public List<AcervoItem> listarDaInstituicaoAtual(String busca) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (StringUtils.hasText(busca)) {
            return acervoItemRepository.findAllByInstituicaoIdAndTituloContainingIgnoreCaseOrderByTituloAsc(instituicaoId, busca);
        }
        return acervoItemRepository.findAllByInstituicaoIdOrderByTituloAsc(instituicaoId);
    }

    public AcervoItem buscarDaInstituicaoAtual(UUID id) {
        return acervoItemRepository.findByIdAndInstituicaoId(id, tenantContext.requireInstituicaoId())
            .orElseThrow(() -> new ResourceNotFoundException("Item do acervo não encontrado"));
    }

    @Transactional
    public AcervoItem criar(AcervoItemForm form) {
        UUID instituicaoId = tenantContext.requireInstituicaoId();
        if (acervoItemRepository.existsByTomboIgnoreCaseAndInstituicaoId(form.getTombo(), instituicaoId)) {
            throw new BusinessException("Já existe item com este tombo nesta instituição.");
        }

        AcervoItem item = novoItem(form);
        item.setInstituicaoId(instituicaoId);
        item.setStatus(StatusAcervo.DISPONIVEL);
        aplicarCamposComuns(item, form, instituicaoId);
        aplicarCamposEspecificos(item, form);
        return acervoItemRepository.save(item);
    }

    @Transactional
    public AcervoItem mudarStatus(UUID id, StatusAcervo novoStatus) {
        AcervoItem item = buscarDaInstituicaoAtual(id);
        item.mudarStatus(novoStatus);
        return item;
    }

    private AcervoItem novoItem(AcervoItemForm form) {
        return switch (form.getTipoItem()) {
            case LIVRO -> new Livro();
            case PERIODICO -> new Periodico();
            case CARTA -> new Carta();
            case FOTO -> new Foto();
            case MIDIA -> new Midia();
        };
    }

    private void aplicarCamposComuns(AcervoItem item, AcervoItemForm form, UUID instituicaoId) {
        item.setTitulo(form.getTitulo());
        item.setTombo(form.getTombo());
        item.setCodigoBarras(form.getCodigoBarras());
        item.setLocalizacao(form.getLocalizacao());
        item.setDataAquisicao(form.getDataAquisicao());
        item.setBiblioteca(resolverBiblioteca(form.getBibliotecaId(), instituicaoId));
    }

    private Biblioteca resolverBiblioteca(UUID bibliotecaId, UUID instituicaoId) {
        if (bibliotecaId == null) {
            return null;
        }
        return bibliotecaRepository.findByIdAndInstituicaoId(bibliotecaId, instituicaoId)
            .orElseThrow(() -> new BusinessException("Biblioteca não pertence à instituição atual."));
    }

    private void aplicarCamposEspecificos(AcervoItem item, AcervoItemForm form) {
        switch (item) {
            case Livro livro -> {
                livro.setIsbn(form.getIsbn());
                livro.setNumeroPaginas(form.getNumeroPaginas());
                livro.setEdicao(form.getEdicao());
                livro.setVolume(form.getVolume());
            }
            case Periodico periodico -> {
                periodico.setTipoPeriodico(form.getTipoPeriodico());
                periodico.setIssn(form.getIssn());
                periodico.setVolume(form.getVolume());
                periodico.setNumero(form.getNumero());
            }
            case Carta carta -> {
                carta.setRemetente(form.getRemetente());
                carta.setDestinatario(form.getDestinatario());
                carta.setDataEnvio(form.getDataEnvio());
            }
            case Foto foto -> {
                foto.setAssunto(form.getAssunto());
                foto.setFotografo(form.getFotografo());
                foto.setFormato(form.getFormato());
                foto.setResolucao(form.getResolucao());
            }
            case Midia midia -> {
                midia.setTipoMidia(form.getTipoMidia());
                midia.setDuracao(form.getDuracao());
                midia.setProdutora(form.getProdutora());
            }
            default -> throw new BusinessException("Tipo de item não suportado.");
        }
    }
}
