package no.difi.einnsyn.shacl_engine.rules.propertyconstraints;

import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;
import no.difi.einnsyn.shacl_engine.rules.Shape;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationExactCount;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationMaxCount;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationMinCount;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.memory.model.MemStatement;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Checks that any o in s --p--> o occurs the minimum or/and maximum number of
 * times specified in the SHACL constraint for the specified predicat (p)
 *
 * https://www.w3.org/TR/shacl/#AbstractCountPropertyConstraint
 */
public class MinMaxConstraint extends PropertyConstraint {

    protected final Optional<Integer> minCount;
    protected final Optional<Integer> maxCount;
    private long count;

    public MinMaxConstraint(Resource object, RepositoryConnection shapes, boolean strictMode, Shape shape) {
        super(object, shapes, strictMode, shape);

        this.minCount = SesameUtils.getOptionalOneInteger(shapes, object, SHACL.minCount);
        this.maxCount = SesameUtils.getOptionalOneInteger(shapes, object, SHACL.maxCount);
    }

    public Optional<Integer> getMinCount() {
        return this.minCount;
    }

    public Optional<Integer> getMaxCount() {
        return this.maxCount;
    }

    public long getCount() {
        return this.count;
    }

    public void validate(Resource resource, List<Statement> list, ConstraintViolationHandler constraintViolationHandler,
                         RepositoryConnection dataGraphConnection) {



        Stream<Statement> peek = list.stream()

            .filter(statement -> statement.getPredicate().equals(predicate))
            .peek(statement -> {
                if (strictMode && statement instanceof MemStatement) {
                    ((MemStatement) statement).setTillSnapshot(Integer.MAX_VALUE - 1);
                }
            });

        count = peek
            .count();

        if(maxCount.isPresent() && minCount.isPresent() && maxCount.equals(minCount))  {
            if(maxCount.get() < count || minCount.get() > count) {
                constraintViolationHandler.handle(new ConstraintViolationExactCount(this, resource, "was " + count));
            }
        } else {

        if (maxCount.isPresent()) {
            if (maxCount.get() < count) {
                constraintViolationHandler.handle(new ConstraintViolationMaxCount(this, resource, "was " + count));
            }
        }

        if (minCount.isPresent()) {
            if (minCount.get() > count) {
                constraintViolationHandler.handle(new ConstraintViolationMinCount(this, resource, "was " + count));
            }
        }
    } }

    @Override
    public String toString() {
        return "PropertyConstraint {" +
            ", predicate=" + predicate +
            ", minCount=" + minCount +
            ", maxCount=" + maxCount +
            '}';
    }
}
