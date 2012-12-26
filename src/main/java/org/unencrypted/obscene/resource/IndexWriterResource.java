package org.unencrypted.obscene.resource;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.codehaus.jackson.map.util.JSONWrappedObject;
import org.unencrypted.obscene.indexer.IndexService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/index/writer")
public class IndexWriterResource {

    @Context
    IndexService indexService;

    @POST
    @Consumes("application/json")
    public Response submit(String objString) throws InterruptedException {
        Field field = new TextField("body", objString, Field.Store.YES);
        Document doc = new Document();
        doc.add(field);
        indexService.getWriteQueue().put(doc);
        return Response.ok().build();
    }
}
