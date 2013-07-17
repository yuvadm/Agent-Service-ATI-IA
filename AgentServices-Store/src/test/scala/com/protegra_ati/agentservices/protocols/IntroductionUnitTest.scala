package com.protegra_ati.agentservices.protocols

import com.biosimilarity.evaluator.distribution.PortableAgentCnxn
import com.protegra_ati.agentservices.protocols.msgs._
import com.protegra_ati.agentservices.store.{AgentTestNode, TestNodeWrapper}
import java.net.URI
import java.util.UUID
import org.specs2.mutable._
import org.specs2.specification.Scope
import scala.concurrent._
import scala.concurrent.duration._

class IntroductionUnitTest extends SpecificationWithJUnit with Tags {
  class ConcreteBaseProtocols extends BaseProtocols

  class Setup extends Scope {
    val node = new TestNodeWrapper(new AgentTestNode)
    val baseProtocols = new ConcreteBaseProtocols

    val zuiCnxn = new PortableAgentCnxn(new URI("z"), "zui", new URI("ui"))
    val uizCnxn = new PortableAgentCnxn(new URI("ui"), "uiz", new URI("z"))
    val zaCnxn = new PortableAgentCnxn(new URI("z"), "za", new URI("a"))
    val azCnxn = new PortableAgentCnxn(new URI("a"), "az", new URI("z"))
    val zbCnxn = new PortableAgentCnxn(new URI("z"), "zb", new URI("b"))
    val bzCnxn = new PortableAgentCnxn(new URI("b"), "bz", new URI("z"))
    val auiCnxn = new PortableAgentCnxn(new URI("a"), "aui", new URI("ui"))
    val uiaCnxn = new PortableAgentCnxn(new URI("ui"), "uia", new URI("a"))
    val buiCnxn = new PortableAgentCnxn(new URI("b"), "bui", new URI("ui"))
    val uibCnxn = new PortableAgentCnxn(new URI("ui"), "uib", new URI("b"))

    val messageToA = "Message to A"
    val messageToB = "Message to B"

    val aRejectReason = "A Reject Reason"
    val bRejectReason = "B Reject Reason"

    val beginIntroductionRequest = new BeginIntroductionRequest(
      Some(UUID.randomUUID.toString),
      Some(zuiCnxn),
      Some(zaCnxn),
      Some(azCnxn),
      Some(zbCnxn),
      Some(bzCnxn),
      Some(messageToA),
      Some(messageToB))

    baseProtocols.genericIntroducer(node, uizCnxn)
    baseProtocols.genericIntroduced(node, zaCnxn, auiCnxn, uiaCnxn)
    baseProtocols.genericIntroduced(node, zbCnxn, buiCnxn, uibCnxn)

    val fAIRq = baseProtocols.listen(node, auiCnxn, new IntroductionRequest())
    val fBIRq = baseProtocols.listen(node, buiCnxn, new IntroductionRequest())
    val fBIRsp = baseProtocols.listen(node, zuiCnxn, new BeginIntroductionResponse(beginIntroductionRequest.requestId.get))
  }

  sequential
  section("unit", "ia")

  "Introduction protocol" should {
    "Send A's UI an introduction request" in new Setup {
      node.publish(uizCnxn)(beginIntroductionRequest)
      val aIntroductionRequest = Await.result(fAIRq, Duration(10, "seconds"))
      val bIntroductionRequest = Await.result(fBIRq, Duration(10, "seconds"))
      val aIntroductionResponse = new IntroductionResponse(aIntroductionRequest.requestId.get, Some(true), None, None, None)
      val bIntroductionResponse = new IntroductionResponse(bIntroductionRequest.requestId.get, Some(true), None, None, None)
      node.put(aIntroductionRequest.responseCnxn.get)(aIntroductionResponse)
      node.put(bIntroductionRequest.responseCnxn.get)(bIntroductionResponse)
      val beginIntroductionResponse = Await.result(fBIRsp, Duration(10, "seconds"))

      aIntroductionRequest.requestId must be_!=(None) and
        (aIntroductionRequest.responseCnxn must be_==(Some(uiaCnxn))) and
        (aIntroductionRequest.message must be_==(Some(messageToA)))
    }

    "Send B's UI an introduction request" in new Setup {
      node.publish(uizCnxn)(beginIntroductionRequest)
      val aIntroductionRequest = Await.result(fAIRq, Duration(10, "seconds"))
      val bIntroductionRequest = Await.result(fBIRq, Duration(10, "seconds"))
      val aIntroductionResponse = new IntroductionResponse(aIntroductionRequest.requestId.get, Some(true), None, None, None)
      val bIntroductionResponse = new IntroductionResponse(bIntroductionRequest.requestId.get, Some(true), None, None, None)
      node.put(aIntroductionRequest.responseCnxn.get)(aIntroductionResponse)
      node.put(bIntroductionRequest.responseCnxn.get)(bIntroductionResponse)
      val beginIntroductionResponse = Await.result(fBIRsp, Duration(10, "seconds"))

      bIntroductionRequest.requestId must be_!=(None) and
        (bIntroductionRequest.responseCnxn must be_==(Some(uibCnxn))) and
        (bIntroductionRequest.message must be_==(Some(messageToB)))
    }

    "Return approved if A and B accept" in new Setup {
      node.publish(uizCnxn)(beginIntroductionRequest)
      val aIntroductionRequest = Await.result(fAIRq, Duration(10, "seconds"))
      val bIntroductionRequest = Await.result(fBIRq, Duration(10, "seconds"))
      val aIntroductionResponse = new IntroductionResponse(aIntroductionRequest.requestId.get, Some(true), None, None, None)
      val bIntroductionResponse = new IntroductionResponse(bIntroductionRequest.requestId.get, Some(true), None, None, None)
      node.put(aIntroductionRequest.responseCnxn.get)(aIntroductionResponse)
      node.put(bIntroductionRequest.responseCnxn.get)(bIntroductionResponse)
      val beginIntroductionResponse = Await.result(fBIRsp, Duration(10, "seconds"))

      beginIntroductionResponse.responseId must be_==(beginIntroductionRequest.requestId.get) and
        (beginIntroductionResponse.accepted must be_==(Some(true))) and
        (beginIntroductionResponse.aRejectReason must be_==(None)) and
        (beginIntroductionResponse.bRejectReason must be_==(None))
    }

    "Return not approved if A accepts and B rejects" in new Setup {
      node.publish(uizCnxn)(beginIntroductionRequest)
      val aIntroductionRequest = Await.result(fAIRq, Duration(10, "seconds"))
      val bIntroductionRequest = Await.result(fBIRq, Duration(10, "seconds"))
      val aIntroductionResponse = new IntroductionResponse(aIntroductionRequest.requestId.get, Some(true), None, None, None)
      val bIntroductionResponse = new IntroductionResponse(bIntroductionRequest.requestId.get, Some(false), None, Some(bRejectReason), None)
      node.put(aIntroductionRequest.responseCnxn.get)(aIntroductionResponse)
      node.put(bIntroductionRequest.responseCnxn.get)(bIntroductionResponse)
      val beginIntroductionResponse = Await.result(fBIRsp, Duration(10, "seconds"))

      beginIntroductionResponse.responseId must be_==(beginIntroductionRequest.requestId.get) and
        (beginIntroductionResponse.accepted must be_==(Some(false))) and
        (beginIntroductionResponse.aRejectReason must be_==(None)) and
        (beginIntroductionResponse.bRejectReason must be_==(Some(bRejectReason)))
    }

    "Return not approved if A rejects and B accepts" in new Setup {
      node.publish(uizCnxn)(beginIntroductionRequest)
      val aIntroductionRequest = Await.result(fAIRq, Duration(10, "seconds"))
      val bIntroductionRequest = Await.result(fBIRq, Duration(10, "seconds"))
      val aIntroductionResponse = new IntroductionResponse(aIntroductionRequest.requestId.get, Some(false), None, Some(aRejectReason), None)
      val bIntroductionResponse = new IntroductionResponse(bIntroductionRequest.requestId.get, Some(true), None, None, None)
      node.put(aIntroductionRequest.responseCnxn.get)(aIntroductionResponse)
      node.put(bIntroductionRequest.responseCnxn.get)(bIntroductionResponse)
      val beginIntroductionResponse = Await.result(fBIRsp, Duration(10, "seconds"))

      beginIntroductionResponse.responseId must be_==(beginIntroductionRequest.requestId.get) and
        (beginIntroductionResponse.accepted must be_==(Some(false))) and
        (beginIntroductionResponse.aRejectReason must be_==(Some(aRejectReason))) and
        (beginIntroductionResponse.bRejectReason must be_==(None))
    }

    "Return not approved if A and B reject" in new Setup {
      node.publish(uizCnxn)(beginIntroductionRequest)
      val aIntroductionRequest = Await.result(fAIRq, Duration(10, "seconds"))
      val bIntroductionRequest = Await.result(fBIRq, Duration(10, "seconds"))
      val aIntroductionResponse = new IntroductionResponse(aIntroductionRequest.requestId.get, Some(false), None, Some(aRejectReason), None)
      val bIntroductionResponse = new IntroductionResponse(bIntroductionRequest.requestId.get, Some(false), None, Some(bRejectReason), None)
      node.put(aIntroductionRequest.responseCnxn.get)(aIntroductionResponse)
      node.put(bIntroductionRequest.responseCnxn.get)(bIntroductionResponse)
      val beginIntroductionResponse = Await.result(fBIRsp, Duration(10, "seconds"))

      beginIntroductionResponse.responseId must be_==(beginIntroductionRequest.requestId.get) and
        (beginIntroductionResponse.accepted must be_==(Some(false))) and
        (beginIntroductionResponse.aRejectReason must be_==(Some(aRejectReason))) and
        (beginIntroductionResponse.bRejectReason must be_==(Some(bRejectReason)))
    }
  }
}