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


    # The base conversion class.
    #
    # This base class defines the API for a conversion. All conversions must
    # define the method <tt>convert_value</tt> documented here. Conversions
    # need not actually extend this base class, as long as they duck-type
    # this method. However, this base class does provide a few convenience
    # methods such as a sane implementation of inspect.

    class Base


      # Create a conversion using a simple DSL.
      # You can pass a block to the initializer that takes the same
      # parameters as convert_value, and the conversion will use that block
      # to perform the conversion.

      def initialize(&block_)
        @_converter = block_
      end


      # Convert the given value to the given format and return the converted
      # value.
      #
      # The convert_params may be interpreted however the particular
      # conversion wishes.
      #
      # Raises Versionomy::Errors::ConversionError if the conversion failed.

      def convert_value(value_, format_, convert_params_=nil)
        if @_converter
          @_converter.call(value_, format_, convert_params_)
        else
          raise Errors::ConversionError, "Conversion not implemented"
        end
      end


      # Inspect this conversion.

      def inspect
        "#<#{self.class}:0x#{object_id.to_s(16)}>"
      end


      # The default to_s implementation just calls inspect.

      def to_s
        inspect
      end


    end


  end

end
