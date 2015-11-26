package com.typesafe.spark.test.mesos

import java.net.InetAddress

import com.typesafe.spark.test.mesos.mesosstate.MesosCluster
import org.scalatest.FunSuite
import org.scalatest.concurrent.TimeLimitedTests
import com.typesafe.spark.test.mesos.framework.runners.RoleConfigInfo

import scala.collection.mutable.{Set => MSet}

class ClientModeSpec(
    override val mesosConsoleUrl: String,
    override val cfg: RoleConfigInfo)
  extends FunSuite with TimeLimitedTests with MesosIntTestHelper
  with SimpleFineGrainSpec
  with SimpleCoarseGrainSpec
  with SparkPropertiesSpec
  with RolesSpec {

  import MesosIntTestHelper._

  override val timeLimit = TEST_TIMEOUT

}
