// -*- mode: Scala;-*- 
// Filename:    QryToURI.scala 
// Authors:     lgm                                                    
// Creation:    Wed Apr 30 15:16:26 2014 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.evaluator.distribution

import com.protegra_ati.agentservices.store._
import com.protegra_ati.agentservices.store.extensions.URIExtensions._
import com.protegra_ati.agentservices.store.extensions.MonikerExtensions._

import com.biosimilarity.lift.model.ApplicationDefaults
import com.biosimilarity.lift.model.store.xml._
import com.biosimilarity.lift.model.store._
import com.biosimilarity.lift.lib._
import com.biosimilarity.lift.lib.moniker._

import java.util.UUID
import java.net.URI

case class URICnxn (
  val root : URI,
  val path : String
) extends Cnxn[URI,String,URI] {
  override def src : URI = root
  override def label : String = path
  override def trgt : URI = root
}

trait LabelToPathT {
  self : CnxnString[String, String, String] =>
  def labelToPaths(
    qry : CnxnCtxtLabel[String,String,String]
  ) : List[String] = {
    qry match {
      case CnxnCtxtLeaf( Left( t ) ) => {
        List( "?" + "=" + t )
      }
      case CnxnCtxtLeaf( Right( v ) ) => {
        List( "?" + "=" + v )
      }
      case CnxnCtxtBranch( ns, fs ) => {
        fs.flatMap(
          ( f : CnxnCtxtLabel[String,String,String] ) =>
            f match {
              case CnxnCtxtLeaf( Left( t ) ) => {
                List( "?" + ns + "=" + t )
              }
              case CnxnCtxtLeaf( Right( v ) ) => {
                List( "?" + ns + "=" + v )
              }
              case CnxnCtxtBranch( _, _ ) => {
                labelToPaths( f ).map(
                  {
                    ( p ) => {
                      if ( p( 0 ).equals( '?' ) ) {
                        ns + p
                      }
                      else {
                        ns + "/" + p
                      }
                    }
                  }
                )
              }
            }
        )
      }
    }
  }
}

trait QryToURIT extends LabelToPathT {
  self : CnxnString[String, String, String] =>
  def queryToURIStrs( cnxn : com.biosimilarity.lift.model.store.Cnxn[URI,String,URI] )(
    filter : CnxnCtxtLabel[String,String,String]
  ) : List[String] = {
    labelToPaths( filter ).map(
      ( p ) => {
        (
          cnxn.src
          + "/" + cnxn.label
          + ( if ( p( 0 ).equals( '?' ) ) { p } else { "/" + p } )
        )
      }
    )
  }  
  def queryToURIs( cnxn : com.biosimilarity.lift.model.store.Cnxn[URI,String,URI] )(
    filter : CnxnCtxtLabel[String,String,String]
  ) : List[URI] = {
    queryToURIStrs( cnxn )( filter ).map( new URI( _ ) )
  }
}

trait URIHandlerT {
  self : /*EvaluationCommsService with*/ QryToURIT =>
  import DSLCommLink.mTT
  import ConcreteHL._    

  type Rsrc = mTT.Resource

  // BUGBUG : lgm -- none of the write methods are implemented and
  // most of the read methods are all the same

  def post[Value](
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    content : Value,
    onPost : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit

  def postV[Value](
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    content : Value,
    onPost : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit

  def put[Value](
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    content : Value,
    onPut : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit

  def read(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    onReadRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) },
    sequenceSubscription : Boolean = false
  ) : Unit

  def fetch(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    onFetchRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit

  def feed(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    onFeedRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit

  def get(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    onGetRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit

  def score(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    staff : Either[Seq[Cnxn],Seq[Label]],
    onScoreRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit

  def cancel(
    filter : CnxnCtxtLabel[String,String,String],
    connections : Seq[URICnxn],
    onCancel : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "onCancel: optRsrc = " + optRsrc ) }
  ) : Unit
}
