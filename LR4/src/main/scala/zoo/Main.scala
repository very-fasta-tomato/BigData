package zoo

import scala.util.Random

object Main {

  val sleepTime = 3000

  def main(args: Array[String]): Unit = {
    println("Starting animal runner")

    val Seq(animalName, hostPort, partySize) = args.toSeq

    // создание животного
    val animal = Animal(animalName, hostPort, "/zoo", partySize.toInt)

    // ZooKeeper
    try {

      // взаимодействие с ZooKeeper
      animal.enter()

      println(s"${animal.name} entered.")

      for (i <- 1 to Random.nextInt(20)) {
        Thread.sleep(sleepTime)

        println(s"${animal.name} is running...")
      }

      // выход из ZooKeeper
      animal.leave()

    } catch {
      case e: Exception => println("Animal was not permitted to the zoo." + e)
    }

  }
}