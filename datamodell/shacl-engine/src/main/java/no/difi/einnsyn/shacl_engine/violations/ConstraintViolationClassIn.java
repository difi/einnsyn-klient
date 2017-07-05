package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.ClassInConstraint;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;

import java.util.List;

/**
 * Created by sebastienmuller on 09/06/16.
 */
public class ConstraintViolationClassIn extends ConstraintViolation {
    private final ClassInConstraint classIn;
    private final List<Statement> collect;

    public ConstraintViolationClassIn(ClassInConstraint classIn, Resource resource, String message, Statement statement, List<Statement> collect) {
        super(classIn, resource, message);
        this.classIn = classIn;
        this.collect = collect;

    }

    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        classIn.classIn.forEach((k, v) -> {
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.expected, k));
        });

        collect.forEach(v -> {
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.actual, v.getObject()));
        });

        statements.add(factory.createStatement(validationResultsIri, RDF.TYPE, SHACLExt.ConstraintViolationClass));

        return statements;
    }

}
