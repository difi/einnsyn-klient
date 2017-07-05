package no.difi.einnsyn.shacl_engine.violations;


import org.openrdf.model.Statement;

/**
 * Created by havardottestad on 25/05/16.
 */
public interface StrictModeStatementHandler {

     void handle(Statement statement);

}
