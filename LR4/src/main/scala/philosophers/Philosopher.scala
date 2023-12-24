package philosophers

import java.util.concurrent.Semaphore
import org.apache.zookeeper._
import scala.util.Random

case class Philosopher(id: Int,
                       hostPort: String,
                       root: String,
                       left: Semaphore,
                       right: Semaphore,
                       seats: Integer) extends Watcher {

  val zooKeeper = new ZooKeeper(hostPort, 3000, this)
  val mutex = new Object()
  val path: String = root + "/" + id.toString

  if (zooKeeper == null) throw new Exception("zooKeeper is not initialized")

  override def process(event: WatchedEvent): Unit = {

    // Блок синхронизации
    mutex.synchronized {
      mutex.notify()
    }
  }

  def performEating(): Boolean = {
    print("Philosopher " + id + " is going to eat\n")

    mutex.synchronized {
      var created = false

      while (true) {
        if (!created) {

          // Создадим эфимерный узел
          zooKeeper.create(path, Array.emptyByteArray, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL)
          created = true
        }

        val active = zooKeeper.getChildren(root, this)
        if (active.size() > seats) {
          zooKeeper.delete(path, -1)

          mutex.wait(3000)

          Thread.sleep(Random.nextInt(5)* 100)
          created = false
        } else {
          left.acquire()
          print("Philosopher " + (id + 1) +  " picked up the left fork\n")

          right.acquire()
          print("Philosopher " + (id + 1) + " picked up the right fork\n")

          Thread.sleep((Random.nextInt(5) + 1) * 1000)

          right.release()
          print("Philosopher " + (id + 1) + " put the right fork\n")

          left.release()
          print("Philosopher " + (id + 1) + " put the loft fork and finished eating\n")
          return true
        }
      }
    }

    false
  }

  // освобождение узла
  def performThinking(): Unit = {
    printf("Philosopher " + (id + 1) + " is thinking")

    zooKeeper.delete(path, -1)

    Thread.sleep((Random.nextInt(5) + 1) * 1000)
  }
}