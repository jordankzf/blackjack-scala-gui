package blackjack.model

import scala.collection.mutable.ListBuffer

class Player(val user: User) {
  val hand: ListBuffer[Card] = ListBuffer[Card]()
  var balance = 1000
  var betAmt = 50
  var betConfirmed = false

  def newHand(): Unit = {
    hand.clear()
  }

  def addToHand(card: Card): Unit = {
    hand += card
    //println(s"${hand}")
  }

  def adjustBetAmt(amt: Int): Unit = {
    betAmt += amt
    if (betAmt > balance) {
      betAmt = balance
    }
    if (betAmt < 0) {
      betAmt = 0
    }
  }

}
