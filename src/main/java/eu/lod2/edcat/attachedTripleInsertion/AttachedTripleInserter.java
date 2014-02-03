package eu.lod2.edcat.attachedTripleInsertion;

import eu.lod2.edcat.utils.Tuple;
import eu.lod2.hooks.constraints.Constraint;
import eu.lod2.hooks.constraints.Priority;
import eu.lod2.hooks.contexts.AtContext;
import eu.lod2.hooks.handlers.dcat.AtCreateHandler;
import eu.lod2.hooks.handlers.dcat.AtUpdateHandler;
import eu.lod2.query.Sparql;
import org.openrdf.model.*;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Allows the user to attach raw triples to the request in a raw triple format.
 * <p/>
 * Supported formats are
 */
public class AttachedTripleInserter implements AtCreateHandler, AtUpdateHandler {

  /** Contains the context of the request on which this AttachedTripleInserter operates. */
  private AtContext context;

  @Override
  public void handleAtCreate( AtContext context ) {
    this.context = context;
    appendTriples();
  }

  @Override
  public void handleAtUpdate( AtContext context ) {
    this.context = context;
    appendTriples();
  }

  @Override
  public Collection<Priority> getConstraints( String hook ) {
    return Arrays.asList( Constraint.EARLY );
  }

  /**
   * Appends the triples to the context's model which have been attached through an attached
   * definition and removes the original triples.
   */
  private void appendTriples() {
    for ( Tuple<URI, String> attachedTripleDefinition : filterOutAttachedTriples() )
      try {
        for ( Statement triple : parseTriples( attachedTripleDefinition.left, attachedTripleDefinition.right ) )
          context.getStatements().add( replaceIdentifier( triple ) );
      } catch ( IOException e ) {
        LoggerFactory.getLogger( getClass() ).error( "Failed to parse triples." );
      } catch ( RDFParseException e ) {
        LoggerFactory.getLogger( getClass() ).error( "Failed to parse triples." );
      }
  }

  /**
   * Parses the triples in {@code content} with format {@code format}.
   *
   * @param format  Type in which the triples are stored.
   * @param content String containing the serialized form of the triples.
   * @return Collection of statements describing the parsed triples.
   */
  private Model parseTriples( URI format, String content ) throws IOException, RDFParseException {
    StringReader stream = new StringReader( content );
    return Rio.parse(
        stream,
        context.getDatasetUri().stringValue(),
        getRioFormat( format ) );
  }

  /**
   * Retrieves the information about the attached triples from the context's model and retrieves
   * the
   * information about the content.
   *
   * @return Collection of Tuple's containing the information about the attached triples.  The Left
   * of the Tuple represents the type of input, the Right represents its content.
   */
  private Collection<Tuple<URI, String>> filterOutAttachedTriples() {
    Model attachedTripleStatements = context.getStatements().filter(
        null,
        Sparql.namespaced( "edcat", "attachedTripleInserter/attachedTriples" ),
        null );
    Collection<Tuple<URI, String>> result = new ArrayList<Tuple<URI, String>>();
    // for each attachedTriples statement
    for ( Statement attachedTriplesStatement : new ArrayList<Statement>( attachedTripleStatements ) ) {
      // find the resources
      Resource descriptionObject = ( Resource ) attachedTriplesStatement.getObject();
      Statement formatStatement =
          singleCtxFilter( descriptionObject, Sparql.namespaced( "dct", "format" ), null );
      URI format = new URIImpl( formatStatement.getObject().stringValue() );
      Statement contentStatement =
          singleCtxFilter( descriptionObject, Sparql.namespaced( "edcat", "attachedTripleInserter/content" ), null );
      String content = contentStatement.getObject().stringValue();
      // add the relevant tuple
      result.add( new Tuple<URI, String>( format, content ) );
      // remove the original triples
      context.getStatements().remove( formatStatement );
      context.getStatements().remove( contentStatement );
      context.getStatements().remove( attachedTriplesStatement );
    }

    return result;
  }

  /**
   * Constructs a statement in which the identifier which may be used to identify the current
   * Dataset is replaced with the identifier of the current Dataset.
   *
   * @param s Statement in which the identifier should be injected.
   * @return New Statement in which the identifier is replaced if it existed.
   */
  private Statement replaceIdentifier( Statement s ) {
    URI statementIdentifier = Sparql.namespaced( "edcat", "attachedTripleInserter/replacedIdentifier" );
    URI newIdentifier = context.getDatasetUri();
    s = new StatementImpl(
        s.getSubject().equals( statementIdentifier ) ? newIdentifier : s.getSubject(),
        s.getPredicate().equals( statementIdentifier ) ? newIdentifier : s.getPredicate(),
        s.getObject().equals( statementIdentifier ) ? newIdentifier : s.getObject()
    );

    return s;
  }

  /**
   * Retrieves a single statement from the context's statements.
   *
   * @param subj     Subject passed to filter.
   * @param pred     Predicate passed to filter.
   * @param obj      Object passed to filter.
   * @param contexts Contexts passed to filter.
   * @return First matching statement.
   */
  private Statement singleCtxFilter( Resource subj, URI pred, Value obj, Resource... contexts ) {
    return context.getStatements().filter( subj, pred, obj, contexts ).iterator().next();
  }

  /**
   * Retrieves the Rio RDFFormat for the supplied format URI.
   * <p/>
   * The URIs used can be retrieved from http://www.w3.org/ns/formats/
   *
   * @param format URI representing the supplied format.
   * @return RDFFormat which can be used for parsing the supplied format.
   */
  private static RDFFormat getRioFormat( URI format ) {
    String formatString = format.stringValue();

    if ( formatString.equals( "http://www.w3.org/ns/formats/RDF_XML" ) )
      return RDFFormat.RDFXML;
    if ( formatString.equals( "http://www.w3.org/ns/formats/Turtle" ) )
      return RDFFormat.TURTLE;
    if ( formatString.equals( "http://www.w3.org/ns/formats/N3" ) )
      return RDFFormat.N3;

    throw new IllegalArgumentException( "Don't know how to parse format " + format );
  }

}