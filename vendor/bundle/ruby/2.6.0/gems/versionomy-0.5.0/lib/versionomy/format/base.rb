# -----------------------------------------------------------------------------
#
# Versionomy format base class
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

  module Format


    # The base format.
    #
    # This format doesn't actually do anything useful. It causes all strings
    # to parse to the schema's default value, and unparses all values to the
    # empty string. Instead, the purpose here is to define the API for a
    # format.
    #
    # All formats must define the methods +schema+, +parse+, and +unparse+.
    # It is also recommended that formats define the <tt>===</tt> method,
    # though this is not strictly required. Finally, formats may optionally
    # implement <tt>uparse_for_serialize</tt>.
    #
    # Formats need not extend this base class, as long as they duck-type
    # these methods.

    class Base


      # Create an instance of this base format, with the given schema.

      def initialize(schema_)
        @_schema = schema_
      end


      def inspect   # :nodoc:
        "#<#{self.class}:0x#{object_id.to_s(16)} schema=#{@_schema.inspect}>"
      end

      def to_s   # :nodoc:
        inspect
      end


      # Returns the schema understood by this format.

      def schema
        @_schema
      end


      # Parse the given string and return a value.
      #
      # The optional parameter hash can be used to pass parameters to the
      # parser to affect its behavior. The exact parameters supported are
      # defined by the format.

      def parse(string_, params_=nil)
        Value.new([], self)
      end


      # Unparse the given value and return a string.
      #
      # The optional parameter hash can be used to pass parameters to the
      # unparser to affect its behavior. The exact parameters supported
      # are defined by the format.

      def unparse(value_, params_=nil)
        ''
      end


      # An optional method that does unparsing especially for serialization.
      # Implement this if normal unparsing is "lossy" and doesn't guarantee
      # reconstruction of the version number. This method should attempt to
      # unparse in such a way that the entire version value can be
      # reconstructed from the unparsed string. Serialization routines will
      # first attempt to call this method to unparse for serialization. If
      # this method is not present, the normal unparse method will be used.
      #
      # Return either the unparsed string, or an array consisting of the
      # unparsed string and a hash of parse params to pass to the parser
      # when the string is to be reconstructed. You may also either return
      # nil or raise Versionomy::Errors::UnparseError if the unparsing
      # cannot be done satisfactorily for serialization. In this case,
      # serialization will be done using the raw value data rather than an
      # unparsed string.
      #
      # This default implementation just turns around and calls unparse.
      # Thus it is equivalent to the method not being present at all.

      def unparse_for_serialization(value_)
        unparse(value_)
      end


      # Determine whether the given value uses this format.

      def ===(obj_)
        if obj_.kind_of?(Value)
          obj_.format == self
        else
          obj_ == self
        end
      end


    end


  end

end
