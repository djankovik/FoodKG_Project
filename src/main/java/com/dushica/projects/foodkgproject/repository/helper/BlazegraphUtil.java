package com.dushica.projects.foodkgproject.repository.helper;

import com.bigdata.journal.BufferMode;
import com.bigdata.journal.Options;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.util.*;

public class BlazegraphUtil {

    public static List<BindingSet> processQueryForRepository(String repositoryPath, String sparqlQuery) {
        List<BindingSet> results = new ArrayList<>();
        final Properties props = new Properties();
        props.put(Options.BUFFER_MODE, BufferMode.Disk); // persistent file system located journal
        props.put(Options.FILE, "src/main/resources/blazegraph/"+repositoryPath); // journal file location
        final BigdataSail sail = new BigdataSail(props); // instantiate a sail
        final BigdataSailRepository repo = new BigdataSailRepository(sail); // create a Sesame repository
        try {
            repo.initialize();
            try {
                RepositoryConnection cxn = repo.getConnection();
                cxn = repo.getReadOnlyConnection();
                try {
                    final TupleQuery tupleQuery = cxn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
                    final TupleQueryResult result = tupleQuery.evaluate();
                    try {
                        while (result.hasNext()) {
                            results.add(result.next());
                        }
                    } catch(Exception e){e.printStackTrace();}
                    finally {
                        result.close();
                    }
                } catch (Exception e) {e.printStackTrace();}
                finally { cxn.close();}
            } catch(Exception e){e.printStackTrace();}
            finally {
                repo.shutDown();
            }
        } catch (Exception e) {e.printStackTrace();}
        return results;
    }
}
