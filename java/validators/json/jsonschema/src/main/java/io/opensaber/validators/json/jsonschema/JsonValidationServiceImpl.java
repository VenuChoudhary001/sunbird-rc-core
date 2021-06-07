package io.opensaber.validators.json.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opensaber.registry.middleware.MiddlewareHaltException;
import io.opensaber.validators.IValidate;
import org.apache.logging.log4j.util.Strings;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JsonValidationServiceImpl implements IValidate {
	private static Logger logger = LoggerFactory.getLogger(JsonValidationServiceImpl.class);

	private Map<String, Schema> entitySchemaMap = new HashMap<>();
	private Map<String, String> definitionMap = new HashMap<>();;
	private final String schemaUrl;

	public JsonValidationServiceImpl(String schemaUrl) {
		this.schemaUrl = schemaUrl;
	}

	private Schema getEntitySchema(String entityType) throws MiddlewareHaltException {

		if (entitySchemaMap.containsKey(entityType)) {
			return entitySchemaMap.get(entityType);
		} else {
			Schema schema;
			try {
				String definitionContent = definitionMap.get(entityType);
                JSONObject rawSchema = new JSONObject(definitionContent);

				SchemaLoader schemaLoader = SchemaLoader.builder().schemaJson(rawSchema).draftV7Support()
						.resolutionScope(schemaUrl).build();
				schema = schemaLoader.load().build();
				entitySchemaMap.put(entityType, schema);
			} catch (Exception ioe) {
			    ioe.printStackTrace();
				throw new MiddlewareHaltException("can't validate, "+ entityType + ": schema has a problem!");
			}
			return schema;
		}
	}

	@Override
	public boolean validate(String entityType, String objString) throws MiddlewareHaltException {
		boolean result = false;
		Schema schema = getEntitySchema(entityType);
		JSONObject obj = new JSONObject(objString);
		try {
			schema.validate(obj); // throws a ValidationException if this object is invalid
			result = true;
		} catch (ValidationException e) {
			logger.error(e.getMessage() + " : " + e.getErrorMessage());
			e.getCausingExceptions().stream()
					.map(ValidationException::getMessage)
					.forEach(logger::error);
            logger.error(e.toJSON().toString());
			throw new MiddlewareHaltException("Validation exception\n" + Strings.join(e.getCausingExceptions().stream()
					.map(ValidationException::getMessage).iterator(), '\n'));
		}
		return result;
	}
    /**
     * Store all list of known definitions as definitionMap.
     * Must get populated before creating the schema.
     * 
     * @param definitionTitle
     * @param definitionContent
     */
    @Override
    public void addDefinitions(String definitionTitle, String definitionContent) {
        definitionMap.put(definitionTitle, definitionContent);
    }


    public String getEntitySubject(String entityType, JsonNode entity) throws Exception {
    	String subjectJsonPath = new ObjectMapper()
				.readTree(definitionMap.get(entityType))
				.findPath("subjectJsonPath").textValue();
    	return entity.findPath(subjectJsonPath).textValue();
	}


}
