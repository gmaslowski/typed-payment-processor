package io.bernhardt.typedpayment.creditCardStorage.domain {

  import akka.actor.typed.Behavior
  import akka.actor.typed.scaladsl.Behaviors
  import akka.persistence.typed.PersistenceId
  import akka.persistence.typed.scaladsl.EventSourcedBehavior
  import io.bernhardt.typedpayment.creditCardStorage.application.CreditCardStorageCommands._
  import io.bernhardt.typedpayment.creditCardStorage.domain.CreditCardStorageEvents.Event

  object CreditCardStorage {

    def apply(): Behavior[Command[_]] = Behaviors.setup { context =>
      EventSourcedBehavior.withEnforcedReplies[Command[_], Event, CreditCardStorageState](
        persistenceId = PersistenceId("cc"),
        emptyState = CreditCardStorageState(),
        commandHandler = (state, cmd) => state.applyCommand(context, cmd),
        eventHandler = (state, evt) => state.applyEvent(evt)
      )
    }
  }
}
