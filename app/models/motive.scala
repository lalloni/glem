package models

sealed trait PushMotive

case object CommitsPublished extends PushMotive

case object BranchCreated extends PushMotive

case object BranchDeleted extends PushMotive

object PushMotive {
  val Zeros = "^0+$".r
  val NotZeros = "^[0-9a-f]*[1-9a-f]+[0-9a-f]*$".r
  def apply(push: Push): PushMotive = {
    push match {
      case Push(Zeros(), NotZeros(), _, _, _, _, _, _) ⇒ BranchCreated
      case Push(NotZeros(), Zeros(), _, _, _, _, _, _) ⇒ BranchDeleted
      case _ ⇒ CommitsPublished
    }
  }
}
