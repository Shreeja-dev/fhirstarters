package ca.uhn.fhir.techbd;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.SingleValidationMessage;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;

import org.hl7.fhir.common.hapi.validation.support.PrePopulatedValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;

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
        DefaultProfileValidationSupport validationSupport = new DefaultProfileValidationSupport(ctx);
        //validationSupport.loadFromPackageUrl(igPackageUrl);
        validationSupport.fetchStructureDefinition(igPackageUrl);
        

        // Add IG to the validation support chain
        ValidationSupportChain validationSupportChain = new ValidationSupportChain(validationSupport, new PrePopulatedValidationSupport(ctx));
        instanceValidator.setValidationSupport(validationSupportChain);

        // Add the instance validator to the validator module list
        validator.registerValidatorModule(instanceValidator);

        // JSON string representing the Bundle
        String bundleJson = " {\n" + //
                        "          \"resourceType\": \"Bundle\",\n" + //
                        "          \"id\": \"example-bundle\",\n" + //
                        "          \"type\": \"transaction\",\n" + //
                        "          \"entry\": [\n" + //
                        "            {\n" + //
                        "              \"fullUrl\": \"urn:uuid:1\",\n" + //
                        "              \"resource\": {\n" + //
                        "                \"resourceType\": \"Patient\",\n" + //
                        "                \"id\": \"example\",\n" + //
                        "                \"name\": [\n" + //
                        "                  {\n" + //
                        "                    \"family\": \"Doe\",\n" + //
                        "                    \"given\": [\"John\"]\n" + //
                        "                  }\n" + //
                        "                ]\n" + //
                        "              },\n" + //
                        "              \"request\": {\n" + //
                        "                \"method\": \"POST\",\n" + //
                        "                \"url\": \"Patient\"\n" + //
                        "              }\n" + //
                        "            },\n" + //
                        "            {\n" + //
                        "              \"fullUrl\": \"urn:uuid:2\",\n" + //
                        "              \"resource\": {\n" + //
                        "                \"resourceType\": \"Observation\",\n" + //
                        "                \"id\": \"example-observation\",\n" + //
                        "                \"status\": \"final\",\n" + //
                        "                \"code\": {\n" + //
                        "                  \"coding\": [\n" + //
                        "                    {\n" + //
                        "                      \"system\": \"http://loinc.org\",\n" + //
                        "                      \"code\": \"789-8\",\n" + //
                        "                      \"display\": \"Erythrocytes [#/volume] in Blood by Automated count\"\n" + //
                        "                    }\n" + //
                        "                  ]\n" + //
                        "                },\n" + //
                        "                \"subject\": {\n" + //
                        "                  \"reference\": \"Patient/example\"\n" + //
                        "                }\n" + //
                        "              },\n" + //
                        "              \"request\": {\n" + //
                        "                \"method\": \"POST\",\n" + //
                        "                \"url\": \"Observation\"\n" + //
                        "              }\n" + //
                        "            }\n" + //
                        "          ]\n" + //
                        "        }";

        // Parse the Bundle JSON string
        Bundle bundle = ctx.newJsonParser().parseResource(Bundle.class, bundleJson);

        // Validate the Bundle resource
        ValidationResult result = validator.validateWithResult(bundle);

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