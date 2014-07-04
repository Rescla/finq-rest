package nl.finan.jbehave.rest;

import nl.finan.jbehave.dao.RunningStoriesDao;
import nl.finan.jbehave.entities.RunningStories;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("status")
@Stateless
public class StatusResources {

    @EJB
    private RunningStoriesDao runningStoriesDao;

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public RunningStories getStatus(@PathParam("id") Long statusId){
        RunningStories runningStories = runningStoriesDao.find(statusId);
        return runningStories;
    }
}
