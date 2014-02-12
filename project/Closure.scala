// Closure.scala: Functions for running Google Closure in an sbt build.
// Copyright (C) 2013  McGraw Hill Financial
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
// ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package com.clarifi.datatablesfixedcolumnsstatic.project

import scala.collection.JavaConverters._

import sbt.{File, Level, Logger}
import sbt.std.TaskStreams
import com.google.javascript.jscomp

object Closure {
  def compiler(s: TaskStreams[_]) =
    new jscomp.Compiler(new SbtClosureErrors(s.log))

  def compile(c: jscomp.Compiler, inf: File) = {
    val res = c.compile(List(jscomp.SourceFile.fromCode("/dev/null", "")).asJava,
                        List(jscomp.SourceFile.fromFile(inf)).asJava,
                        stdOptions)
    if (res.success) Right(c.toSource)
    else Left(res.errors.toSeq)
  }

  // simulate command line from scripts/make.sh with our defaults
  private def stdOptions = {
    val v = new jscomp.CompilerOptions()
    val lvl = jscomp.CompilationLevel.SIMPLE_OPTIMIZATIONS
    lvl.setOptionsForCompilationLevel(v)
    v
  }
}

/** Report Closure errors via sbt. */
private[project] final class SbtClosureErrors(log: Logger)
    extends jscomp.BasicErrorManager {
  override def println(l: jscomp.CheckLevel, e: jscomp.JSError): Unit = {
    import jscomp.CheckLevel._
    log log (l match {case ERROR => Level.Error
                      case OFF => Level.Debug
                      case WARNING | _ => Level.Warn},
             e.toString)
  }

  // very much like sbt.LoggerReporter#printSummary.
  override def printSummary(): Unit = {
    import sbt.LoggerReporter.countElementsAsString
    if (getWarningCount > 0)
      log warn (countElementsAsString(getWarningCount, "warning") + " found")
    if (getErrorCount > 0)
      log error (countElementsAsString(getErrorCount, "error") + " found")
  }
}
