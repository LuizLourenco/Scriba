package br.dev.lourenco.scriba.modules.pessoas.controller;

import java.util.UUID;

import br.dev.lourenco.scriba.core.exception.BusinessException;
import br.dev.lourenco.scriba.modules.pessoas.domain.TipoLeitor;
import br.dev.lourenco.scriba.modules.pessoas.dto.TipoLeitorForm;
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
@RequestMapping("/pessoas/tipos-leitor")
public class TipoLeitorController {

    private final TipoLeitorService tipoLeitorService;

    public TipoLeitorController(TipoLeitorService tipoLeitorService) {
        this.tipoLeitorService = tipoLeitorService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tiposLeitor", tipoLeitorService.listarDaInstituicaoAtual());
        return "pessoas/tipos-leitor/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tipoLeitorForm", new TipoLeitorForm());
        return "pessoas/tipos-leitor/form";
    }

    @PostMapping
    public String criar(
        @Valid @ModelAttribute("tipoLeitorForm") TipoLeitorForm form,
        BindingResult binding,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            return "pessoas/tipos-leitor/form";
        }

        try {
            tipoLeitorService.criar(form);
        } catch (BusinessException exception) {
            binding.reject("tipoLeitor", exception.getMessage());
            return "pessoas/tipos-leitor/form";
        }

        redirectAttributes.addFlashAttribute("sucesso", "Tipo de leitor criado com sucesso.");
        return "redirect:/pessoas/tipos-leitor";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model) {
        TipoLeitor tipoLeitor = tipoLeitorService.buscarDaInstituicaoAtual(id);
        model.addAttribute("tipoLeitorId", id);
        model.addAttribute("tipoLeitorForm", tipoLeitorService.paraFormulario(tipoLeitor));
        return "pessoas/tipos-leitor/form";
    }

    @PostMapping("/{id}")
    public String atualizar(
        @PathVariable UUID id,
        @Valid @ModelAttribute("tipoLeitorForm") TipoLeitorForm form,
        BindingResult binding,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (binding.hasErrors()) {
            model.addAttribute("tipoLeitorId", id);
            return "pessoas/tipos-leitor/form";
        }

        try {
            tipoLeitorService.atualizar(id, form);
        } catch (BusinessException exception) {
            binding.reject("tipoLeitor", exception.getMessage());
            model.addAttribute("tipoLeitorId", id);
            return "pessoas/tipos-leitor/form";
        }

        redirectAttributes.addFlashAttribute("sucesso", "Tipo de leitor atualizado com sucesso.");
        return "redirect:/pessoas/tipos-leitor";
    }
}
