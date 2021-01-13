package blackjack.model

import scalafx.scene.media.{Media, MediaPlayer}

import scala.util.Random

trait ScalaFXSound extends MPlayer {
  def playSelectedSound(filename: String, loop: Boolean = false): Unit = {
    val media = new Media(getClass.getResource(s"/blackjack/sound/${filename}").toURI.toString)
    val player = new MediaPlayer(media)
    if (loop) player.setCycleCount(MediaPlayer.Indefinite)
    player.stop()
    player.play()
  }
  override def playBlackjackSound(): Unit = {
    playSelectedSound(s"vo/Blackjack${Random.nextInt(8) + 1}.m4a")
  }
  override def playBustSound(): Unit = {
    playSelectedSound(s"vo/Bust${Random.nextInt(14) + 1}.m4a")
  }
  override def playHouseWinSound(): Unit = {
    playSelectedSound(s"vo/HouseWins${Random.nextInt(8) + 1}.m4a")
  }
  override def playRoundStartSound(): Unit = {
    playSelectedSound(s"vo/Start${Random.nextInt(7) + 1}.m4a")
  }
  override def playPlayerTurnSound(): Unit = {
    Random.setSeed(System.currentTimeMillis())
    playSelectedSound(s"vo/YourTurn${Random.nextInt(9) + 1}.m4a")
  }
  override def playCardSlideSound(): Unit = {
    playSelectedSound(s"vo/Slide${Random.nextInt(3) + 1}.m4a")
  }
  override def playAmbienceSound(): Unit = {
    playSelectedSound("CasinoAmbience.mp3", loop = true)
  }
  override def playLobbySound(): Unit = {
    playSelectedSound("Lobby.mp3", loop = true)
  }
  override def playClickSound(): Unit = {
    playSelectedSound("Click.wav")
  }
}
