package blackjack.view

import blackjack.model.{ScalaFXSound, User}
import blackjack.{Board, Client, ClientApp, ClientMem}
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.media.{Media, MediaPlayer}
import scalafx.scene.text.Text
import scalafxml.core.macros.sfxml

@sfxml
class LobbyController(private var playerName: TextField,
                      private var topText: Text,
                      private var lobbyList: ListView[User],
                      private var roomList: ListView[User],
                      private var lobbyStatusIcon: ImageView,
                      private var muteBtn: Button,
                      private var joinBtn: Button,
                      private var inviteBtn: Button,
                      private var newBtn: Button,
                      private var startBtn: Button,
                      private var leaveBtn: Button
                     ) extends ScalaFXSound {
  // Initialisation
  newBtn.disable = true
  inviteBtn.disable = true
  startBtn.disable = true
  leaveBtn.disable = true

  val media = new Media(getClass.getResource(s"/blackjack/sound/Lobby.mp3").toURI.toString)
  val player = new MediaPlayer(media)
  player.setCycleCount(MediaPlayer.Indefinite)
  if (!ClientMem.mute) player.play()

  def mute(action: ActionEvent): Unit = {
    ClientMem.mute = !ClientMem.mute
    muteUpdate()
  }

  def muteUpdate(): Unit = {
    if (!ClientMem.mute) {
      player.play()
      muteBtn.setStyle("-fx-background-color: #ccdb30;")
    } else {
      player.stop()
      muteBtn.setStyle("-fx-background-color: #ff6666;")
    }
  }

  def playerJoin(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    if (playerName.text() != "") {
      ClientMem.ownName = playerName.text()
      ClientMem.ownRef.foreach(_ ! Client.StartJoin(playerName.text()))
    } else {
      new Alert(AlertType.Warning) {
        title = "Error"
        headerText = "Name must not be empty"
        contentText = "Please insert a name."
      }.showAndWait()
    }
  }

  def playerJoined(): Unit = {
    newBtn.disable = false
    topText.text = s"Welcome, ${ClientMem.ownName}!"
    playerName.setVisible(false)
    joinBtn.setVisible(false)
    lobbyStatusIcon.image = new Image(getClass.getResourceAsStream("/blackjack/img/serverConnected.png"))
  }

  def playerInvite(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    val indexOpt = lobbyList.selectionModel().selectedIndex.toInt
    if (indexOpt < 0) { // No player selected
      new Alert(AlertType.Warning) {
        title = "Error"
        headerText = "No player selected"
        contentText = "Please select a player first."
      }.showAndWait()
    } else {
      val userRefOpt = lobbyList.selectionModel().selectedItem.value.ref
      if (!ClientMem.isHost) { // Only host can invite
        new Alert(AlertType.Warning) {
          title = "Error"
          headerText = "Only host can invite"
          contentText = "Wait for host or join another room."
        }.showAndWait()
      } else if (ClientMem.roomList.size >= 3) { // Room full
        new Alert(AlertType.Warning) {
          title = "Error"
          headerText = "Room full"
          contentText = "Wait for a player to leave before inviting again."
        }.showAndWait()
      } else if (userRefOpt == ClientApp.ownRef) { // Check valid player
        new Alert(AlertType.Warning) {
          title = "Error"
          headerText = "Invalid player"
          contentText = "Invite another player."
        }.showAndWait()
      } else {
        ClientMem.ownRef.foreach(userRefOpt ! Client.IsInvitable(_))
      }
    }
  }

  def playerInviteResult(from: User, result: Boolean): Unit = {
    if (result) {
      ClientMem.ownRef.foreach(_ ! Client.SendInvitation(from.ref))
    } else {
      new Alert(AlertType.Warning) {
        title = "Oops"
        headerText = s"${from.name} is busy right now"
        contentText = "Invite another player."
      }.showAndWait()
    }
  }

  def playerInvited(from: User): Unit = {
    val alert = new Alert(AlertType.Confirmation) {
      title = "Invitation to Room"
      headerText = s"${from.name} has invited you to their room."
      contentText = "Would you like to join?"
      }

    val result = alert.showAndWait()
    result match {
      case Some(ButtonType.OK) => {
        newBtn.disable = true
        leaveBtn.disable = false
        ClientMem.ownRef.foreach(_ ! Client.AcceptInvitation(User(ClientMem.ownName, from.ref)))
      }
      case _ => ClientMem.ownRef.foreach(_ ! Client.RejectInvitation(User(ClientMem.ownName, from.ref)))
    }
  }

  def showInvitationResponse(choice: Boolean, from: User): Unit = {
    if (choice) {
      new Alert(AlertType.Information) {
        title = "Invitation Accepted"
        headerText = s"${from.name} has accepted your invitation."
        contentText = ""
      }.showAndWait()
    } else {
      new Alert(AlertType.Information) {
        title = "Invitation Rejected"
        headerText = s"${from.name} has rejected your invitation."
        contentText = ""
      }.showAndWait()
    }
  }

  def playerNew(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    inviteBtn.disable = false
    newBtn.disable = true
    startBtn.disable = false
    leaveBtn.disable = false
    ClientMem.ownRef.foreach(_ ! Client.NewRoom)
  }

  def playerLeave(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    inviteBtn.disable = true
    newBtn.disable = false
    startBtn.disable = true
    leaveBtn.disable = true

    if (ClientMem.isHost) {
      for (user <- ClientMem.roomList) { //Inform all players in room that I'm leaving
        if (user.ref != ClientApp.ownRef) {
          ClientMem.ownRef.foreach(_ ! Client.HostLeaveRoom(user))
        }
      }
    } else {
      for (user <- ClientMem.roomList) { //Inform all players in room that I'm leaving
        if (user.ref != ClientApp.ownRef) {
          ClientMem.ownRef.foreach(_ ! Client.ClientLeaveRoom(user))
        }
      }
    }
    ClientMem.ownRef.foreach(_ ! Client.ResetRoomList)
  }

  def hostLeft(): Unit = {
    newBtn.disable = false
    leaveBtn.disable = true
    ClientMem.ownRef.foreach(_ ! Client.ResetRoomList)
    new Alert(AlertType.Warning) {
      title = "Oops"
      headerText = "The host left the room"
      contentText = "Please join another room."
    }.showAndWait()
  }

  def playerExit(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    ClientApp.mainSystem.terminate()
    System.exit(0)
  }

  def updateList(x: Iterable[User]): Unit = {
    lobbyList.items = new ObservableBuffer[User]() ++= x
  }

  def updateRoomList(x: Iterable[User]): Unit = {
    roomList.items = new ObservableBuffer[User]() ++= x
  }

  def gameStart(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    inviteBtn.disable = true
    newBtn.disable = false
    leaveBtn.disable = true
    startBtn.disable = true
    for (user <- ClientMem.roomList) { //Inform all players to start game
      ClientMem.ownRef.foreach(_ ! Client.GameStart(user.ref))
    }
  }

  def gameLoad(): Unit = {
    player.stop()
    ClientMem.isPlaying = true
    if (ClientMem.loadFXMLCount == 0) {
      ClientMem.loadFXMLCount += 1
      Board
    } else {
      Board.load()
    }
  }

}