package eu.lod2.edcat.attachedTripleInsertion;

import eu.lod2.hooks.contexts.catalog.AtContext;
import eu.lod2.hooks.handlers.dcat.ActionAbortException;
import eu.lod2.hooks.handlers.dcat.catalog.AtCreateHandler;
import org.openrdf.model.Model;
import org.openrdf.model.URI;

/**
 * Allows the user to attach data to the Catalog in a raw triple format.
 */
public class CatalogAttachedTripleInserter extends AttachedTripleInserter implements AtCreateHandler {


  // --- VARIABLES


  /** The context which is available on creating a Catalog. */
  AtContext context;


  // --- PROVIDING FOR THE HOOKS


  @Override
  public void handleAtCreate( AtContext context ) throws ActionAbortException {
    this.context = context;
    appendTriples();
  }


  // --- IMPLEMENTING CONTEXT-SPECIFIC GETTERS FOR THE AttachedTripleInserter


  @Override
  protected URI getInsertedObjectUri() {
    return context.getCatalog().getUri();
  }

  @Override
  protected Model getContextModel() {
    return context.getStatements();
  }

  @Override
  protected URI getContextBaseURI() {
    return context.getCatalog().getUri();
  }
}
