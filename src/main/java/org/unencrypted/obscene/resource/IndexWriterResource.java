package org.unencrypted.obscene.resource;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.unencrypted.obscene.indexer.IndexService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(IndexWriterResource.path)
public class IndexWriterResource {
    public static final String path = "/index/writer";

    @Context
    IndexService indexService;

    /**
     * Receives a post of 'text/plain' and indexes it as the document's only field
     * @param objString
     * @return
     * @throws InterruptedException
     */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response submit(String objString) throws InterruptedException {
        Field field = new TextField("body", objString, Field.Store.YES);
        Document doc = new Document();
        doc.add(field);
        indexService.getWriteQueue().put(doc);
        return Response.ok().build();
    }
}
