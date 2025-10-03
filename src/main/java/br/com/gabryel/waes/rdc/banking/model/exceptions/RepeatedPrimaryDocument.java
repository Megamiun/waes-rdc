package br.com.gabryel.waes.rdc.banking.model.exceptions;


import br.com.gabryel.waes.rdc.banking.model.entity.enums.DocumentType;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class RepeatedPrimaryDocument extends ResponseStatusException {
    private final DocumentType documentType;

    public RepeatedPrimaryDocument(DocumentType documentType) {
        super(HttpStatusCode.valueOf(400), "Document of type " + documentType + " with given value already exists");
        this.documentType = documentType;
    }
}
