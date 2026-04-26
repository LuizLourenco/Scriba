package br.dev.lourenco.scriba.modules.pessoas.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.modules.pessoas.domain.Leitor;
import br.dev.lourenco.scriba.modules.pessoas.dto.LeitorForm;
import br.dev.lourenco.scriba.modules.pessoas.service.LeitorService;
import br.dev.lourenco.scriba.modules.pessoas.service.TipoLeitorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pessoas/leitores")
public class LeitorController {

    private final LeitorService leitorService;
    private final TipoLeitorService tipoLeitorService;

    public LeitorController(LeitorService leitorService, TipoLeitorService tipoLeitorService) {
        this.leitorService = leitorService;
        this.tipoLeitorService = tipoLeitorService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("leitores", leitorService.listarDaInstituicaoAtual());
        return "pessoas/leitores/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        prepararFormulario(model, new LeitorForm());
        return "pessoas/leitores/form";
    }

    @PostMapping
    public String criar(
        @Valid @ModelAttribute("leitorForm") LeitorForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            prepararFormulario(model, form);
            return "pessoas/leitores/form";
        }

        leitorService.criar(form);

        redirectAttributes.addFlashAttribute("sucesso", "Leitor criado com sucesso.");
        return "redirect:/pessoas/leitores";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model) {
        Leitor leitor = leitorService.buscarDaInstituicaoAtual(id);
        model.addAttribute("leitorId", id);
        prepararFormulario(model, leitorService.paraFormulario(leitor));
        return "pessoas/leitores/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable UUID id,
        @Valid @ModelAttribute("leitorForm") LeitorForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("leitorId", id);
            prepararFormulario(model, form);
            return "pessoas/leitores/form";
        }

        leitorService.atualizar(id, form);

        redirectAttributes.addFlashAttribute("sucesso", "Leitor atualizado com sucesso.");
        return "redirect:/pessoas/leitores";
    }

    private void prepararFormulario(Model model, LeitorForm form) {
        model.addAttribute("leitorForm", form);
        model.addAttribute("tiposLeitor", tipoLeitorService.listarDaInstituicaoAtual());
    }
}
