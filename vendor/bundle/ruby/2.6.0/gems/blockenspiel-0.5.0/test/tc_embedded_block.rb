# -----------------------------------------------------------------------------
#
# Blockenspiel behavior tests
#
# This file contains tests for behavior settings.
#
# -----------------------------------------------------------------------------
# Copyright 2008-2011 Daniel Azuma
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


require 'minitest/autorun'
require 'blockenspiel'


module Blockenspiel
  module Tests  # :nodoc:

    class TestEmbeddedBlock < ::Minitest::Test  # :nodoc:


      class Target1 < ::Blockenspiel::Base

        def initialize(value_)
          @value = value_
          @block = nil
        end

        def set_block(&block_)
          @block = block_
        end

        def value
          @value
        end

        dsl_methods false

        def call_block
          @block.call
        end

      end


      BLOCK = ::Proc.new do
        set_block do
          self.value
        end
      end


      # Test an embedded block with a proxy.

      def test_proxy_embedded_block
        if false  # TEMP
          target_ = Target1.new(23)
          ::Blockenspiel.invoke(BLOCK, target_, :parameterless => :proxy)
          assert_equal(23, target_.call_block)
        end
      end


    end

  end
end
