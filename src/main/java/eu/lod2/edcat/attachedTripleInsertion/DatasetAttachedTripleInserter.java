package eu.lod2.edcat.attachedTripleInsertion;

import eu.lod2.hooks.contexts.dataset.AtContext;
import eu.lod2.hooks.handlers.dcat.ActionAbortException;
import eu.lod2.hooks.handlers.dcat.dataset.AtCreateHandler;
import eu.lod2.hooks.handlers.dcat.dataset.AtUpdateHandler;
import org.openrdf.model.Model;
import org.openrdf.model.URI;

/**
 * Allows the user to attach data to the Datasets in a raw triple format.
 */
public class DatasetAttachedTripleInserter extends AttachedTripleInserter implements AtCreateHandler, AtUpdateHandler {


   // --- VARIABLES


  /** Contains the datasetContext of the request on which this AttachedTripleInserter operates. */
  private AtContext datasetContext;


  // --- PROVIDING FOR THE HOOKS


  @Override
  public void handleAtCreate( AtContext context ) {
    this.datasetContext = context;
    appendTriples();
  }

  @Override
  public void handleAtUpdate( AtContext context ) throws ActionAbortException {
    this.datasetContext = context;
    appendTriples();
  }


  // --- IMPLEMENTING CONTEXT-SPECIFIC GETTERS FOR THE AttachedTripleInserter


  /**
   * Retrieves the URI of the object on which we are operating in this request.
   *
   * @return URI of the object on which this request operates.
   */
  protected URI getInsertedObjectUri(){
    return datasetContext.getDatasetUri();
  }

  /**
   * Returns the model from the currently active context.
   *
   * @return Model which is contained in the current Context.
   */
  protected Model getContextModel(){
    return datasetContext.getStatements();
  }

  /**
   * Retrieves the base URI for importing triples in the current context.
   *
   * @return base URI.
   */
  protected URI getContextBaseURI(){
    return datasetContext.getDatasetUri();
  }

}