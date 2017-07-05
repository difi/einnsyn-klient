package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.ClassConstraint;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import java.util.List;

/**
 * Created by veronika on 5/10/16.
 * Modified by havardottestad.
 *
 * This is an subclass of ConstraintViolation which will produce validation results specific on the property
 * shacl:class. Is the data triple of an expected type?
 *
 */
public class ConstraintViolationClass extends ConstraintViolationWithStatement {

    private final List<Value> actualStatements;
    private IRI class_property;

    public ConstraintViolationClass(ClassConstraint propertyConstraint, Resource resource, String message, Statement failingStatement, List<Value> actualStatements) {
        super(propertyConstraint, resource, message, failingStatement);

        if (propertyConstraint.getClassProperty() != null) {
            this.class_property = propertyConstraint.getClassProperty();
        }

        this.actualStatements = actualStatements;
    }

    /**
     * Adding validation result triples specific to this kind of violations.
     * @return a list of statements containing validation results
     */
    @Override
    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        statements.add(factory.createStatement(validationResultsIri, SHACLExt.expected, factory.createLiteral(class_property.toString())));
        statements.add(factory.createStatement(validationResultsIri, RDF.TYPE, SHACLExt.ConstraintViolationClass));

        actualStatements.forEach(actual -> {
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.actual, actual));

        });

        return statements;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ConstraintViolationClass) {

            ConstraintViolationClass classObject = (ConstraintViolationClass) obj;

            return classObject.message.equals(message) && classObject.resource.equals(resource);

        } else {
            return false;
        }
    }
}
