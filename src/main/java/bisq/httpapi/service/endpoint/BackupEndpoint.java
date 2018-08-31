package bisq.httpapi.service.endpoint;

import bisq.httpapi.exceptions.NotFoundException;
import bisq.httpapi.facade.BackupFacade;
import bisq.httpapi.model.BackupList;
import bisq.httpapi.model.CreatedBackup;
import bisq.httpapi.util.ResourceHelper;

import javax.inject.Inject;

import java.nio.file.FileAlreadyExistsException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;



import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import javax.validation.ValidationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;


@Api(value = "backups", authorizations = @Authorization(value = "accessToken"))
@Produces(MediaType.APPLICATION_JSON)
public class BackupEndpoint {

    private final BackupFacade backupFacade;

    @Inject
    public BackupEndpoint(BackupFacade backupFacade) {
        this.backupFacade = backupFacade;
    }

    @ApiOperation("List backups")
    @GET
    public BackupList getBackupList() {
        return new BackupList(backupFacade.getBackupList());
    }

    @ApiOperation("Create backup")
    @POST
    public CreatedBackup createBackup() throws IOException {
        return new CreatedBackup(backupFacade.createBackup());
    }

    @ApiOperation("Upload backup")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    @Path("/upload")
    public void uploadBackup(@FormDataParam("file") InputStream uploadedInputStream,
                             @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
        try {
            backupFacade.uploadBackup(fileDetail.getFileName(), uploadedInputStream);
        } catch (FileAlreadyExistsException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(value = "Get backup")
    @GET
    @Path("/{path}")
    public Response getBackup(@PathParam("path") String fileName) {
        try {
            return Response.ok(backupFacade.getBackup(fileName), MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (FileNotFoundException e) {
            return ResourceHelper.toValidationErrorResponse(e, 404).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @ApiOperation(value = "Restore backup")
    @POST
    @Path("/{path}/restore")
    public void restoreBackup(@PathParam("path") String fileName) throws IOException {
        try {
            backupFacade.requestBackupRestore(fileName);
        } catch (FileNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @ApiOperation(value = "Remove backup")
    @DELETE
    @Path("/{path}")
    public Response removeBackup(@PathParam("path") String fileName) {
        try {
            if (backupFacade.removeBackup(fileName))
                return Response.status(Response.Status.NO_CONTENT).build();
            else
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unable to remove file: " + fileName).build();
        } catch (FileNotFoundException e) {
            return ResourceHelper.toValidationErrorResponse(e, 404).build();
        }
    }

}
