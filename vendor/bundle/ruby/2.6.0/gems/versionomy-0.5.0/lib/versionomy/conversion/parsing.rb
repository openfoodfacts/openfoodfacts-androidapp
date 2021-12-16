# -----------------------------------------------------------------------------
#
# Versionomy conversion base class
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

  module Conversion


    # A conversion strategy that relies on parsing.
    # Essentially, it unparses the value and then attempts to parse it with
    # the new format.

    class Parsing < Base


      # Create a parsing conversion.
      #
      # By default, this just unparses and reparses using the default
      # parse settings. In some cases, this may be enough, but you may
      # wish to improve the reliability of the conversion by tweaking the
      # parsing settings. To do so, pass a block to the new method, and
      # call methods of Versionomy::Conversion::Parsing::Builder in that
      # block.

      def initialize(&block_)
        if block_
          builder_ = Builder.new
          ::Blockenspiel.invoke(block_, builder_)
          @original_value_modifier = builder_._get_original_value_modifier
          @string_modifier = builder_._get_string_modifier
          @unparse_params_modifier = builder_._get_unparse_params_modifier
          @parse_params_generator ||= builder_._get_parse_params_generator
        end
      end


      # Returns a value equivalent to the given value in the given format.
      #
      # The convert_params are passed to this conversion's customization
      # blocks (if any).
      #
      # Raises Versionomy::Errors::ConversionError if the conversion failed.
      # Typically, this is due to a failure of the parsing or unparsing.

      def convert_value(value_, format_, convert_params_=nil)
        begin
          convert_params_ ||= {}
          if @original_value_modifier
            value_ = @original_value_modifier.call(value_, convert_params_)
          end
          unparse_params_ = value_.unparse_params || {}
          if @unparse_params_modifier
            unparse_params_ = @unparse_params_modifier.call(unparse_params_, convert_params_)
          end
          string_ = value_.unparse(unparse_params_)
          if @string_modifier
            string_ = @string_modifier.call(string_, convert_params_)
          end
          if @parse_params_generator
            parse_params_ = @parse_params_generator.call(convert_params_)
          else
            parse_params_ = nil
          end
          new_value_ = format_.parse(string_, parse_params_)
          return new_value_
        rescue Errors::UnparseError => ex_
          raise Errors::ConversionError, "Unparsing failed: #{ex_.inspect}"
        rescue Errors::ParseError => ex_
          raise Errors::ConversionError, "Parsing failed: #{ex_.inspect}"
        end
      end


      # Call methods of this class in the block passed to
      # Versionomy::Conversion::Parsing#new to fine-tune the behavior of
      # the converter.

      class Builder

        include ::Blockenspiel::DSL


        def initialize  # :nodoc:
          @original_value_modifier = nil
          @string_modifier = nil
          @parse_params_generator = nil
          @unparse_params_modifier = nil
        end


        # Provide a block that generates the params used to parse the new
        # value. The block should take one parameter, the convert_params
        # passed to convert_value (which may be nil). It should return the
        # parse params that should be used.

        def to_generate_parse_params(&block_)
          @parse_params_generator = block_
        end


        # Provide a block that can modify the params used to unparse the
        # old value. The block should take two parameters: first, the
        # original unparse params from the old value (which may be nil),
        # and second, the convert_params passed to convert_value (which
        # may also be nil). It should return the unparse params that
        # should actually be used.

        def to_modify_unparse_params(&block_)
          @unparse_params_modifier = block_
        end


        # Provide a block that can modify the original value prior to it
        # being unparsed. The block should take two parameters: first, the
        # original value to be converted, and second, the convert_params
        # passed to convert_value (which may be nil). It should return the
        # value to be unparsed, which may be the same as the value
        # originally passed in. This method may fail-fast by raising a
        # Versionomy::Errors::ConversionError if it determines that the
        # value passed in cannot be converted as is.

        def to_modify_original_value(&block_)
          @original_value_modifier = block_
        end


        # Provide a block that can modify the unparsed string prior to
        # it being passed to the parser. The block should take two
        # parameters: first, the string resulting from unparsing the old
        # value, and second, the convert_params passed to convert_value
        # (which may be nil). It should return the string to be parsed to
        # get the new value.

        def to_modify_string(&block_)
          @string_modifier = block_
        end


        def _get_original_value_modifier  # :nodoc:
          @original_value_modifier
        end

        def _get_string_modifier  # :nodoc:
          @string_modifier
        end

        def _get_unparse_params_modifier  # :nodoc:
          @unparse_params_modifier
        end

        def _get_parse_params_generator  # :nodoc:
          @parse_params_generator
        end

      end


    end


  end

end
