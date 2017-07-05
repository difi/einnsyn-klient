package no.difi.einnsyn.shacl_engine.violations;

import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.DatatypeConstraint;
import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;

import java.util.List;

/**
 * Created by havardottestad on 06/05/16.
 * Modified by veronika.
 *
 * This is a subclass of ConstraintViolation which will produce validation results specific to the property
 * shacl:datatype. Is this literal of the expected datatype?
 *
 */
public class ConstraintViolationDatatype extends ConstraintViolationWithStatement {
    private final IRI actualDatatype;
    private final IRI expectedDatatype;

    public ConstraintViolationDatatype(DatatypeConstraint propertyConstraint, Resource resource, String s, Statement failingStatement, IRI actualDatatype) {
        super(propertyConstraint, resource, s, failingStatement);
        this.actualDatatype = actualDatatype;
        this.expectedDatatype = propertyConstraint.getDatatype();
    }

    @Override
    public String toString() {

        return "ConstraintViolationDatatype{" +
            "propertyConstraint=" + propertyConstraint +
            ", resource=" + resource +
            ", message='" + message + '\'' +
            ", actualDatatype=" + actualDatatype +
            '}';
    }

    /**
     * Adding validation result triples specific to this kind of violations.
     * @return a list of statements containing validation results
     */
    @Override
    public List<Statement> validationResults() {
        List<Statement> statements = super.validationResults();

        if(actualDatatype != null){
            statements.add(factory.createStatement(validationResultsIri, SHACLExt.actual, actualDatatype));
        }
        statements.add(factory.createStatement(validationResultsIri, SHACLExt.expected, expectedDatatype));
        statements.add(factory.createStatement(validationResultsIri, RDF.TYPE, SHACLExt.ConstraintViolationDatatype));

        return statements;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ConstraintViolationDatatype) {

            ConstraintViolationDatatype datatypeObject = (ConstraintViolationDatatype) obj;

            return datatypeObject.actualDatatype.equals(actualDatatype) && datatypeObject.resource.equals(resource);

        } else {
            return false;
        }
    }
}
