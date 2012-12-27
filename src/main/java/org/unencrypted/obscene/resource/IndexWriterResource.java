package org.unencrypted.obscene.resource;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.unencrypted.obscene.indexer.IndexService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path(IndexWriterResource.path)
public class IndexWriterResource {
    public static final String path = "/index/writer";

    @Context
    IndexService indexService;

    @POST
    @Consumes("application/text")
    public Response submit(String objString) throws InterruptedException {
        Field field = new TextField("body", objString, Field.Store.YES);
        Document doc = new Document();
        doc.add(field);
        indexService.getWriteQueue().put(doc);
        return Response.ok().build();
    }
}
