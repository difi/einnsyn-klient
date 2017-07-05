package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

import java.util.List;

/**
 * Created by veronika on 5/20/16.
 * Modified by havardottestad.
 *
 * A subclass of ConstraintViolation where we have an additional parameter (Statement) to the constructor.
 * With a statement we may reach the object of the triple who creates the violation. In some cases, we want to know
 * what the object is. For instance, when there is a literal instead of an URI at the object position in a triple.
 *
 */
class ConstraintViolationWithStatement extends ConstraintViolation{

    private Statement failingStatement;

    ConstraintViolationWithStatement(PropertyConstraint propertyConstraint, Resource resource, String message, Statement failingStatement) {
        super(propertyConstraint, resource, message);
        this.failingStatement = failingStatement;
    }

    /**
     * Adding validation result triples specific to this kind of violations.
     * @return a list of statements containing validation results
     */
    @Override
    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        statements.add(factory.createStatement(validationResultsIri, SHACL.object, failingStatement.getObject()));

        return statements;
    }
}
