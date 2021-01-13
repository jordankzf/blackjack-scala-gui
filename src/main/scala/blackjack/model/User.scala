package blackjack.model

import akka.actor.typed.ActorRef
import blackjack.Client
import blackjack.protocol.JsonSerializable

case class User(name: String, ref: ActorRef[Client.Command]) extends JsonSerializable {
  override def toString: String = {
    name
  }
}
