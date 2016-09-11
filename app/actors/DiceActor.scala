package actors

import akka.actor._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import akka.cluster.Cluster
import akka.cluster.ddata.DistributedData
import akka.cluster.ddata.Replicator
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.GSet
import akka.cluster.ddata.GSetKey
import akka.cluster.ddata.LWWRegister
import akka.cluster.ddata.LWWRegisterKey

import play.api.Logger
import akka.event.LoggingReceive

import dicebot._ // import DiceBot

class DiceActor extends Actor {
  val dice = new DiceBot()
  val replicator = DistributedData(context.system).replicator
  implicit val node = Cluster(context.system)

  def topicMsgKey(topic: String) = LWWRegisterKey[ChatMessage](topic + "-lwwreg")

  def receive = LoggingReceive {
    case ChatMessage(topic, uid, msg, date) =>
      val commandResult = dice.commandCheck(msg)
      if (commandResult != None) {
        val diceMessage = ChatMessage(topic, "DiceBot: ", msg + "\n-> " + commandResult.get, date)
        replicator ! Update(GSetKey[ChatMessage](topic), GSet.empty[ChatMessage], WriteLocal) {
          _ + diceMessage
        }
        replicator ! Update(topicMsgKey(topic), LWWRegister[ChatMessage](null), WriteLocal) {
          reg => reg.withValue(diceMessage)
        }
        replicator ! FlushChanges
      }
      Logger.debug("DiceActor get: " + msg)
  }
}
