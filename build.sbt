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

name := "datatables-colreorderwithresize-static"

organization := "com.clarifi"

version := "1.0.8.4"

licenses := Seq("GNU General Public License (GPL), Version 2"
                  -> url("http://www.gnu.org/licenses/old-licenses/gpl-2.0.html"),
                "BSD 3-Clause"
                  -> url("http://datatables.net/license_bsd"))

homepage := Some(url("http://www.datatables.net"))

libraryDependencies += "com.clarifi" % "datatables-static" % "1.9.4"

autoScalaLibrary := false // don't dep on scala-library

sourcesInBase := false

crossVersion := CrossVersion.Disabled // don't add _2.9.2 to artifact name

resourceDirectory in Compile <<= baseDirectory(_ / "media")

excludeFilter in (Compile, unmanagedResources) <<=
  (resourceDirectory in Compile, excludeFilter in (Compile, unmanagedResources)) {
    (resd, ef) =>
    val rbase = resd.toURI
    ef || new SimpleFileFilter({s =>
      val rel = (rbase relativize s.toURI getPath)
      Seq("unit_testing/", "src/") exists (rel startsWith)})
}

classDirectory in Compile ~= (_ / "com" / "clarifi" / "datatablescolreorderwithresizestatic")

// Remove precisely as many path components as we added in
// `classDirectory in Compile`, for the jar output.
products in Compile <<= (classDirectory in Compile, products in Compile) map {
  (cd, filt) =>
  (filt filter (cd !=)) :+ (cd / ".." / ".." / "..")
}

resourceGenerators in Compile <+= (streams, resourceManaged in Compile,
                                   resourceDirectory in Compile) map {(s, tgt, sd) =>
  val ifile = sd / "js" / "ColReorder.js"
  val ofile = tgt / "js" / "ColReorder.min.js"
  import com.clarifi.datatablesstatic.project._
  Closure.compile(Closure.compiler(s), ifile) match {
    case Left(errs) => throw new RuntimeException(errs.size + " errors")
    case Right(compiled) => IO.write(ofile, compiled, append = false)
  }
  Seq(ofile)
}
