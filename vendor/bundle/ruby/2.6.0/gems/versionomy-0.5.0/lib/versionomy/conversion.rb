# -----------------------------------------------------------------------------
#
# Versionomy conversion interface and registry
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


require 'thread'


module Versionomy


  # === Conversion between version schemas.
  #
  # Conversions are algorithms for converting from one schema to another.
  # This is useful for performing conversions as well as comparing version
  # numbers that use different schemas.
  #
  # To implement a conversion algorithm, implement the API defined by
  # Versionomy::Conversion::Base. Then, register your conversion by calling
  # Versionomy::Conversion#register. You will need to specify which schemas
  # (from and to) that your conversion should handle. From that point on,
  # whenever Versionomy needs to convert a value between those two schemas,
  # it will use your conversion. You can register the same conversion object
  # for multiple pairs of schemas, but you can register only one conversion
  # object for any pair.
  #
  # A common technique for doing conversions is to unparse the version to a
  # string, and then parse it in the new format. Versionomy provides a tool,
  # Versionomy::Conversion::Parsing, for performing such conversions. The
  # conversions between the standard and rubygems formats uses this tool.
  # See Versionomy::Conversion::Rubygems for annotated examples.

  module Conversion

    @registry = {}
    @mutex = ::Mutex.new

    class << self


      # Convert the given value to the given format. This is identical to
      # calling <tt>value_.convert(format_, convert_params_)</tt>.
      #
      # The format may be specified as a format object or as the name of a
      # format in the Format registry.
      #
      # Raises Versionomy::Errors::ConversionError if the value could not
      # be converted.

      def convert(value_, format_, convert_params_=nil)
        value_.convert(format_, convert_params_)
      end


      # Get a conversion capable of converting between the given schemas.
      #
      # The schemas may be specified as format names, Format objects,
      # schema wrapper objects, or the root field of the schema.
      #
      # If strict is set to false, returns nil if no such conversion could
      # be found. If strict is set to true, may raise one of these errors:
      #
      # Raises Versionomy::Errors::UnknownFormatError if a format was
      # specified by name but the name is not known.
      #
      # Raises Versionomy::Errors::UnknownConversionError if the formats
      # were recognized but no conversion was found to handle them.

      def get(from_schema_, to_schema_, strict_=false)
        key_ = _get_key(from_schema_, to_schema_)
        conversion_ = @mutex.synchronize{ @registry[key_] }
        if strict_ && conversion_.nil?
          raise Errors::UnknownConversionError
        end
        conversion_
      end


      # Register the given conversion as the handler for the given schemas.
      #
      # The schemas may be specified as format names, Format objects,
      # schema wrapper objects, or the root field of the schema.
      #
      # Raises Versionomy::Errors::ConversionRedefinedError if a conversion
      # has already been registered for the given schemas.
      #
      # Raises Versionomy::Errors::UnknownFormatError if a format was
      # specified by name but the name is not known.

      def register(from_schema_, to_schema_, conversion_, silent_=false)
        key_ = _get_key(from_schema_, to_schema_)
        @mutex.synchronize do
          if @registry.include?(key_)
            unless silent_
              raise Errors::ConversionRedefinedError
            end
          else
            @registry[key_] = conversion_
          end
        end
      end


      private

      def _get_key(from_schema_, to_schema_)  # :nodoc:
        [_get_schema(from_schema_), _get_schema(to_schema_)]
      end

      def _get_schema(schema_)  # :nodoc:
        schema_ = Format.get(schema_, true) if schema_.kind_of?(::String) || schema_.kind_of?(::Symbol)
        schema_ = schema_.schema if schema_.respond_to?(:schema)
        schema_ = schema_.root_field if schema_.respond_to?(:root_field)
        schema_
      end


    end

  end


end
