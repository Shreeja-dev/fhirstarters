package ca.uhn.fhir.techbd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.StructureDefinition; // Ensure this import is present

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.text.MessageFormat;

import java.nio.file.Files;
import java.nio.file.Paths;

public class SchemaValidationR4App {
    private static final String IG_PROFILE_URL = "https://djq7jdt8kb490.cloudfront.net/1115/StructureDefinition-SHINNYBundleProfile.json";
    private static FhirContext ourCtx = FhirContext.forR4();

    public static void main(String[] args) {
        try {
            before();
            testJsonValidation();
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            afterClassClearContext();
        }
    }

    private static void before() {
        System.out.println(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
        System.out.println(getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
        System.out.println(getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
        System.out.println(getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));
    }

    private static void testJsonValidation() {
        String input = "";
    
        // Read JSON input from a file
        try {
            input = new String(Files.readAllBytes(Paths.get("/home/shreeja/workspaces/github.com/Shreeja-dev/fhirstarters/java/hapi-fhirstarters-simple-server/src/main/resources/ca/uhn/fhir/TestCase301.json")));
        } catch (IOException e) {
            System.err.println("Failed to read JSON input from file: " + e.getMessage());
            return; // Exit the method if the file read fails
        }
    
        FhirValidator val = ourCtx.newValidator();
        val.setValidateAgainstStandardSchema(true);
        val.setValidateAgainstStandardSchematron(false);
    
        // Load the StructureDefinition from the IG profile URL
        try {
            URL url = new URL(IG_PROFILE_URL);
            IParser parser = ourCtx.newJsonParser();
            StructureDefinition profile = parser.parseResource(StructureDefinition.class, url.openStream());
    
            // Create ValidationOptions and set the profile URL
            ValidationOptions options = new ValidationOptions();
            options.addProfile(profile.getUrl());  // Add the profile URL to validation options
            
            
            //val.loadFromPackageUrl(profile.getUrl());
    
            // Parse the input JSON into a Bundle resource
            Bundle bundle = ourCtx.newJsonParser().parseResource(Bundle.class, input);
    
            // Validate the Bundle resource against the profile
            ValidationResult result = val.validateWithResult(bundle, options);
    
            String encoded = ourCtx.newJsonParser().setPrettyPrint(true).encodeResourceToString(result.toOperationOutcome());
            System.out.println(encoded);
        } catch (IOException e) {
            System.err.println("Failed to load Implementation Guide profile: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Validation error: " + e.getMessage());
        }
    }
    

    private static String getJaxpImplementationInfo(String componentName, Class<?> componentClass) {
        CodeSource source = componentClass.getProtectionDomain().getCodeSource();
        return MessageFormat.format(
            "{0} implementation: {1} loaded from: {2}",
            componentName,
            componentClass.getName(),
            source == null ? "Java Runtime" : source.getLocation());
    }

    private static void afterClassClearContext() {
        // Your logic to clean up the context if needed
    }
}
