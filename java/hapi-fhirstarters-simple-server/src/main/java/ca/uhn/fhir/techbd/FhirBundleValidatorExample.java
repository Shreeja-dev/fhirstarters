package ca.uhn.fhir.techbd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.SingleValidationMessage;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.parser.StrictErrorHandler;

import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import ca.uhn.fhir.parser.IParser;

// import org.hl7.fhir.r4.hapi.validation.FhirInstanceValidator;
// import ca.uhn.fhir.validation.support.ValidationSupportChain;
// import ca.uhn.fhir.validation.support.PrePopulatedValidationSupport;
// import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;

import java.util.List;

public class FhirBundleValidatorExample {
    public static void main(String[] args) {
        // Create a FHIR context for R4
        FhirContext ctx = FhirContext.forR4();

        // Create a FHIR validator
        FhirValidator validator = ctx.newValidator();

        // Create a FHIR instance validator
        FhirInstanceValidator instanceValidator = new FhirInstanceValidator(ctx);

        // Load the Implementation Guide (IG) to use in validation
        String igPackageUrl = "https://djq7jdt8kb490.cloudfront.net/1115/StructureDefinition-SHINNYBundleProfile.json";
        // https://djq7jdt8kb490.cloudfront.net/1115/StructureDefinition-shinny-patient.json

        // String igPackageLocalUrl =
        // "/home/vinod/Desktop/SOURCECODE/IMPORTANT_OLD/fhir-server-prime/src/main/resources/fhir-test/SHINNYBundleProfile.json";
        DefaultProfileValidationSupport validationSupport = new DefaultProfileValidationSupport(ctx);
        // validationSupport.loadFromPackageUrl(igPackageUrl);
        validationSupport.fetchStructureDefinition(igPackageUrl);

        // Add IG to the validation support chain
        ValidationSupportChain validationSupportChain = new ValidationSupportChain(validationSupport,
                new PrePopulatedValidationSupport(ctx));
        instanceValidator.setValidationSupport(validationSupportChain);

        // Add the instance validator to the validator module list
        validator.registerValidatorModule(instanceValidator);

        String bundleJsonFile = "";
        try {
            bundleJsonFile = new String(Files.readAllBytes(Paths.get(
                    "/home/shreeja/workspaces/github.com/Shreeja-dev/fhirstarters/java/hapi-fhirstarters-simple-server/src/main/resources/ca/uhn/fhir/AHCHRSNQuestionnaireResponseExample.json")));
        } catch (IOException e) {
            System.err.println("Failed to read JSON input from file: " + e.getMessage());
            return; // Exit the method if the file read fails
        }

        // Parse the Bundle JSON string
        /// Bundle bundle = ctx.newJsonParser().parseResource(Bundle.class,
        // bundleJsonFile);

        // Validate the Bundle resource
        //// ValidationResult result = validator.validateWithResult(bundle);

        final IParser strictParser = ctx.newJsonParser();
        strictParser.setParserErrorHandler(new StrictErrorHandler());
        final IBaseResource parsedResource = strictParser.parseResource(bundleJsonFile);

        ValidationResult result = validator.validateWithResult(parsedResource);

        // Parse and print the validation results
        if (result.isSuccessful()) {
            System.out.println("Validation passed.");
        } else {
            System.out.println("Validation failed with issues:");
            List<SingleValidationMessage> messages = result.getMessages();
            for (SingleValidationMessage next : messages) {
                System.out.println("Severity: " + next.getSeverity());
                System.out.println("Location: " + next.getLocationString());
                System.out.println("Message : " + next.getMessage());
                System.out.println();
            }
        }
    }
}