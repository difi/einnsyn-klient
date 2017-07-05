package no.difi.einnsyn.shacl_engine.rules;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.ClassConstraint;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.ClassInConstraint;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.DatatypeConstraint;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.MinMaxConstraint;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;

import java.util.List;

/**
 * Created by havardottestad on 04/05/16.
 * Modified by veronika.
 *
 */
public abstract class PropertyConstraint {

    protected final boolean strictMode;
    protected final IRI predicate;
    IRI severity = SHACL.Violation;
    Resource id;
    Shape shape;

    protected PropertyConstraint(Resource subject, RepositoryConnection shapesConnection, boolean strictMode, Shape shape) {
        this.strictMode = strictMode;
        predicate = SesameUtils.getExactlyOneIri(shapesConnection, subject, SHACL.predicate);
        if(shapesConnection.hasStatement(subject, SHACL.severity, null, true)){
            this.severity = SesameUtils.getExactlyOneIri(shapesConnection, subject, SHACL.severity);
        }
        id = subject;
        this.shape = shape;
    }

    protected abstract void validate(Resource resource, List<Statement> list,
                                     ConstraintViolationHandler constraintViolationHandler,
                                     RepositoryConnection dataGraphConnection);

    public IRI getPredicate() {
        return this.predicate;
    }

    public IRI getSeverity() {
        return this.severity;
    }

    public Resource getId() {
        return id;
    }

    public Shape getShape() {
        return shape;
    }

    static class Factory {

        static PropertyConstraint create(Resource object, RepositoryConnection shapesConnection, boolean strictMode, Shape shape) {

            if (shapesConnection.hasStatement(object, SHACL.class_property, null, true)) {
                return new ClassConstraint(object, shapesConnection, strictMode, shape);
            }

            if (shapesConnection.hasStatement(object, SHACL.datatype, null, true)) {
                return new DatatypeConstraint(object, shapesConnection, strictMode, shape);
            }

            if (shapesConnection.hasStatement(object, SHACL.classIn, null, true)) {
                return new ClassInConstraint(object, shapesConnection, strictMode, shape);
            }

            if (shapesConnection.hasStatement(object, SHACL.minCount, null, true) ||
                shapesConnection.hasStatement(object, SHACL.maxCount, null, true)) {

                return new MinMaxConstraint(object, shapesConnection, strictMode, shape);
            }

            // Throw exception for unhandled contraints.
            RepositoryResult<Statement> statements = shapesConnection.getStatements(object, null, null);
            Model model = QueryResults.asModel(statements);

            String shaclRuleAsTurtle = SesameUtils.modelToString(model, RDFFormat.TURTLE);

            throw new UnsupportedOperationException("Property constraint not implemented. \n" + shaclRuleAsTurtle);
        }

    }
}
