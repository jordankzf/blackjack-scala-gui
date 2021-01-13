package blackjack.model

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Random

class Dealer {
  var deck:ArrayBuffer[Card] = ArrayBuffer.empty[Card]
  val ranks: List[String] = List("ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king")
  val suits: List[String] = List("diamonds", "clubs", "hearts", "spades")
  var hand = new ListBuffer[Card]()

  def generateDeck(): Unit = {
    for (suit <- suits) {
      for (rank <- ranks) {
        deck += Card(rank, suit)
      }
    }
    deck = Random.shuffle(deck)
  }

  def getCard: Card = {
    if (deck.isEmpty) {
      generateDeck()
    }
    deck.remove(0)
  }

  def addToHand(card: Card): Unit = {
    hand += card
  }

  def newHand(): Unit = {
    hand.clear()
  }

}
