package no.difi.einnsyn.shacl_engine.validation;

import info.aduna.iteration.Iterations;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.Shape;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationHandler;
import no.difi.einnsyn.shacl_engine.violations.StrictModeStatementHandler;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.memory.model.MemStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by veronika on 4/28/16.
 * Modified by havardottestad.
 * <p>
 * This class is the one that gets it all going. The constructor takes in a repository of SHACL shapes and a
 * repository containing the ontology which we will apply on our data graph before validating against the SHACL shapes.
 */
public class SHACLValidator {

    private boolean strictMode;
    List<Shape> shapes;
    private Repository ontology;

    private static Logger logger = LoggerFactory.getLogger(SHACLValidator.class);


    public SHACLValidator(Repository shaclRules, Repository ontology, boolean strictMode) {

        this.strictMode = strictMode;
        if (shaclRules == null) {
            return;
        }

        try (RepositoryConnection shapesConnection = shaclRules.getConnection()) {
            RepositoryResult<Statement> statements = shapesConnection.getStatements(null, RDF.TYPE, SHACL.Shape);

            shapes = QueryResults
                .stream(statements)
                .parallel()
                .map(statement -> new Shape(statement.getSubject(), shapesConnection, strictMode))
                .collect(Collectors.toList());
        }

        this.ontology = ontology;
    }

    public SHACLValidator(Repository shaclRules, Repository ontology) {
        if (shaclRules == null) {
            return;
        }

        try (RepositoryConnection shapesConnection = shaclRules.getConnection()) {
            RepositoryResult<Statement> statements = shapesConnection.getStatements(null, RDF.TYPE, SHACL.Shape);

            shapes = QueryResults.stream(statements)
                .map(statement -> new Shape(statement.getSubject(), shapesConnection, false))
                .collect(Collectors.toList());
        }

        this.ontology = ontology;

    }

    /**
     * Validate a given data graph against an ontology using Jena. The data graph is then validated against
     * a list of SHACL shapes to ensure that the data "fits" certain shapes (rules).
     * <p>
     * We are using Jena for inference, since Sesame will use ten times the time Jena uses to complete the task.
     * That was such an big difference between these two frameworks, so even that we're using Sesame in this project,
     * Jena will have to do with the inferencing job.
     *
     * @param data                       the given data graph
     * @param constraintViolationHandler an instance of the ConstraintViolationHandler
     * @return true if no constraint violations is found
     */
    public boolean validate(Repository data, ConstraintViolationHandler constraintViolationHandler) {
        return validate(data, constraintViolationHandler, statement -> {
        });
    }


    public boolean validate(Repository data, ConstraintViolationHandler constraintViolationHandler, StrictModeStatementHandler strictModeStatementHandler) {

        if (shapes == null) {
            throw new NullPointerException("Shapes repository was null");
        }

        if (data == null) {
            throw new NullPointerException("Data repository was null");
        }

        Repository originalData = data;

        if (ontology != null) {
            logger.info("Inferencing");
            data = addInferencing(data, ontology);
            //data = addInferencingUsingJena(data, ontology);
        }

        final boolean[] failed = {false};

        try (RepositoryConnection dataConnection = data.getConnection()) {
            logger.info("Validating");

            shapes.stream()
                .forEach(shape -> shape.validate(dataConnection, (violation) -> {
                    if (violation.getSeverity().equals(SHACL.Violation)) {
                        failed[0] = true;
                    }
                    constraintViolationHandler.handle(violation);
                }));


            if (strictMode) {
                logger.info("Handle strict mode");
                try (RepositoryConnection originalDataConnection = originalData.getConnection();) {

                    /* Extract all triples that have not been validated
                    * these are triples where there is no rule that matches
                    * possible due to a misspelled predicate, or more malicious intent like introducing a new status
                    *
                    * The general approach is that there are two sets of data
                    *   originalDataConnection contains the data that should have been validated
                    *   dataConnection contains the data that was validated
                    *
                    * In dataConnection, the data that was acutally validated has been marked by setting the tillSnapshot to Integer.MAX_VALUE -1
                     *
                     * To find the triples that did not get checked by a SHACL rule we have to take every triple in originalDataConnection
                     * and check that it either does not exist in dataConnection, or that it exists as a statement in dataConnection with tillSnapshot == Integer.MAX_VALUE
                    */


                    // start by iterating over all the triples in the incoming data
                    Iterations.stream(dataConnection.getStatements(null, null, null, false))

//
//                        // get hold of the validated triple that matches the one from the incoming data
//                        .map(statement -> {
//                            RepositoryResult<Statement> matchingStatementFromValidatedRepository = dataConnection.getStatements(statement.getSubject(), statement.getPredicate(), statement.getObject());
//                            boolean statementExistsInValidatedData = matchingStatementFromValidatedRepository.hasNext();
//
//                            // return the validated triple, or if it does not exist, then the original triple
//                            return statementExistsInValidatedData ? matchingStatementFromValidatedRepository.next() : statement;
//                        })

                        //.filter(statement -> statement instanceof MemStatement) commented out  because crashes are preferable to someone accidentally using strict mode without an in memory repository

                        // cast to MemStatement
                        .map(statement -> ((MemStatement) statement))

                        // Abuse till snapshot for marking which triples have been validated.
                        // The validation methods run previously will have set tillSnapshot == Integer.MAX_VALUE -1
                        .filter(memStatement -> memStatement.getTillSnapshot() == Integer.MAX_VALUE)

                        // side effect to mark the validation as failed
                        .peek(memStatement -> failed[0] = true)

                        // notify the statement handler of the statements that caused the validation to fail
                        .forEach(strictModeStatementHandler::handle);

                }
            }

            return !failed[0];
        }
    }

    private static Repository addInferencingUsingJena(Repository data, Repository ontology) {


        Model dataJena = ModelFactory.createDefaultModel();
        Model ontologyJena = ModelFactory.createDefaultModel();

        dataJena.read(new ByteArrayInputStream(SesameUtils.repositoryToString(data, RDFFormat.NTRIPLES).getBytes()), "", "NTRIPLES");
        ontologyJena.read(new ByteArrayInputStream(SesameUtils.repositoryToString(ontology, RDFFormat.NTRIPLES).getBytes()), "", "NTRIPLES");

        Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();

        InfModel infModel = ModelFactory.createInfModel(reasoner, ontologyJena, dataJena);


        Model inferenced = ModelFactory.createDefaultModel();

        StmtIterator stmtIterator = infModel.listStatements();
        while (stmtIterator.hasNext()) {
            inferenced.add(stmtIterator.nextStatement());
        }

        StringWriter stringWriter = new StringWriter();
        inferenced.write(stringWriter, "NTRIPLES");


        Repository repository = SesameUtils.stringToRepository(stringWriter.toString(), RDFFormat.NTRIPLES);

        return repository;

    }

    private static Repository addInferencing(Repository data, Repository ontology) {
        MemoryStore baseSail = new MemoryStore();
        Repository inferencedRepository = new SailRepository(new ForwardChainingRDFSInferencer(baseSail));
        inferencedRepository.initialize();


        try (RepositoryConnection inferencedConnection = inferencedRepository.getConnection()) {
            inferencedConnection.begin(IsolationLevels.READ_UNCOMMITTED);

            try (RepositoryConnection dataConnection = data.getConnection()) {
                dataConnection.begin(IsolationLevels.READ_UNCOMMITTED);
                inferencedConnection.add(dataConnection.getStatements(null, null, null));
                dataConnection.commit();
            }

            try (RepositoryConnection ontologyConnection = ontology.getConnection()) {
                ontologyConnection.begin(IsolationLevels.READ_UNCOMMITTED);
                inferencedConnection.add(ontologyConnection.getStatements(null, null, null));
                ontologyConnection.commit();


            }

            inferencedConnection.commit();

            inferencedConnection.begin(IsolationLevels.READ_UNCOMMITTED);
            try (RepositoryConnection ontologyConnection = ontology.getConnection()) {

                ontologyConnection.begin(IsolationLevels.READ_UNCOMMITTED);
                RepositoryResult<Statement> statements = ontologyConnection.getStatements(null, null, null);
                while (statements.hasNext()) {
                    Statement next = statements.next();
                    (((MemStatement) inferencedConnection.getStatements(next.getSubject(), next.getPredicate(), next.getObject()).next())).setTillSnapshot(Integer.MAX_VALUE - 1);
                }
                ontologyConnection.commit();

            }

            inferencedConnection.commit();
        }


        return inferencedRepository;
    }


}
