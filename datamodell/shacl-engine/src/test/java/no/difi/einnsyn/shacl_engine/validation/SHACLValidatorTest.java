package no.difi.einnsyn.shacl_engine.validation;

import com.github.jsonldjava.core.JsonLdError;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import no.difi.einnsyn.Arkiv;
import no.difi.einnsyn.SHACL;
import no.difi.einnsyn.SHACLExt;
import no.difi.einnsyn.sesameutils.SesameUtils;
import no.difi.einnsyn.shacl_engine.rules.PropertyConstraint;
import no.difi.einnsyn.shacl_engine.rules.Shape;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.ClassConstraint;
import no.difi.einnsyn.shacl_engine.rules.propertyconstraints.DatatypeConstraint;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolation;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationClass;
import no.difi.einnsyn.shacl_engine.violations.ConstraintViolationDatatype;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by havardottestad on 04/05/16.
 *
 *
 */
public class SHACLValidatorTest {

    @Test(expected = NullPointerException.class)
    public void validateNull() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator(null, null);
        assertFalse(shaclValidator.validate(null, null));
    }

    @Test
    public void validateEmptyRule() throws Exception {

        SHACLValidator shaclValidator = new SHACLValidator(SesameUtils.stringToRepository("", RDFFormat.TURTLE), null);

        assertTrue("Empty rules set and empty data should validate", shaclValidator.validate(
            SesameUtils.stringToRepository("", RDFFormat.TURTLE),
            violation -> {
            }
        ));
    }

    @Test
    public void strictModeTest() throws IOException {
        String dir = "strictModeTestData/strictMode";

        SHACLValidator shaclValidator = new SHACLValidator(getShacl(dir), null, true);

        assertFalse(shaclValidator.validate( getData(dir+"/fail"),   violation -> {}, statement -> {

            System.out.println(statement);
        }));


    }


    @Test
    public void strictModeTest2() throws IOException {
        String dir = "strictModeTestData/strictMode";

        SHACLValidator shaclValidator = new SHACLValidator(getShacl(dir), getOntology(dir+"/pass"), true);

        assertTrue(shaclValidator.validate( getData(dir+"/pass"),   violation -> {}, statement -> {

            System.out.println(statement);
        }));


    }

    @Test
    public void exactCountTest() throws IOException {
        String dir = "testData/exactCount";

        SHACLValidator shaclValidator = new SHACLValidator(getShacl(dir), null);

        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            violation -> {
                System.out.println(violation);

                try {
                    JsonElement jsonElement = violation.toJson();
                    JsonObject asJsonObject = jsonElement.getAsJsonObject();

                    assertEquals("", 1, asJsonObject.get("expected").getAsJsonObject().get("value").getAsInt());
                    assertEquals("", 2, asJsonObject.get("actual").getAsJsonObject().get("value").getAsInt());
                    assertEquals("", "sh-ext:ConstraintViolationExactCount", asJsonObject.get("type").getAsString());

                } catch (JsonLdError jsonLdError) {
                    assertTrue(jsonLdError.getMessage(), false);
                }
            }
        ));

    }

    @Test
    public void minCountTest() throws Exception {
        String dir = "testData/minCount1";

        SHACLValidator shaclValidator = new SHACLValidator(getShacl(dir), null);

        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            violation -> {
                System.out.println(violation);

                try {
                    JsonElement jsonElement = violation.toJson();
                    JsonObject asJsonObject = jsonElement.getAsJsonObject();

                    assertEquals("", 1, asJsonObject.get("expected").getAsJsonObject().get("value").getAsInt());
                    assertEquals("", 0, asJsonObject.get("actual").getAsJsonObject().get("value").getAsInt());

                } catch (JsonLdError jsonLdError) {
                    assertTrue(jsonLdError.getMessage(), false);
                }
            }
        ));
    }


    @Test
    public void simpleShaclClassViolation2() throws Exception {
        String dir = "testData/simpleClass";

        SHACLValidator shaclValidator = new SHACLValidator(getShacl(dir), null);
        Shape shape = shaclValidator.shapes.get(0);

        List<ConstraintViolation> violations = new ArrayList<>();

        assertFalse(shaclValidator.validate(
            getData(dir+"/fail2"),
            violation -> {
                violations.add(violation);
                System.out.println(violation);

                JsonElement jsonElement = null;
                try {
                    jsonElement = violation.toJson();
                } catch (JsonLdError jsonLdError) {
                    assertTrue(jsonLdError.getMessage(), false);
                }

                JsonObject asJsonObject = jsonElement.getAsJsonObject();

                assertEquals("", "utfort", asJsonObject.get("object").getAsString());
                assertEquals("", Arkiv.Journalpoststatus.toString(), asJsonObject.get("expected").getAsString());

                String message = asJsonObject.get("message").getAsString();
                assertEquals("", "Object is a literal, expected IRI.", message);

                System.out.println(jsonElement);
                assertEquals("",
                    "\"type\":\"sh-ext:ConstraintViolationClass\",\"actual\":\"utfort\",\"expected\":\"http://www.arkivverket.no/standarder/noark5/arkivstruktur/Journalpoststatus\",\"focusNode\":\"http://example.org/1\",\"message\":\"Object is a literal, expected IRI.\",\"object\":\"utfort\",\"predicate\":\"arkiv:journalpoststatus\",\"scopeClass\":\"arkiv:Journalpost\",\"severity\":\"sh:Violation\",\"sourceConstraint\":\"_:b0\",\"subject\":\"http://example.org/1\"}".replaceAll("\\{\"id\":(.*?),",""),
                    jsonElement.toString().replaceAll("\\{\"id\":(.*?),","")
                );

                System.out.println(jsonElement);
            }
        ));

        SimpleValueFactory instance = SimpleValueFactory.getInstance();

        List<PropertyConstraint> properties = (List<PropertyConstraint>) ReflectionTestUtils.getField(shape, "properties");

        List<ConstraintViolation> expectedViolations = new ArrayList<>();
        expectedViolations.add(new ConstraintViolationClass(((ClassConstraint) properties.get(0)), instance.createIRI("http://example.org/1"), "Object is a literal, expected IRI.", null, null));

        assertEquals("", expectedViolations.size(), violations.size());
        assertTrue("", violations.containsAll(expectedViolations));
        assertTrue("", expectedViolations.containsAll(violations));
    }

    @Test
    public void simpleShaclDatetimeInDateViolation() throws Exception {
        String dir = "testData/datetimeIndate";

        SHACLValidator shaclValidator = new SHACLValidator(getShacl(dir), null);

        List<ConstraintViolation> violations = new ArrayList<>();

        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            System.out::println
        ));

        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            violation -> {
                violations.add(violation);
                System.out.println(violation);

                JsonElement jsonElement = null;
                try {
                    jsonElement = violation.toJson();
                } catch (JsonLdError jsonLdError) {
                    assertTrue(jsonLdError.getMessage(), false);
                }

                JsonObject asJsonObject = jsonElement.getAsJsonObject();

                String message = asJsonObject.get("message").getAsString();
                assertEquals("", "2014-11-21T15:13:35.749+01:00 could not be parsed as xsd:date", message);


                System.out.println(jsonElement);
            }
        ));
    }


    @Test
    public void severityWarningOnDatatypeTest() throws Exception {
        String dir = "testData/severity";

        SHACLValidator shaclValidator = new SHACLValidator(getShacl(dir), null);

        List<ConstraintViolation> violations = new ArrayList<>();

        assertTrue(shaclValidator.validate(
            getData(dir+"/pass"),
            System.out::println
        ));

        assertTrue(shaclValidator.validate(
            getData(dir+"/pass"),
            violation -> {
                violations.add(violation);
                System.out.println(violation);

                JsonElement jsonElement = null;
                try {
                    jsonElement = violation.toJson();
                    JsonObject asJsonObject = jsonElement.getAsJsonObject();

                    String message = asJsonObject.get("severity").getAsString();
                    assertEquals("", "sh:Warning", message);

                    System.out.println(jsonElement);
                } catch (JsonLdError jsonLdError) {
                    assertTrue(jsonLdError.getMessage(), false);
                }

            }
        ));
    }

    @Test
    public void simpleShaclMinDatatypeViolation() throws Exception {
        String dir = "testData/datatypeStringVsLangString";

        SHACLValidator shaclValidator = new SHACLValidator(getShacl(dir), null);

        List<ConstraintViolation> violations = new ArrayList<>();

        assertFalse(shaclValidator.validate(
            getData(dir+"/fail"),
            violation -> {
                violations.add(violation);
                System.out.println(violation);
            }
        ));

        SimpleValueFactory instance = SimpleValueFactory.getInstance();

        Shape shape = shaclValidator.shapes.get(0);

        List<PropertyConstraint> properties = (List<PropertyConstraint>) ReflectionTestUtils.getField(shape, "properties");

        List<ConstraintViolation> expectedViolations = new ArrayList<>();
        expectedViolations.add(new ConstraintViolationDatatype(((DatatypeConstraint) properties.get(0)), instance.createIRI("http://example.org/1"), null, null, RDF.LANGSTRING));

        assertTrue("", violations.containsAll(expectedViolations));
        assertTrue("", expectedViolations.containsAll(violations));

        ConstraintViolation constraintViolation = violations.get(0);
        List<Statement> statements = constraintViolation.validationResults();
        assertEquals("Every violation should have exactly 1 focusNode", statements.stream().filter(s -> s.getPredicate().equals(SHACL.focusNode)).count(), 1);
        assertEquals("Every violation should have exactly 1 actual", statements.stream()
            .filter(s -> s.getPredicate().equals(SHACLExt.actual))
            .filter(s -> s.getObject().equals(RDF.LANGSTRING))
            .count(), 1);
        assertEquals("Every violation should have exactly 1 expected", statements.stream()
            .filter(s -> s.getPredicate().equals(SHACLExt.expected))
            .filter(s -> s.getObject().equals(XMLSchema.STRING))
            .count(), 1);

        JsonElement jsonElement = constraintViolation.toJson();

        System.out.println(jsonElement);
    }


    @Test
    public void testAllViolationsAtOnce() throws Exception {
        String dir = "testData/allViolations";

        SHACLValidator shaclValidator = new SHACLValidator(getShacl(dir), getOntology(dir+"/fail"));


        List<ConstraintViolation> violations = new ArrayList<>();

        assertFalse(shaclValidator.validate(
            getData(dir + "/fail"),
            violation -> {
                violations.add(violation);
                try {
                    violation.toJson();
                } catch (JsonLdError jsonLdError) {
                    throw new RuntimeException(jsonLdError.toString());
                }
                System.out.println(violation);
            }
        ));

        assertEquals("All SHACL rules should fail, including failures for minCount and maxCount inside datatype and class rules", 11, violations.size());
    }

    private Repository getData(String simpleShaclViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(simpleShaclViolation + "/data.ttl");

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }

    private Repository getShacl(String simpleShaclViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(simpleShaclViolation + "/shacl.ttl");

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }

    private Repository getOntology(String simpleShaclViolation) throws IOException {
        InputStream resourceAsStream = SHACLValidatorTest.class.getClassLoader().getResourceAsStream(simpleShaclViolation + "/ontology.ttl");

        return SesameUtils.streamToRepository(resourceAsStream, RDFFormat.TURTLE);
    }
}