package blackjack.model

import blackjack.protocol.JsonSerializable

case class Card(rank: String, suit: String) extends JsonSerializable {
  override def toString: String = {
    s"${rank}_of_${suit}"
  }
}

case class CardMeta(isPlayer: Boolean, user: User, card: Card, cardPosition: Int) extends JsonSerializable {
  override def toString: String = {
    s"${isPlayer}_${user}_${card}_${cardPosition}"
  }
}