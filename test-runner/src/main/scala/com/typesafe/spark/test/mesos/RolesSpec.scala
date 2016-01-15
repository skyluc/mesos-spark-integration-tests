package com.typesafe.spark.test.mesos

import org.scalatest.Assertions._

import com.typesafe.spark.test.mesos.mesosstate.MesosCluster
import com.typesafe.spark.test.mesos.framework.runners.RoleConfigInfo


trait RolesSpec { self: MesosIntTestHelper =>

  def mesosConsoleUrl: String

  def cfg: RoleConfigInfo

  runSparkTest("simple count in fine-grain mode with role",
    "spark.mesos.coarse" -> "false", "spark.mesos.role" -> cfg.role, "spark.cores.max" -> cfg.roleCpus) { sc =>
    val rdd = sc.makeRDD(1 to 5)
    val res = rdd.sum()

    assert(15 == res)

    val m = MesosCluster.loadStates(mesosConsoleUrl)
    assert(m.sparkFramework.isDefined, "The driver should be running")

    if (cfg.role != "*") {
      assert(m.slaves.flatMap { x => x.roleResources.map { y => y.roleName } }.contains(cfg.role),
          s"Role ${cfg.role} should be available on the cluster")

      assertResult(cfg.roleCpus.toInt, "Wrong number of used cpus"){
       m.sparkFramework.get.resources.cpu
      }
    }
  }

  ignoreSparkTest("simple count in coarse-grained mode with role",
    "spark.mesos.coarse" -> "true", "spark.mesos.role" -> cfg.role, "spark.cores.max" -> cfg.roleCpus) { sc =>
    val rdd = sc.makeRDD(1 to 5)
    val res = rdd.sum()

    assert(15 == res)

    val m = MesosCluster.loadStates(mesosConsoleUrl)
    assert(m.sparkFramework.isDefined, "The driver should be running")

    if (cfg.role != "*") {
      // TODO: add message
      assert(m.slaves.flatMap { x => x.roleResources.map { y => y.roleName } }.contains(cfg.role))

      // make sure reserved resources for that role are used
      m.slaves.foreach {
        x =>
          val reserved = x.roleResources.filter(r => r.roleName == cfg.role).head
          val used = x.usedResources
          // TODO: add message
          assert(reserved.resources.cpu >= used.cpu)
      }

      // TODO: add message
      assert(m.sparkFramework.get.resources.cpu == cfg.roleCpus.toInt)
    }
  }
}
