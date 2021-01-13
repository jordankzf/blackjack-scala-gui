package blackjack.view

import blackjack.model.{CardMeta, Game, ScalaFXSound, User}
import blackjack.{Client, ClientApp, ClientMem, Lobby}
import scalafx.animation.AnimationTimer
import scalafx.event.ActionEvent
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, Button, ButtonType}
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.media.{Media, MediaPlayer}
import scalafx.scene.text.Text
import scalafxml.core.macros.sfxml

import scala.collection.mutable.ListBuffer
@sfxml
class BoardController(private var playerLeftName: Text,
                      private var playerMiddleName: Text,
                      private var playerRightName: Text,
                      private var playerLeftBetLabel: Text,
                      private var playerLeftBalLabel: Text,
                      private var playerRightBetLabel: Text,
                      private var playerRightBalLabel: Text,
                      private var playerLeftCard1: ImageView,
                      private var playerLeftCard2: ImageView,
                      private var playerLeftCard3: ImageView,
                      private var playerLeftCard4: ImageView,
                      private var playerLeftCard5: ImageView,
                      private var playerMiddleCard1: ImageView,
                      private var playerMiddleCard2: ImageView,
                      private var playerMiddleCard3: ImageView,
                      private var playerMiddleCard4: ImageView,
                      private var playerMiddleCard5: ImageView,
                      private var playerRightCard1: ImageView,
                      private var playerRightCard2: ImageView,
                      private var playerRightCard3: ImageView,
                      private var playerRightCard4: ImageView,
                      private var playerRightCard5: ImageView,
                      private var playerTopCard1: ImageView,
                      private var playerTopCard2: ImageView,
                      private var playerTopCard3: ImageView,
                      private var playerTopCard4: ImageView,
                      private var playerTopCard5: ImageView,
                      private var statusText: Text,
                      private var hitBtn: Button,
                      private var standBtn: Button,
                      private var increaseBetBtn: Button,
                      private var decreaseBetBtn: Button,
                      private var confirmBetBtn: Button,
                      private var playerLeftBet: Text,
                      private var playerMiddleBet: Text,
                      private var playerRightBet: Text,
                      private var playerLeftBal: Text,
                      private var playerMiddleBal: Text,
                      private var playerRightBal: Text,
                      private var nextRoundBtn: Button,
                      private var playerConfetti: ImageView,
                      private var muteBtn: Button
                     ) extends ScalaFXSound {
  // Initialise board state
  val blankImg = new Image(getClass.getResourceAsStream("/blackjack/img/blank.png"))
  clearBoard()
  nextRoundBtn.disable = true
  nextRoundBtn.visible = false
  playerMiddleName.text = ClientMem.ownName

  if (!ClientMem.mute) {
    muteBtn.setStyle("-fx-background-color: #ccdb30;")
  } else {
    muteBtn.setStyle("-fx-background-color: #ff6666;")
  }

  val media = new Media(getClass.getResource(s"/blackjack/sound/CasinoAmbience.mp3").toURI.toString)
  val player = new MediaPlayer(media)
  player.setCycleCount(MediaPlayer.Indefinite)
  if (!ClientMem.mute) player.play()

  private val localPlayer = User(ClientMem.ownName, ClientApp.ownRef)
  var game = new Game(ClientMem.isHost, ClientMem.roomList.to[ListBuffer], localPlayer)

  // Initialise additional players
  if (game.playerLeft.isDefined) {
    playerLeftName.text = game.playerLeft.get.user.name
  } else {
    playerLeftBetLabel.setVisible(false)
    playerLeftBalLabel.setVisible(false)
    playerLeftBet.setVisible(false)
    playerLeftBal.setVisible(false)
  }
  if (game.playerRight.isDefined) {
    playerRightName.text = game.playerRight.get.user.name
  } else {
    playerRightBetLabel.setVisible(false)
    playerRightBalLabel.setVisible(false)
    playerRightBet.setVisible(false)
    playerRightBal.setVisible(false)
  }

  // Host giving cards animation
  var cardMetaList = new ListBuffer[CardMeta]
  var time = 0L
  val timerCard: AnimationTimer = AnimationTimer(t => {
    if (cardMetaList.isEmpty) {
      timerCard.stop
      if (game.getCurrentUserTurn.isDefined && game.allPlayersConfirmedBet.value) {
        announceTurn(game.getCurrentUserTurn.get)
      }
    }
    if ((t - time) > 0.333e9) {
      val currentCard = cardMetaList.remove(0)
      for (user <- ClientMem.roomList) {
        ClientMem.ownRef.foreach(_ ! Client.GiveCard(currentCard, user))
      }
      time = t
    }
  })

  // Host starts first round
  if (ClientMem.isHost) {
    game.roundStart()
    roundStart()
    nextRoundBtn.visible = true
  }

  // Host starts distributing cards after all players confirmed bet
  game.allPlayersConfirmedBet.onChange((_, old, newV) => {
    if (game.allPlayersConfirmedBet.value && ClientMem.isHost) {
      cardMetaList = game.getStartingCards
      timerCard.start
    }
  })

  // Host wait after each action before calling the next user's turn
  val timerTurn: AnimationTimer = AnimationTimer(t => {
    if (time == 0L) {
      time = t
    }
    if ((t - time) > 0.5e9) {
      //Get next player's turn
      val nextPlayer = game.getCurrentUserTurn
      if (nextPlayer.isDefined) {
        announceTurn(nextPlayer.get)
      } else {
        roundEnd()
      }
      timerTurn.stop
    }
  })

  def mute(action: ActionEvent): Unit = {
    ClientMem.mute = !ClientMem.mute
    if (!ClientMem.mute) {
      player.play()
      muteBtn.setStyle("-fx-background-color: #ccdb30;")
    } else {
      player.stop()
      muteBtn.setStyle("-fx-background-color: #ff6666;")
    }
  }

  // Host send round start command
  def roundStart(): Unit = {
    nextRoundBtn.disable = true
    clearBoard()
    playerConfetti.visible = false
    game.roundStart()
    for (user <- ClientMem.roomList) { //Inform clients to restart round
      if (user.ref != ClientMem.hostRef.get) {
        ClientMem.ownRef.foreach(_ ! Client.RoundStart(user))
      }
    }
  }

  // Client receive round start command
  def roundStartReceive(): Unit = {
    clearBoard()
    playerConfetti.visible = false
    game.roundStart()
  }

  // Client receive cards to display
  def updateCard(cardMeta: CardMeta): Unit = {
    var playerInt = game.getPlayerPosition(cardMeta.user)
    val currentPlayer = game.getPlayer(cardMeta.user).get
    if (!ClientMem.mute) playCardSlideSound()

    // Dealer card is placed on top
    if (!cardMeta.isPlayer) {
      playerInt = 0

      // Make sure dealer's hand fully revealed before enabling next round button
      if (ClientMem.isHost && cardMeta.cardPosition == game.dealer.hand.size && game.roundEnd.value) {
        nextRoundBtn.disable = false
      }
    }

    // Non-host clients update player object's hand
    if (cardMeta.isPlayer && !ClientMem.isHost) {
      currentPlayer.addToHand(cardMeta.card)
    }

    // Play Blackjack, Lucky13 sound for local client player
    if (cardMeta.isPlayer && cardMeta.user.ref == ClientMem.ownRef.get && cardMeta.cardPosition >= 2) {
      val handValue = game.getHandValue(currentPlayer.hand)
      if (handValue == 21) {
        if (!ClientMem.mute) playBlackjackSound()
      } else if (handValue > 21) {
        if (!ClientMem.mute) playBustSound()
      }
    }

    val cardImg = new Image(getClass.getResourceAsStream(s"/blackjack/img/cards/${cardMeta.card.toString}.jpg"))

    if (playerInt == 0) { //Dealer
      if (cardMeta.cardPosition == 1) playerTopCard1.image = cardImg
      else if (cardMeta.cardPosition == 2) playerTopCard2.image = cardImg
      else if (cardMeta.cardPosition == 3) playerTopCard3.image = cardImg
      else if (cardMeta.cardPosition == 4) playerTopCard4.image = cardImg
      else if (cardMeta.cardPosition == 5) playerTopCard5.image = cardImg
    }
    else if (playerInt == 1) { //Left player
      if (cardMeta.cardPosition == 1) playerLeftCard1.image = cardImg
      else if (cardMeta.cardPosition == 2) playerLeftCard2.image = cardImg
      else if (cardMeta.cardPosition == 3) playerLeftCard3.image = cardImg
      else if (cardMeta.cardPosition == 4) playerLeftCard4.image = cardImg
      else if (cardMeta.cardPosition == 5) playerLeftCard5.image = cardImg
    }
    else if (playerInt == 2) { //Middle player
      if (cardMeta.cardPosition == 1) playerMiddleCard1.image = cardImg
      else if (cardMeta.cardPosition == 2) playerMiddleCard2.image = cardImg
      else if (cardMeta.cardPosition == 3) playerMiddleCard3.image = cardImg
      else if (cardMeta.cardPosition == 4) playerMiddleCard4.image = cardImg
      else if (cardMeta.cardPosition == 5) playerMiddleCard5.image = cardImg
    }
    else if (playerInt == 3) { //Right player
      if (cardMeta.cardPosition == 1) playerRightCard1.image = cardImg
      else if (cardMeta.cardPosition == 2) playerRightCard2.image = cardImg
      else if (cardMeta.cardPosition == 3) playerRightCard3.image = cardImg
      else if (cardMeta.cardPosition == 4) playerRightCard4.image = cardImg
      else if (cardMeta.cardPosition == 5) playerRightCard5.image = cardImg
    }
  }

  // Host send turn announcement
  def announceTurn(currentUser: User): Unit = {
    for (user <- ClientMem.roomList) {
      ClientMem.ownRef.foreach(_ ! Client.AnnounceTurn(currentUser, user))
    }
  }

  // Client receive turn announcement
  def announceTurnReceive(currentUser: User): Unit = {
    if (currentUser.ref == ClientMem.ownRef.get) {
      if (!ClientMem.mute) playPlayerTurnSound()
      statusText.text = "It is your turn, choose an action!"
      hitBtn.disable = false
      standBtn.disable = false
    } else {
      statusText.text = s"${currentUser.name}'s turn!"
      hitBtn.disable = true
      standBtn.disable = true
    }
  }

  // Client send player hit action
  def playerHit(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    ClientMem.hostRef.foreach(_ ! Client.PlayerHit(localPlayer))
    hitBtn.disable = true
    standBtn.disable = true
    increaseBetBtn.disable = true
    decreaseBetBtn.disable = true
  }

  // Host receive player hit action
  def playerHitReceive(currentUser: User): Unit = {
    val cardMeta = game.playerHit(currentUser)
    for (user <- Client.roomList) {
      ClientMem.ownRef.foreach(_ ! Client.GiveCard(cardMeta, user))
    }
    time = 0L
    timerTurn.start
  }

  // Client send player stand action
  def playerStand(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    ClientMem.hostRef.foreach(_ ! Client.PlayerStand(localPlayer))
    hitBtn.disable = true
    standBtn.disable = true
    increaseBetBtn.disable = true
    decreaseBetBtn.disable = true
  }

  // Host receive player stand action
  def playerStandReceive(currentUser: User): Unit = {  //Host Receive Player Stand Response
    game.playerStand(currentUser)
    time = 0L
    timerTurn.start
  }

  // Client send increase bet action
  def increaseBet(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    ClientMem.ownRef.foreach(_ ! Client.IncreaseBetSend(ClientMem.hostRef.get))
  }

  // Client send decrease bet action
  def decreaseBet(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    ClientMem.ownRef.foreach(_ ! Client.DecreaseBetSend(ClientMem.hostRef.get))
  }

  // Host receive increase bet action
  def increaseBetReceive(user: User): Unit = {
    updateBet(user, game.playerIncreaseBet(user))
  }

  // Host receive decrease bet action
  def decreaseBetReceive(user: User): Unit = {
    updateBet(user, game.playerDecreaseBet(user))
  }

  // Host send updated bet amount
  def updateBet(user: User, amt: Int): Unit = {
    for (target <- ClientMem.roomList) {
      ClientMem.ownRef.foreach(_ ! Client.UpdateBet(target, user, amt))
    }
  }

  // Client receive updated bet amount
  def updateBetReceive(user: User, amt: Int): Unit = {
    var playerInt = game.getPlayerPosition(user)

    if (playerInt == 1) {
      playerLeftBet.text = s"${amt}"
    } else if (playerInt == 2) {
      playerMiddleBet.text = s"${amt}"
    } else if (playerInt == 3) {
      playerRightBet.text = s"${amt}"
    }
  }

  def confirmBet(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    ClientMem.ownRef.foreach(_ ! Client.ConfirmBetSend(ClientMem.hostRef.get))
  }

  def confirmBetReceiveAck(): Unit = {
    increaseBetBtn.disable = true
    decreaseBetBtn.disable = true
    confirmBetBtn.disable = true
  }

  def confirmBetReceive(from: User): Unit = {
    game.playerConfirmBet(from)
  }

  // Host send updated balance and bet amount
  def updateBalAndBet(): Unit = {
    for (player <- ClientMem.roomList) { // For each player's balance and bet amount
      val currentUser = game.getPlayer(player).get
      for (target <- ClientMem.roomList) { // Send it to all players
        ClientMem.ownRef.foreach(_ ! Client.UpdateBalAndBet(target, player, currentUser.balance, currentUser.betAmt))
      }
    }
  }

  // Client receive updated balance and bet amount
  def updateBalAndBetReceive(user: User, bal: Int, bet: Int): Unit = {
    val playerInt = game.getPlayerPosition(user)

    if (playerInt == 1) {
      playerLeftBal.text = s"${bal}"
      playerLeftBet.text = s"${bet}"
    } else if (playerInt == 2) {
      playerMiddleBal.text = s"${bal}"
      playerMiddleBet.text = s"${bet}"
    } else if (playerInt == 3) {
      playerRightBal.text = s"${bal}"
      playerRightBet.text = s"${bet}"
    }
  }

  //Host send dealer's final hand and announce winner
  def roundEnd(): Unit = {
    this.cardMetaList = game.getDealerHand
    timerCard.start

    val isHouseWin = game.isHouseWin
    for (user <- Client.roomList) {
      if (isHouseWin) {
        ClientMem.ownRef.foreach(_ ! Client.AnnounceHouseWin(user))
      }
      ClientMem.ownRef.foreach(_ ! Client.AnnounceWinResult(user, game.getPlayerResult(user)))
    }
    updateBalAndBet()
    //nextRoundBtn.disable = false
  }

  // Client receive house win announcement
  def announceHouseWin(): Unit = {
    if (!ClientMem.mute) playHouseWinSound()
  }

  def announceWinResult(playerResult: String): Unit = {
    statusText.text = playerResult
    if (playerResult == "You won!") {
      playerConfetti.visible = true
    }
  }

  // Host start next round
  def hostStartNextRound(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    game.prepareNewRound()
    roundStart()
  }

  // Clear cards and disable buttons
  def clearBoard(): Unit = {
    statusText.text = "Waiting for players to place bets"
    playerLeftCard1.image = blankImg
    playerLeftCard2.image = blankImg
    playerLeftCard3.image = blankImg
    playerLeftCard4.image = blankImg
    playerLeftCard5.image = blankImg
    playerMiddleCard1.image = blankImg
    playerMiddleCard2.image = blankImg
    playerMiddleCard3.image = blankImg
    playerMiddleCard4.image = blankImg
    playerMiddleCard5.image = blankImg
    playerRightCard1.image = blankImg
    playerRightCard2.image = blankImg
    playerRightCard3.image = blankImg
    playerRightCard4.image = blankImg
    playerRightCard5.image = blankImg
    playerTopCard1.image = blankImg
    playerTopCard2.image = blankImg
    playerTopCard3.image = blankImg
    playerTopCard4.image = blankImg
    playerTopCard5.image = blankImg
    hitBtn.disable = true
    standBtn.disable = true
    increaseBetBtn.disable = false
    decreaseBetBtn.disable = false
    confirmBetBtn.disable = false
  }

  // Client update status text
  def updateStatusText(text: String): Unit = {
    statusText.text = text
  }

  // Client leave game
  def leaveGame(action: ActionEvent): Unit = {
    if (!ClientMem.mute) playClickSound()
    val alert = new Alert(AlertType.Warning) {
      title = "Leave room"
      headerText = "Leave room"
      contentText = "Are you sure to leave the room?"
    }

    val result = alert.showAndWait()
    result match {
      case Some(ButtonType.OK) =>
        player.stop()
        ClientMem.isPlaying = false
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
        Lobby.load()
      case _ =>
    }
  }

  // Client receive notice that a player disconnected
  def clientSuddenLeft(user: User): Unit = {
    val playerPosition = game.getPlayerPosition(user)
    game.roomList -= user

    if (playerPosition == 1) {
      playerLeftName.setStrikethrough(true)
    } else if (playerPosition == 3) {
      playerRightName.setStrikethrough(true)
    }

    if (ClientMem.isHost) {
      nextRoundBtn.disable = false
      new Alert(AlertType.Warning) {
        title = "Oops"
        headerText = s"${user} has left the room"
        contentText = "Please start a new round."
      }.showAndWait()
    }
  }

}