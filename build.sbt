// build.sbt: sbt build settings for DataTables.
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

parallelExecution := true

name := "datatables-fixedcolumns-static"

organization := "com.clarifi"

version := "3.0.0"

licenses := Seq("MIT" -> url("http://datatables.net/license_mit"))

homepage := Some(url("http://www.datatables.net"))

libraryDependencies += "com.clarifi" % "datatables-static" % "1.9.4"

autoScalaLibrary := false // don't dep on scala-library

sourcesInBase := false

crossVersion := CrossVersion.Disabled // don't add _2.9.2 to artifact name

unmanagedResourceDirectories in Compile <<= baseDirectory(bd =>
  Seq(bd / "js", bd / "css"))

classDirectory in Compile ~= (_ / "com" / "clarifi" / "datatablesfixedcolumnsstatic")

// Remove precisely as many path components as we added in
// `classDirectory in Compile`, for the jar output.
products in Compile <<= (classDirectory in Compile, products in Compile) map {
  (cd, filt) =>
  (filt filter (cd !=)) :+ (cd / ".." / ".." / "..")
}

resourceGenerators in Compile <+= (streams, resourceManaged in Compile,
                                   baseDirectory) map {(s, tgt, sd) =>
  val ifile = sd / "js" / "dataTables.fixedColumns.js"
  val ofile = tgt / "js" / "dataTables.fixedColumns.min.js"
  import com.clarifi.datatablesfixedcolumnsstatic.project._
  Closure.compile(Closure.compiler(s), ifile) match {
    case Left(errs) => throw new RuntimeException(errs.size + " errors")
    case Right(compiled) => IO.write(ofile, compiled, append = false)
  }
  Seq(ofile)
}
