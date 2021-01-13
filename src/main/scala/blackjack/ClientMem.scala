package blackjack

import akka.actor.typed.ActorRef
import blackjack.model.User
import scalafx.collections.ObservableHashSet

//Random stuff accessible to all controllers
object ClientMem {
  var mute: Boolean = false
  var loadFXMLCount: Int = 0
  var isPlaying: Boolean = false

  var ownRef: Option[ActorRef[Client.Command]] = None
  var ownName: String = ""

  var roomList = new ObservableHashSet[User]()
  var hostRef: Option[ActorRef[Client.Command]] = None

  def isHost: Boolean = {
    if (ownRef == hostRef) {
      return true
    } else {
      return false
    }
  }

}
