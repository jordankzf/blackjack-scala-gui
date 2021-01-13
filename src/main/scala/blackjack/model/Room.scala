package blackjack.model

import akka.actor.typed.ActorRef
import blackjack.Client
import blackjack.protocol.JsonSerializable
import scalafx.collections.ObservableHashSet

import scala.collection.mutable.ListBuffer

object Room extends JsonSerializable{
  val maxRoomSize = 3
  var status = "WAITING"

  var roomList = ListBuffer[User]()

  def setHost(user: User): Unit = {
    roomList += user
  }

  def addPlayer(user: User): Unit = {
    roomList += user
  }

  def removePlayer(user: User): Unit = {
    roomList -= user
  }

  def reset(): Unit = {
    roomList = ListBuffer[User]()
  }

  override def toString: String = {
    s"blank's room | ${status} | /${maxRoomSize}"
  }
}
