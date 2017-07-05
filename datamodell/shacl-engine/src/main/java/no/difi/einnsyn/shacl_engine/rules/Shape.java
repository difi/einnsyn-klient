package no.difi.einnsyn.shacl_engine.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.sail.memory.model.MemStatement;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;

/**
 * Created by havardottestad on 04/05/16.
 */
public class Shape {


    private Resource scopeClass;
    private final List<PropertyConstraint> properties = new ArrayList<>();
    private boolean strictMode;

    public Shape(Resource subject, RepositoryConnection shapesConnection, boolean strictMode) {
        this.strictMode = strictMode;

        try{
            scopeClass = (Resource) shapesConnection.getStatements(subject, SHACL.scopeClass, null).next().getObject();

        }catch (NoSuchElementException e){
            scopeClass = null;
        }

        RepositoryResult<Statement> statements = shapesConnection.getStatements(subject, SHACL.property, null);

        Iterations.stream(statements)
            .map(statement -> PropertyConstraint.Factory.create((Resource) statement.getObject(), shapesConnection, strictMode, this))
            .forEach(properties::add);

    }

    public Resource getScopeClass() {
        return scopeClass;
    }

    private class TempStatementsAndResource {
        Resource resource;
        List<Statement> list;

        TempStatementsAndResource(Resource resource, List<Statement> list) {
            this.resource = resource;
            this.list = list;
        }
    }

    /**
     * @param dataConnection             the given data graph
     * @param constraintViolationHandler an instance of the ConstraintViolationHandler
     */
    public void validate(RepositoryConnection dataConnection, ConstraintViolationHandler constraintViolationHandler) {

        RepositoryResult<Statement> statements = dataConnection.getStatements(null, RDF.TYPE, scopeClass, true);

        Iterations.stream(statements)
            .peek(
                statement -> {
                    if (strictMode && statement instanceof MemStatement) {
                        ((MemStatement) statement).setTillSnapshot(Integer.MAX_VALUE - 1);
                    }
                }
            )
            .map(statement ->
                new TempStatementsAndResource(
                    statement.getSubject(),
                    Iterations
                        .stream(dataConnection.getStatements(statement.getSubject(), null, null, true))
                        .collect(Collectors.toList())
                )
            )

            // validate every property in the properties (constraint list)
            .forEach(tempStatementsAndResource ->
                properties.stream()
                    .forEach(property -> property.validate(tempStatementsAndResource.resource,
                        tempStatementsAndResource.list, constraintViolationHandler, dataConnection)));
    }
}