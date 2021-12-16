# -----------------------------------------------------------------------------
#
# Versionomy schema namespace
#
# -----------------------------------------------------------------------------
# Copyright 2008 Daniel Azuma
#
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright notice,
#   this list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright notice,
#   this list of conditions and the following disclaimer in the documentation
#   and/or other materials provided with the distribution.
# * Neither the name of the copyright holder, nor the names of any other
#   contributors to this software, may be used to endorse or promote products
#   derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# -----------------------------------------------------------------------------
;


module Versionomy


  # === Version number schema.
  #
  # A schema defines the structure and semantics of a version number.
  # The schema controls what fields are present in the version, how
  # version numbers are compared, what the default values are, and how
  # values can change. Version numbers with the same schema can be
  # compared with one another, and version numbers can be converted
  # trivially to formats that share the same schema, without requiring a
  # Conversion implementation.
  #
  # At its simplest, a version number is defined as a sequence of fields,
  # each with a name and data type. These fields may be integer-valued,
  # string-valued, or symbolic, though most will probably be integers.
  # Symbolic fields are enumerated types that are useful, for example, if
  # you want a field to specify the type of prerelease (e.g. "alpha",
  # "beta", or "release candidate").
  #
  # As a simple conceptual example, you could construct a schema for
  # version numbers of the form "major.minor.tiny" like this. (This is a
  # conceptual diagram, not actual syntax.)
  #
  #  ("major": integer), ("minor": integer), ("tiny": integer)
  #
  # More generally, fields are actually organized into a DAG (directed
  # acyclic graph) in which the "most significant" field is the root, the
  # next most significant is a child of that root, and so forth down the
  # line. The simple schema above, then, is actually represented as a
  # linked list (a graph with one path), like this:
  #
  #  ("major": integer) ->
  #      ("minor": integer) ->
  #          ("tiny": integer) ->
  #              nil
  #
  # It is, however, possible for the form of a field to depend on the value
  # of the previous field. For example, suppose we wanted a schema in which
  # if the value of the "minor" field is 0, then the "tiny" field doesn't
  # exist. e.g.
  #
  #  ("major": integer) ->
  #      ("minor": integer) ->
  #          [value == 0] : nil
  #          [otherwise]  : ("tiny": integer) ->
  #              nil
  #
  # The Versionomy::Schema::Field class represents a field in this graph.
  # The Versionomy::Schema::Wrapper class represents a full schema object.
  #
  # Generally, you should create schemas using Versionomy::Schema#create.
  # That method provides a DSL that lets you quickly create the fields.

  module Schema
  end


end
