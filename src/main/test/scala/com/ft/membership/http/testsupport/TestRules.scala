package com.ft.membership.http.testsupport

import org.junit.rules.{RuleChain, TestRule}
import org.junit.runner.Description
import org.junit.runners.model.Statement

class TestRules extends TestRule{
  val simpleStub = new SimpleStub(9000)

  val ruleChain = RuleChain.outerRule(simpleStub)

  override def apply(statement: Statement, description: Description): Statement = {
    ruleChain.apply(statement, description)
  }
}
