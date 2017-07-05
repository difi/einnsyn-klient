package no.difi.einnsyn.shacl_engine.rules.propertyconstraints;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.Shape;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationClass;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;

/**
 * Checks that the o in s --p--> o is an instance of the class specified in the SHACL
 * constraint for the specified predicat (p)
 *
 * https://www.w3.org/TR/shacl/#AbstractClassPropertyConstraint
 *
 */
public class ClassConstraint extends MinMaxConstraint {

    private final IRI class_property;

    public ClassConstraint(Resource object, RepositoryConnection shapes, boolean strictMode, Shape shape) {
        super(object, shapes, strictMode, shape);

        this.class_property = SesameUtils.getExactlyOneIri(shapes, object, SHACL.class_property);
    }

    public IRI getClassProperty() {
        return this.class_property;
    }

    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {

        super.validate(resource, list, constraintViolationHandler, dataGraphConnection);

        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> (statement.getObject() instanceof Resource) &&
                !(dataGraphConnection.hasStatement((Resource) statement.getObject(), RDF.TYPE, class_property, true)))

            .forEach(statement -> {
                List<Value> collect = Iterations.stream(dataGraphConnection.getStatements(((Resource) statement.getObject()), RDF.TYPE, null, true)).map(statement1 -> statement1.getObject()).collect(Collectors.toList());
                constraintViolationHandler.handle(
                    new ConstraintViolationClass(this, resource, "Incorrect class type.", statement, collect)
                );
            });

        list.stream()
            .filter(statement -> statement.getPredicate().equals(predicate))
            .filter(statement -> !(statement.getObject() instanceof Resource))
            .forEach(statement -> constraintViolationHandler.handle(
                new ConstraintViolationClass(this, resource, "Object is a literal, expected IRI.", statement, Arrays.asList(statement.getObject())))
            );
    }

    @Override
    public String toString() {
        return "PropertyConstraint{" +
            ", predicate=" + predicate +
            ", minCount=" + minCount +
            ", maxCount=" + maxCount +
            '}';
    }
}
