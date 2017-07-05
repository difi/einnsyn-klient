package no.difi.einnsyn.shacl_engine.violations;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;

/**
 * Created by havardottestad on 06/05/16.
 * Modified by veronika.
 *
 * Superclass of all constraint violations in the SHACL engine.
 *
 */
public class ConstraintViolation {
    public static final String TYPE = "type";
    final PropertyConstraint propertyConstraint;
    final Resource resource;
    final String message;

    IRI validationResultsIri;
    ValueFactory factory = SimpleValueFactory.getInstance();

    ConstraintViolation(PropertyConstraint propertyConstraint, Resource resource, String message) {
        this.propertyConstraint = propertyConstraint;
        this.resource = resource;
        this.message = message;

        // TODO: Find a better namespace and ID.
        validationResultsIri = factory.createIRI("http://example.org/", UUID.randomUUID().toString());

    }

    /**
     * Adding validation results to a list of statements. The triples added in this method is common
     * for every constraint violation. The results in this project is an extended version of
     * SHACL results (https://www.w3.org/TR/shacl/#results).
     *
     * @return a list of statements containing validation results
     */
    public List<Statement> validationResults() {

        List<Statement> statements = new ArrayList<>();
        ValueFactory factory = SimpleValueFactory.getInstance();

        statements.add(factory.createStatement(validationResultsIri, SHACL.focusNode, resource));
        statements.add(factory.createStatement(validationResultsIri, SHACL.subject, resource));
        statements.add(factory.createStatement(validationResultsIri, SHACL.predicate, propertyConstraint.getPredicate()));

        statements.add(factory.createStatement(validationResultsIri, SHACL.severity, propertyConstraint.getSeverity()));

        Literal messageLiteral = factory.createLiteral(message);
        statements.add(factory.createStatement(validationResultsIri, SHACL.message, messageLiteral));

        statements.add(factory.createStatement(validationResultsIri, SHACL.sourceConstraint, propertyConstraint.getId()));
        statements.add(factory.createStatement(validationResultsIri, SHACL.scopeClass, propertyConstraint.getShape().getScopeClass()));


        return statements;
    }

    /**
     * This method runs the validation results into a list which gets read into a model for writing to a
     * StringWriter. Contents of this StringWriter is parsed to JSON-LD via third-party code (GSON), applying
     * the JSON-LD framing template.
     *
     * @return the validation results as framed JSON-LD
     * @throws JsonLdError
     */
    public JsonElement toJson() throws JsonLdError {

        StringWriter stringWriter = new StringWriter();
        List<Statement> statements = validationResults();

        RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, stringWriter);

        writer.startRDF();
        statements.forEach(writer::handleStatement);
        writer.endRDF();

        String jsonld = stringWriter.toString();

        try {
            Object jsonLdObject = JsonUtils.fromString(jsonld);

            String frameString = IOUtils.toString(ConstraintViolation.class.getClassLoader().getResourceAsStream("jsonLdFrame.json"));
            Object frame = JsonUtils.fromString(frameString);

            JsonLdOptions options = new JsonLdOptions();

            Map<String, Object> framedJsonLd = JsonLdProcessor.frame(jsonLdObject, frame, options);

            JsonElement parse = new JsonParser().parse(JsonUtils.toString(framedJsonLd));
            parse.getAsJsonObject().remove("@context");
            JsonArray asJsonArray = parse.getAsJsonObject().getAsJsonArray("@graph");


            if(asJsonArray.size() != 1) {
                throw new IllegalStateException("More than one result was generated for a constraint violation");
            }

            return asJsonArray.get(0);

        } catch (IOException e) {
            throw new IllegalStateException("There should never be an IOException when reading from a string");
        }
    }



    @Override
    public String toString() {

        return "ConstraintViolation{" +
            "propertyConstraint=" + propertyConstraint +
            ", resource=" + resource +
            ", message='" + message + '\'' +
            '}';
    }

    public IRI getSeverity() {
        return propertyConstraint.getSeverity();
    }
}
