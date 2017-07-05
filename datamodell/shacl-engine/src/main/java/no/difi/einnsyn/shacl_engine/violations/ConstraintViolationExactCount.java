package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.MinMaxConstraint;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;

import java.util.List;
import java.util.Optional;

/**
 * Created by sebastienmuller on 17/06/16.
 */
public class ConstraintViolationExactCount extends ConstraintViolation {

    private Optional<Integer> maxCount;
    private long actual;

    public ConstraintViolationExactCount(MinMaxConstraint propertyConstraint, Resource resource, String s) {
        super(propertyConstraint, resource, s);
        this.maxCount = propertyConstraint.getMaxCount();
        this.actual = propertyConstraint.getCount();
    }

    /**
     * Adding validation result triples specific to this kind of violations.
     * @return a list of statements containing validation results
     */
    @Override
    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        if (maxCount.isPresent()) {
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.expected, factory.createLiteral(maxCount.get())));
        }
        statements.add(factory.createStatement(validationResultsIri, SHACLExt.actual, factory.createLiteral(actual)));
        statements.add(factory.createStatement(validationResultsIri, RDF.TYPE, SHACLExt.ConstraintViolationExactCount));

        return statements;
    }
}
