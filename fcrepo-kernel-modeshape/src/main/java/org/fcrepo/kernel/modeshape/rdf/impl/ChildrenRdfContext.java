/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.kernel.modeshape.rdf.impl;

import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.graph.Triple.create;

import static org.fcrepo.kernel.modeshape.utils.FedoraTypesUtils.getJcrNode;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.MessageFormat;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.fcrepo.kernel.api.RdfLexicon;
import org.fcrepo.kernel.api.identifiers.IdentifierConverter;
import org.fcrepo.kernel.api.models.FedoraResource;

import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;

/**
 * @author cabeer
 * @author ajs6f
 * @since 9/16/14
 */
public class ChildrenRdfContext extends NodeRdfContext {

    private static final Logger LOGGER = getLogger(ChildrenRdfContext.class);

    /**
     * Default constructor.
     *
     * @param resource the resource
     * @param idTranslator the idTranslator
     * @throws javax.jcr.RepositoryException if repository exception occurred
     */
    public ChildrenRdfContext(final FedoraResource resource,
            final IdentifierConverter<Resource, FedoraResource> idTranslator)
            throws RepositoryException {
        super(resource, idTranslator);
        final Node node = getJcrNode(resource);
        if (node.hasNodes()) {

            try {
                // Obtain the query manager for the session via the workspace ...
                final QueryManager queryManager = node.getSession().getWorkspace().getQueryManager();
                final String template =
                        "SELECT [jcr:path] FROM [nt:base] AS s WHERE [fedora:hasParent] = ''{0}''";
                final String statement = MessageFormat.format(template, resource().getPath());

                final Query query = queryManager.createQuery(statement, Query.JCR_SQL2);

                final QueryResult result = query.execute();
                final RowIterator it = result.getRows();
                @SuppressWarnings("unchecked")
                final Iterable<Row> iterable = () -> it;
                final Stream<String> childUris = StreamSupport.stream(iterable.spliterator(), false).map(x -> {
                    try {
                        return x.getValue("jcr:path").getString();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                LOGGER.trace("Found children of this resource: {}", resource.getPath());
                concat(childUris.peek(child -> LOGGER.trace("Creating triple for child uri: {}", child))
                        .map(child -> {
                            final String uri = idTranslator.toDomain(child).getURI();
                            return create(subject(), RdfLexicon.CONTAINS.asNode(), createURI(uri));
                        }));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

}
