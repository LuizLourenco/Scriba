package br.dev.lourenco.scriba.modules.acervo.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.modules.acervo.domain.StatusAcervo;
import br.dev.lourenco.scriba.modules.acervo.domain.TipoItem;
import br.dev.lourenco.scriba.modules.acervo.domain.TipoMidia;
import br.dev.lourenco.scriba.modules.acervo.domain.TipoPeriodico;
import br.dev.lourenco.scriba.modules.acervo.dto.AcervoItemForm;
import br.dev.lourenco.scriba.modules.acervo.dto.MudarStatusForm;
import br.dev.lourenco.scriba.modules.acervo.service.AcervoService;
import br.dev.lourenco.scriba.modules.administracao.service.BibliotecaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/acervo/itens")
public class AcervoItemController {

    private final AcervoService acervoService;
    private final BibliotecaService bibliotecaService;

    public AcervoItemController(AcervoService acervoService, BibliotecaService bibliotecaService) {
        this.acervoService = acervoService;
        this.bibliotecaService = bibliotecaService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "busca", required = false) String busca, Model model) {
        model.addAttribute("itens", acervoService.listarDaInstituicaoAtual(busca));
        model.addAttribute("busca", busca);
        model.addAttribute("statusDisponiveis", StatusAcervo.values());
        model.addAttribute("mudarStatusForm", new MudarStatusForm());
        return "itens/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararFormulario(model, new AcervoItemForm());
        return "itens/form";
    }

    @PostMapping
    public String criar(
        @Valid AcervoItemForm acervoItemForm,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            prepararFormulario(model, acervoItemForm);
            return "itens/form";
        }
        try {
            acervoService.criar(acervoItemForm);
        } catch (BusinessException exception) {
            binding.reject("acervo", exception.getMessage());
            prepararFormulario(model, acervoItemForm);
            return "itens/form";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Item do acervo criado com sucesso.");
        return "redirect:/acervo/itens";
    }

    @PatchMapping("/{id}/status")
    public String mudarStatusPatch(@PathVariable UUID id, @Valid MudarStatusForm form, BindingResult binding, Model model) {
        if (!binding.hasErrors()) {
            acervoService.mudarStatus(id, form.getNovoStatus());
        }
        return listar(null, model);
    }

    @PostMapping("/{id}/status")
    public String mudarStatusPost(@PathVariable UUID id, @Valid MudarStatusForm form, RedirectAttributes redirectAttributes) {
        acervoService.mudarStatus(id, form.getNovoStatus());
        redirectAttributes.addFlashAttribute("sucesso", "Status atualizado com sucesso.");
        return "redirect:/acervo/itens";
    }

    private void prepararFormulario(Model model, AcervoItemForm form) {
        model.addAttribute("acervoItemForm", form);
        model.addAttribute("tiposItem", TipoItem.values());
        model.addAttribute("tiposPeriodico", TipoPeriodico.values());
        model.addAttribute("tiposMidia", TipoMidia.values());
        model.addAttribute("bibliotecas", bibliotecaService.listarDaInstituicaoAtual());
    }
}
