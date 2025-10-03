package br.com.gabryel.waes.rdc.banking.model.exceptions;

import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class MissingPrimaryDocument extends ResponseStatusException {
    private final DocumentType documentType;

    public MissingPrimaryDocument(DocumentType documentType) {
        super(HttpStatusCode.valueOf(400), "Document with type " + documentType + " not provided");
        this.documentType = documentType;
    }
}
