package io.bernhardt.typedpayment.creditCardStorageSpec.domain

import java.io.File

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import io.bernhardt.typedpayment.Configuration.{CreditCardId, UserId}
import io.bernhardt.typedpayment.creditCardStorage.application.CreditCardStorageCommands
import io.bernhardt.typedpayment.creditCardStorage.domain.CreditCardStorage
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.reflect.io.Directory

class CreditCardStorageSpec extends ScalaTestWithActorTestKit with WordSpecLike with BeforeAndAfterAll {

  "The Credit Card Storage" should {

    "Add cards" in {
      val probe = createTestProbe[CreditCardStorageCommands.AddCreditCardResult]()
      val storage = spawn(CreditCardStorage())

      storage ! CreditCardStorageCommands.AddCreditCard(UserId("bob"), "1234", probe.ref)
      probe.expectMessageType[CreditCardStorageCommands.Added]
    }

    "Not add duplicate cards" in {
      val probe = createTestProbe[CreditCardStorageCommands.AddCreditCardResult]()
      val storage = spawn(CreditCardStorage())

      storage ! CreditCardStorageCommands.AddCreditCard(UserId("bob"), "4321", probe.ref)
      probe.expectMessageType[CreditCardStorageCommands.Added]
      storage ! CreditCardStorageCommands.AddCreditCard(UserId("bob"), "4321", probe.ref)
      probe.expectMessageType[CreditCardStorageCommands.Duplicate.type]
    }

    "Find cards by id" in {
      val probe = createTestProbe[CreditCardStorageCommands.AddCreditCardResult]()
      val storage = spawn(CreditCardStorage())
      storage ! CreditCardStorageCommands.AddCreditCard(UserId("bob"), "1111", probe.ref)
      val added = probe.expectMessageType[CreditCardStorageCommands.Added]

      val lookupProbe = createTestProbe[CreditCardStorageCommands.FindCreditCardResult]()
      storage ! CreditCardStorageCommands.FindById(added.id, lookupProbe.ref)
      val found = lookupProbe.expectMessageType[CreditCardStorageCommands.CreditCardFound]
      found.card.last4Digits shouldBe "1111"
    }

    "Not find unknown cards by id" in {
      val lookupProbe = createTestProbe[CreditCardStorageCommands.FindCreditCardResult]()
      val storage = spawn(CreditCardStorage())
      storage ! CreditCardStorageCommands.FindById(CreditCardId("42"), lookupProbe.ref)
      lookupProbe.expectMessageType[CreditCardStorageCommands.CreditCardNotFound.type]
    }

  }

  override protected def afterAll(): Unit = {
    val dir = new Directory(new File(system.settings.config.getString("akka.persistence.journal.leveldb.dir")))
    dir.deleteRecursively()
  }
}
