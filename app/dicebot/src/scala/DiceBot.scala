package dicebot

import play.api.libs.functional.syntax._
import play.twirl.api.HtmlFormat

import spire.implicits._
import spire.math._
import spire.random._
import spire.random.rng._

import play.api.Logger

class DiceBot{
  val rng = MersenneTwister64()

  def diceRoll(num: Int, dice: Int, symbol: String, v: Int,  dd: String): String = {
    def loop(loopCount: Int, retStr: String, retSum: Int) : String = {
      if(loopCount >= num){
        val result = symbol match {
          case "&lt;" => retSum < v
          case "&gt;" => retSum > v
          case "&lt;=" => retSum <= v
          case "&gt;=" => retSum >= v
          case "==" => retSum == v
          case _ => None
        }
        if(result != None){
          if(result == true)
            "<i>" + retStr + "</i> -> <b>" + retSum.toString + " -> 成功</b>" 
          else
            "<i>" + retStr + "</i> -> <b>" + retSum.toString + " -> 失敗</b>" 
        }else{
          "<i>" + retStr + "</i> -> <b>" + retSum.toString + "</b>"
        }
      }
      else {
        val random = abs(rng.next[Int]) % dice + 1
        var strChain = ""
        if(loopCount == 0){
          strChain = random.toString
        }else{
          strChain = retStr + " + " + random.toString
        }
        loop(loopCount + 1, 
             strChain,
             retSum + random)
      }
    }
    loop(0, "", 0)
  }

  def commandCheck(str: String) : Option[String] = {
    val regex = ("""(?m)(?s)\A("""
                 + """([１-９][０-９]+|[1-9]\d+|[１-９]|[1-9])"""
                 + """([dDｄＤ])"""
                 + """([１-９][０-９]+|[1-9]\d+|[１-９]|[1-9])){1}"""
                 + """(\Z|\s|\s.*)*?"""
                 + """(&lt;|&gt;|&lt;=|&gt;=|==|\z)*?"""
                 + """(\z|\s)*?"""
                 + """([１-９][０-９]+|[1-9]\d+|[１-９]|[1-9]|\z)"""
                 + """(.*?)\Z""").r
    Logger.debug("str: " + str)
    str match {
      case regex(all, n, dd, d, s1, k, s2, v, s3) => {
        Logger.debug("all: " + all 
                     + " n: " + n.toInt
                     + " dd: " + dd 
                     + " d: " + d.toInt 
                     + " s1: " + s1 
                     + " k: " + k 
                     + " s2: " + s2 
                     + " v: " + v 
                     + " s3: " + s3)
        val vv = safeStringToInt(v).getOrElse(0)
        Some(diceRoll(n.toInt, d.toInt, k, vv, dd))
       
      }
      case _ => None
    }
  }

  def safeStringToInt(str: String): Option[Int] = {
    import scala.util.control.Exception._
    catching(classOf[NumberFormatException]) opt str.toInt
  }

  private def multiLine(text: String) = {
    HtmlFormat.raw(text).body.replace("\n", "<br/>")
  }
}

