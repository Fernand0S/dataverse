package edu.harvard.iq.dataverse.api;

import edu.harvard.iq.dataverse.DataFile;
import edu.harvard.iq.dataverse.FileMetadata;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("files")
public class ProvApi extends AbstractApiBean {

    @POST
    @Path("{id}/prov-json")
    @Consumes("application/json")
    public Response addProvJson(String body, @PathParam("id") Long idSupplied) {
        // TODO: Permissions.
        // TODO: Save PROV-JSON file to filesystem/S3. This format: https://www.w3.org/Submission/prov-json/
        StringReader rdr = new StringReader(body);
        try {
            JsonObject jsonObj = Json.createReader(rdr).readObject();
        } catch (JsonException ex) {
            return error(Response.Status.BAD_REQUEST, "A valid JSON object could not be found. PROV-JSON format is expected.");
        }
        JsonObjectBuilder response = Json.createObjectBuilder();
        /**
         * We are not yet validating the JSON received as PROV-JSON, but we
         * could some day.
         *
         * Section "2.3 Validating PROV-JSON Documents" of the spec says, "A
         * schema for PROV-JSON is provided, which defines all the valid
         * PROV-JSON constructs described in this document. The schema was
         * written using the schema language specified in [JSON-SCHEMA] (Version
         * 4). It can be used for the purpose of validating PROV-JSON documents.
         * A number of libraries for JSON schema validation are available at
         * json-schema.org/implementations.html." It links to
         * https://www.w3.org/Submission/prov-json/schema
         */
        response.add("message", "A valid JSON object was uploaded to the prov-json endpoint.");
        return ok(response);
    }

    @POST
    @Path("{id}/prov-freeform")
    @Consumes("application/json")
    public Response addProvFreeForm(String body, @PathParam("id") Long idSupplied) {
        // TODO: Permissions.
        // TODO: Save prov free form text to FileMetadata table.
        DataFile dataFile = fileSvc.find(idSupplied);
        if (dataFile == null) {
            return error(NOT_FOUND, "Could not file a file based on id " + idSupplied + ".");
        }
        StringReader rdr = new StringReader(body);
        JsonObject jsonObj = null;
        try {
            jsonObj = Json.createReader(rdr).readObject();
        } catch (JsonException ex) {
            return error(BAD_REQUEST, "A valid JSON object could not be found.");
        }
        String provFreeForm = jsonObj.getString("text");
        FileMetadata fileMetadata = dataFile.getFileMetadata();
        fileMetadata.setProvFreeForm(provFreeForm);
        DataFile savedDataFile = fileSvc.save(dataFile);
        JsonObjectBuilder response = Json.createObjectBuilder();
        response.add("message", "Free-form provenance data saved: " + savedDataFile.getFileMetadata().getProvFreeForm());
        return ok(response);
    }

}
