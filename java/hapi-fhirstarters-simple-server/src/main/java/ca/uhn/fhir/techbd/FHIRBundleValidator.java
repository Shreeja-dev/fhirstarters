package ca.uhn.fhir.techbd;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Validate;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.ValidationModeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.ValidationOptions;

@RestController
public class FHIRBundleValidator implements IResourceProvider {

    private static String folderPath;
    private static String profile;
    private static String zipFileName;
    static FhirContext context = FhirContext.forR4();

    /**
     * Constructor
     */
    public FHIRBundleValidator() {
        PropertiesConfiguration config = new PropertiesConfiguration();
        try {
            config.load("application.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        // Load values fom application.properties
        folderPath = config.getString("folderPath");
        profile = config.getString("profile");
        zipFileName = config.getString("zipFileName");
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Bundle.class;
    }

    @Create
    public MethodOutcome createBundle(@ResourceParam Bundle thebundle, @IdParam(optional = true) IIdType id,
            @OptionalParam(name = "x") String xValue) {

        // Accessing x parameter from URL
        if (xValue != null) {
            // Process x parameter if needed
            System.out.println("Value of x parameter: " + xValue);
        }
        // if (thebundle.getIdentifier().getValue().isEmpty()) {
        /*
         * It is also possible to pass an OperationOutcome resource
         * to the UnprocessableEntityException if you want to return
         * a custom populated OperationOutcome. Otherwise, a simple one
         * is created using the string supplied below.
         */
        // throw new UnprocessableEntityException(Msg.code(636) + "No identifier
        // supplied");
        // }
        // Pass the bundle to create CSV files
        // This method returns a MethodOutcome object
        MethodOutcome retVal = new MethodOutcome();
        retVal.setId(new IdType("Bundle", thebundle.getIdentifier().getValue(), "1"));

        // Can also add an OperationOutcome resource to return
        // This part is optional though:
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue().setDiagnostics("One minor issue detected");
        retVal.setOperationOutcome(outcome);
        return retVal;
    }

    // // @Validate
    // public MethodOutcome validateBundle(
    // @ResourceParam Bundle theBundle,
    // @Validate.Mode ValidationModeEnum theMode,
    // @Validate.Profile String theProfile) {
    // System.out.println(
    // "Inside the function validateBundle......................." +
    // theBundle.getIdentifier().getValue());
    // // Actually do our validation: The UnprocessableEntityException
    // // results in an HTTP 422, which is appropriate for business rule failure
    // System.out.println("Profile path :" + theProfile);
    // ////////////// if (theBundle.getIdentifier().isEmpty()) {
    // /*
    // * It is also possible to pass an OperationOutcome resource
    // * to the UnprocessableEntityException if you want to return
    // * a custom populated OperationOutcome. Otherwise, a simple one
    // * is created using the string supplied below.
    // */
    // /// System.out.println("INVALID BUNDLE.");
    // ///// throw new UnprocessableEntityException(Msg.code(639) + "No identifier
    // supplied");
    // /// }
    // //// System.out.println("VALID BUNDLE.");

    // // This method returns a MethodOutcome object
    // MethodOutcome retVal = new MethodOutcome();

    // // You may also add an OperationOutcome resource to return
    // /*
    // * // This part is optional though:
    // * OperationOutcome outcome = new OperationOutcome();
    // * outcome.addIssue().setSeverity(IssueSeverity.WARNING).
    // * setDiagnostics("One minor issue detected");
    // * retVal.setOperationOutcome(outcome);
    // */
    // return retVal;
    // }

    // @Validate
    // public MethodOutcome validateFhirResource(@ResourceParam String jsonBody) {

    // // Parse the JSON text into a FHIR resource
    // FhirContext ctx = FhirContext.forR4();
    // IParser parser = ctx.newJsonParser();
    // MethodOutcome outcome = null;
    // IBaseResource resource = null;
    // // Parse with (default) lenient error handler
    // parser.setParserErrorHandler(new LenientErrorHandler());

    // // Parse with strict error handler
    // parser.setParserErrorHandler(new StrictErrorHandler());
    // // try{
    // resource = parser.parseResource(jsonBody);
    // // } catch (DataFormatException e) {
    // // e.printStackTrace();
    // // return outcome;
    // // }

    // // Read and parse the StructureDefinition from a file
    // ValidationModeEnum mode = ValidationModeEnum.CREATE;
    // outcome = validateBundle((Bundle) resource, mode, profile);

    // // Handle the validation result
    // OperationOutcome operationOutcome = (OperationOutcome)
    // outcome.getOperationOutcome();
    // System.out.println("operationOutcome: " + operationOutcome);
    // return outcome;

    // }

    @Validate
    public MethodOutcome validateBundle(
            @ResourceParam Bundle theBundle,
            @Validate.Mode ValidationModeEnum theMode,
            @Validate.Profile String theProfile) {

        // Initialize the FHIR context and validator
        FhirValidator validator = context.newValidator();
        validator.setValidateAgainstStandardSchema(false);
        validator.setValidateAgainstStandardSchematron(false);

        // Add the profile URL to the validation options
        ValidationOptions validationOptions = new ValidationOptions();
        if (theProfile != null && !theProfile.isEmpty()) {
            validationOptions.addProfile(theProfile);
        }

        // Validate the resource against the profile
        ValidationResult result = validator.validateWithResult(theBundle, validationOptions);

        // Create a MethodOutcome object
        MethodOutcome retVal = new MethodOutcome();

        // Create an OperationOutcome based on the validation result
        OperationOutcome outcome = (OperationOutcome) result.toOperationOutcome();

        // Add the OperationOutcome to the MethodOutcome
        retVal.setOperationOutcome(outcome);

        // Optionally, check if the validation was successful and add custom logic
        if (!result.isSuccessful()) {
            outcome.addIssue()
                    .setSeverity(IssueSeverity.ERROR)
                    .setDiagnostics("Validation failed with errors.");
        } else {
            outcome.addIssue()
                    .setSeverity(IssueSeverity.INFORMATION)
                    .setDiagnostics("Validation passed successfully.");
        }

        return retVal;
    }


    

}
