package org.opensingular.requirement.commons.service;

import java.io.File;
import java.util.Optional;
import javax.inject.Inject;

import org.opensingular.form.SIComposite;
import org.opensingular.lib.commons.dto.HtmlToPdfDTO;
import org.opensingular.lib.commons.pdf.HtmlToPdfConverter;
import org.opensingular.requirement.commons.extrato.ExtratoGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ExtratoGeneratorService {

    @Inject
    private HtmlToPdfConverter htmlToPdfConverter;

    @Inject
    private ExtratoGenerator extratoGenerator;

    @Inject
    private RequirementService<?, ?> requirementService;


    public Optional<File> generatePdfFile(Long codRequirement){
        return htmlToPdfConverter.convert(new HtmlToPdfDTO(generateHtml(codRequirement)));
    }

    public String generateHtml(Long codRequirement){
        SIComposite siComposite = requirementService.getRequirement(codRequirement).getMainForm();
        return extratoGenerator.generate(siComposite);
    }

}
