package com.protegra_ati.agentservices.protocols.msgs

// TODO: Add introduction profile to message
case class GetIntroductionProfileResponse(override val responseId: String) extends ProtocolResponseMessage {
  override def isValid: Boolean = {
    true
  }
}