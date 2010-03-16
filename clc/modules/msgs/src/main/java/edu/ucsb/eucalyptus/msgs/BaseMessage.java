package edu.ucsb.eucalyptus.msgs;

import groovy.lang.GroovyObject;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;

public class BaseMessage {
  private static Logger LOG = Logger.getLogger( BaseMessage.class );
  String                correlationId;
  String                userId;
  String                effectiveUserId;
  boolean               _return;
  String                statusMessage;
  
  public BaseMessage( ) {
    super( );
    this.correlationId = UUID.randomUUID( ).toString( );
  }
  
  public BaseMessage( String userId ) {
    this( );
    this.userId = userId;
    this.effectiveUserId = userId;
    this._return = true;
    this.statusMessage = null;
  }
  public String getCorrelationId( ) {
    return this.correlationId;
  }
  public void setCorrelationId( String correlationId ) {
    this.correlationId = correlationId;
  }
  public String getUserId( ) {
    return this.userId;
  }
  public void setUserId( String userId ) {
    this.userId = userId;
  }
  public void setEffectiveUserId( String effectiveUserId ) {
    this.effectiveUserId = effectiveUserId;
  }
  public boolean is_return( ) {
    return this._return;
  }
  public void set_return( boolean return1 ) {
    this._return = return1;
  }
  public String getStatusMessage( ) {
    return this.statusMessage;
  }
  public void setStatusMessage( String statusMessage ) {
    this.statusMessage = statusMessage;
  }
  
  public String getEffectiveUserId( ) {
    if ( isAdministrator( ) ) return "eucalyptus";
    return effectiveUserId;
  }
  
  public <TYPE extends BaseMessage> TYPE regarding( ) {
    this.userId = "eucalyptus";
    this.effectiveUserId = "eucalyptus";
    return ( TYPE ) this;
  }
  
  public <TYPE extends BaseMessage> TYPE regarding( BaseMessage msg ) {
    return regarding( msg, String.format( "%f", Math.random( ) ).substring( 2 ) );
  }
  
  public <TYPE extends BaseMessage> TYPE regarding( BaseMessage msg, String subCorrelationId ) {
    this.correlationId = msg.getCorrelationId( ) + "-" + subCorrelationId;
    return regarding( );
  }
  
  public boolean isAdministrator( ) {
    return "eucalyptus".equals( this.effectiveUserId );
  }
  
  public String toString( ) {
    String str = this.toString( "msgs_eucalyptus_ucsb_edu" );
    str = ( str != null ) ? str : this.toString( "eucalyptus_ucsb_edu" );
    str = ( str != null ) ? str : "Failed to bind message of type: " + this.getClass().getName( ) + " at " + Thread.currentThread( ).getStackTrace( )[1].toString( );
    return str;
  }
  
  /**
   * Get the XML form of the message.
   * @param namespace
   * @return String representation of the object, null if binding fails.
   */
  public String toString( String namespace ) {
    ByteArrayOutputStream temp = new ByteArrayOutputStream( );
    Class targetClass = this.getClass( );
    while ( !targetClass.getSimpleName( ).endsWith( "Type" ) ) {
      targetClass = targetClass.getSuperclass( );
    }
    try {
      IBindingFactory bindingFactory = BindingDirectory.getFactory( namespace, targetClass );
      IMarshallingContext mctx = bindingFactory.createMarshallingContext( );
      mctx.setIndent( 2 );
      mctx.marshalDocument( this, "UTF-8", null, temp );
    } catch ( JiBXException e ) {
      LOG.debug( e, e );
      return null;
    }
    return temp.toString( );
  }
  
  public <TYPE extends BaseMessage> TYPE getReply( ) {
    Class msgClass = this.getClass( );
    while ( !msgClass.getSimpleName( ).endsWith( "Type" ) ) {
      msgClass = msgClass.getSuperclass( );
    }
    TYPE reply = null;
    String replyType = msgClass.getName( ).replaceAll( "Type", "" ) + "ResponseType";
    try {
      Class responseClass = ClassLoader.getSystemClassLoader( ).loadClass( replyType );
      reply = ( TYPE ) responseClass.newInstance( );
    } catch ( Exception e ) {
      LOG.debug( e, e );
      throw new TypeNotPresentException( correlationId, e );
    }
    reply.setCorrelationId( this.getCorrelationId( ) );
    reply.setUserId( this.getUserId( ) );
    reply.setEffectiveUserId( this.getEffectiveUserId( ) );
    return reply;
  }
    
}
