package org.unencrypted.obscene.resource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.unencrypted.obscene.indexer.IndexService;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path(IndexReaderResource.path)
public class IndexReaderResource {
    public static final String path = "index/reader";
    private final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);

    @Context
    IndexService indexService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(@QueryParam("q") String query) throws IOException {
        JSONObject results = new JSONObject();
        QueryParser qp = new QueryParser(Version.LUCENE_40, "body", analyzer);

        SearcherManager manager = indexService.getManager();
        IndexSearcher s = manager.acquire();

        try {
            TopDocs docs = s.search(qp.parse(query), 10);
            ScoreDoc[] scoredDocs = docs.scoreDocs;
            for(ScoreDoc sd : scoredDocs) {
                Document d = s.doc(sd.doc);
                results.accumulate("results", d.getField("body").stringValue());
            }
        } catch (ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (JSONException e) {
            return Response.serverError().build();
        } finally {
            manager.release(s);
        }

        return Response.ok(results).build();
    }
}
