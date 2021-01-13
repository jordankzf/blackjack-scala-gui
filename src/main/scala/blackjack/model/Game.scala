package blackjack.model

import scalafx.beans.property.{BooleanProperty, StringProperty}

import scala.collection.mutable.ListBuffer

class Game(val isHost: Boolean,
           val roomList: ListBuffer[User],
           val localPlayer: User) {

  // General status
  var gameStatus: StringProperty = new StringProperty()
  var allPlayersConfirmedBet: BooleanProperty = BooleanProperty(false)
  var roundEnd: BooleanProperty = BooleanProperty(false)

  // Host to keep track
  val maxHandSize: Int = 5
  var usersWithAction = new ListBuffer[User]
  for (user <- roomList) usersWithAction += user
  var currentUser: Option[User] = None
  val dealer = new Dealer()
  val dummyCard = new Card("", "")

  // Initialise player positions
  var numOfPlayer: Int = roomList.size
  var playerLeft: Option[Player] = None
  var playerMiddle: Option[Player] = None
  var playerRight: Option[Player] = None
  setPlayerPosition()

  // ------------------------------
  // All clients will run this code
  // ------------------------------
  def roundStart(): Unit = {
    roundEnd.value = false
    println(s"game.roundStart: roomList: ${roomList}")
    for (user <- roomList) {
      getPlayer(user).get.newHand()
    }
  }

  def setPlayerPosition(): Unit = {
    val tmpList = new ListBuffer[User]()
    for (user <- roomList) tmpList += user

    //Middle Player
    tmpList -= localPlayer
    playerMiddle = Option(new Player(localPlayer))

    //Left Player
    if (tmpList.nonEmpty) {
      playerLeft = Option(new Player(tmpList.remove(0)))
    }
    //Right Player
    if (tmpList.nonEmpty) {
      playerRight = Option(new Player(tmpList.remove(0)))
    }
  }

  def getPlayerPosition(user: User): Int = {
    for (p <- playerMiddle) {
      if (p.user == user) {
        return 2
      }
    }
    for (p <- playerLeft) {
      if (p.user == user) {
        return 1
      }
    }
    for (p <- playerRight) {
      if (p.user == user) {
        return 3
      }
    }
    return 0
  }

  def getPlayer(user: User): Option[Player] = {
    if (playerMiddle.get.user == user) {
      return playerMiddle
    } else if (playerLeft.get.user == user) {
      return playerLeft
    } else if (playerRight.get.user == user) {
      return playerRight
    } else {
      return null
    }
  }

  // -------------------------
  // Only host runs this code
  // -------------------------
  def playerConfirmBet(user: User): Unit = {
    getPlayer(user).get.betConfirmed = true

    // Check all player's bet confirmed status
    var allConfirmedBetCheck = true
    for (player <- roomList) {
      if (!getPlayer(player).get.betConfirmed) {
        allConfirmedBetCheck = false
      }
    }
    allPlayersConfirmedBet.value = allConfirmedBetCheck
  }

  def getStartingCards: ListBuffer[CardMeta] = {
    val cardMetaList = new ListBuffer[CardMeta]() //isPlayer, User, Card, cardPosition
    var tempCard = dummyCard

    // First card for players
    for (user <- roomList) {
      tempCard = dealer.getCard
      getPlayer(user).get.addToHand(tempCard)
      cardMetaList += CardMeta(isPlayer = true, user, tempCard, 1)
    }
    // First card for dealer, hidden
    tempCard = dealer.getCard
    dealer.addToHand(tempCard)
    cardMetaList += CardMeta(isPlayer = false, localPlayer, dummyCard, 1)

    // Second card for players
    for (user <- roomList) {
      tempCard = dealer.getCard
      getPlayer(user).get.addToHand(tempCard)
      cardMetaList += CardMeta(isPlayer = true, user, tempCard, 2)
    }
    // Second card for dealer, visible
    tempCard = dealer.getCard
    dealer.addToHand(tempCard)
    cardMetaList += CardMeta(isPlayer = false, localPlayer, tempCard, 2)

    return cardMetaList
  }

  def getHandValue(hand : ListBuffer[Card]): Int = {
    var aceCount = 0
    var totalWithoutAces = 0
    val dict = Map("2" -> 2, "3" -> 3, "4" -> 4, "5" -> 5, "6" -> 6, "7" -> 7, "8" -> 8, "9" -> 9, "10" -> 10, "jack" -> 10, "queen" -> 10, "king" -> 10)

    for (card <- hand) {
      val rank = card.rank
      if (rank == "ace") {
        aceCount += 1
      } else {
        totalWithoutAces += dict(rank)
      }
    }

    // Return directly for fixed cases
    if (aceCount == 0) {
      return totalWithoutAces
    } else if (aceCount == 2 && hand.size == 2) {
      return 21
    }

    // Counting all possible combinations of aces
    val smallAce = 1
    var bigAce = 11
    if (hand.size > 2) {
      bigAce = 10
    }
    val aceCombination = new ListBuffer[Int]
    for (i <- 0 to aceCount) {
      val tmp = totalWithoutAces + ((i*smallAce) + (aceCount-i)*bigAce)
      if (tmp <= 21) {
        aceCombination += tmp
      }
    }

    if (aceCombination.isEmpty) {
      return totalWithoutAces + aceCount
    } else {
      return aceCombination.max
    }
  }

  def getCurrentUserTurn: Option[User] = {
    if (currentUser.isDefined) {
      return currentUser
    } else if (usersWithAction.nonEmpty) {
      currentUser = Option(usersWithAction.remove(0))
    }
    return currentUser
  }

  def playerHit(user: User): CardMeta = {
    val tempCard = dealer.getCard
    val currentPlayer = getPlayer(user).get
    currentPlayer.addToHand(tempCard)

    // If player's hand is full or above 21, no more actions
    if (currentPlayer.hand.size == maxHandSize || getHandValue(currentPlayer.hand) >= 21) {
      currentUser = None
    }
    return CardMeta(isPlayer = true, user, tempCard, currentPlayer.hand.size)
  }

  def playerStand(user: User): Unit = {
    currentUser = None
  }

  def playerIncreaseBet(user: User): Int = {
    val currentUser = getPlayer(user).get
    currentUser.adjustBetAmt(50)
    return currentUser.betAmt
  }

  def playerDecreaseBet(user: User): Int = {
    val currentUser = getPlayer(user).get
    currentUser.adjustBetAmt(-50)
    return currentUser.betAmt
  }

  // Host clear everyone's hand, generate new usersWithAction, reset bet confirmed
  def prepareNewRound(): Unit = {
    usersWithAction.clear()
    for (user <- roomList) {
      getPlayer(user).get.betConfirmed = false
      usersWithAction += user
    }
    if (usersWithAction.nonEmpty) {
      currentUser = Option(usersWithAction.remove(0))
    }
    dealer.newHand()
    allPlayersConfirmedBet.value = false
  }

  def getDealerHand: ListBuffer[CardMeta] = {
    // Dealer draw card if hand value below 15
    while (getHandValue(dealer.hand) < 15) {
      dealer.addToHand(dealer.getCard)
    }
    // Reveal dealer's cards
    val cardMetaList = new ListBuffer[CardMeta]
    for(card <- dealer.hand) {
      cardMetaList += CardMeta(isPlayer = false, localPlayer, card, (cardMetaList.size+1))
    }
    roundEnd.value = true
    return cardMetaList
  }

  def isHouseWin: Boolean = {
    val dealerHandValue = getHandValue(dealer.hand)
    var bestPlayerHandValue = 0
    for (user <- roomList) {
      val playerHandValue = getHandValue(getPlayer(user).get.hand)
      if (playerHandValue <= 21 && playerHandValue > bestPlayerHandValue) {
        bestPlayerHandValue = playerHandValue
      }
    }

    if (dealerHandValue <= 21 && dealerHandValue > bestPlayerHandValue) {
      return true
    } else {
      return false
    }
  }

  def getPlayerResult(user: User): String = {
    val currentUser = getPlayer(user).get
    val currentUserHandValue = getHandValue(currentUser.hand)
    val dealerHandValue = getHandValue(dealer.hand)
    //println(s"user: ${currentUserHandValue} dealer: ${dealerHandValue}")

    if (currentUserHandValue <= 21 && currentUserHandValue > dealerHandValue) {
      adjustPlayerBalance(currentUser, isPlayerWin = true)
      return "You won!"
    } else if (currentUserHandValue > 21 && dealerHandValue > 21) {
      return "Both are bust!"
    } else if (currentUserHandValue == dealerHandValue) {
      return "A tie!"
    } else if (currentUserHandValue > 21 && dealerHandValue <=21) {
      adjustPlayerBalance(currentUser, isPlayerWin = false)
      return "You lost!"
    } else if (dealerHandValue > 21 && currentUserHandValue <= 21) {
      adjustPlayerBalance(currentUser, isPlayerWin = true)
      return "You won!"
    } else if (currentUserHandValue < dealerHandValue) {
      adjustPlayerBalance(currentUser, isPlayerWin = false)
      return "You lost!"
    } else {
      return "An unexpected error occurred."
    }
  }

  def adjustPlayerBalance(player: Player, isPlayerWin: Boolean): Unit = {
    if (isPlayerWin) {
      player.balance += player.betAmt
    } else {
      player.balance -= player.betAmt
      player.adjustBetAmt(0)
    }
  }

}
