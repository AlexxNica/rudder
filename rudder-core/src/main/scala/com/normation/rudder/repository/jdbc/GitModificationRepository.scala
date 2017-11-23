package com.normation.rudder.repository.jdbc


import com.normation.eventlog.ModificationId
import com.normation.rudder.db.DB
import com.normation.rudder.repository.GitCommitId
import com.normation.rudder.repository.GitModificationRepository

import net.liftweb.common._
import com.normation.rudder.db.Doobie

import doobie._, doobie.implicits._
import cats._, cats.data._, cats.effect._, cats.implicits._

class GitModificationRepositoryImpl(
    db : Doobie
) extends GitModificationRepository {
  import db._

  def addCommit(commit: GitCommitId, modId: ModificationId): Box[DB.GitCommitJoin] = {
    val sql = sql"""
      insert into gitcommit (gitcommit, modificationid)
      values (${commit.value}, ${modId.value})
    """.update


    sql.run.attempt.transact(xa).unsafeRunSync match {
      case Right(x) => Full(DB.GitCommitJoin(commit, modId))
      case Left(ex) => Failure(s"Error when trying to add a Git Commit in DB: ${ex.getMessage}", Full(ex), Empty)
    }
  }

  def getCommits(modificationId: ModificationId): Box[Option[GitCommitId]] = {

    val sql = sql"""
      select gitcommit from gitcommit where modificationid=${modificationId.value}
    """.query[String].option

    sql.attempt.transact(xa).unsafeRunSync match {
      case Right(x)  => Full(x.map(id => GitCommitId(id)))
      case Left(ex) => Failure(s"Error when trying to get Git Commit for modification ID '${modificationId.value}': ${ex.getMessage}", Full(ex), Empty)
    }
  }

}
