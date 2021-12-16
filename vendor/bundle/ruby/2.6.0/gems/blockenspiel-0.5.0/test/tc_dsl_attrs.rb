# -----------------------------------------------------------------------------
#
# Blockenspiel dsl attribute tests
#
# This file contains tests for the dsl attribute directives.
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

    class TestDSLAttrs < ::Minitest::Test  # :nodoc:


      class WriterTarget < ::Blockenspiel::Base

        dsl_attr_writer(:attr1, :attr2)

      end


      class AccessorTarget < ::Blockenspiel::Base

        dsl_attr_accessor(:attr1, :attr2)

      end


      # Test dsl attr writer in a parametered block
      #
      # * Asserts that the standard setter syntax works
      # * Asserts that the alternate setter syntax works

      def test_writer_parametered
        block_ = ::Proc.new do |param_|
          param_.attr1 = 1
          assert_equal(2, param_.attr2(2))
        end
        target_ = WriterTarget.new
        ::Blockenspiel.invoke(block_, target_)
        assert_equal(1, target_.instance_variable_get(:@attr1))
        assert_equal(2, target_.instance_variable_get(:@attr2))
      end


      # Test dsl attr writer in a parameterless block
      #
      # * Asserts that the alternate setter syntax works

      def test_writer_parameterless
        block_ = ::Proc.new do
          assert_equal(2, attr2(2))
        end
        target_ = WriterTarget.new
        ::Blockenspiel.invoke(block_, target_)
        assert_equal(false, target_.instance_variable_defined?(:@attr1))
        assert_equal(2, target_.instance_variable_get(:@attr2))
      end


      # Test dsl attr accessor in a parametered block
      #
      # * Asserts that the standard setter syntax works
      # * Asserts that the alternate setter syntax works
      # * Asserts that the getter syntax works

      def _test_accessor_parametered
        block_ = ::Proc.new do |param_|
          param_.attr1 = 1
          assert_equal(2, param_.attr2(2))
          assert_equal(2, param_.attr2)
        end
        target_ = AccessorTarget.new
        ::Blockenspiel.invoke(block_, target_)
        assert_equal(1, target_.instance_variable_get(:@attr1))
        assert_equal(2, target_.instance_variable_get(:@attr2))
      end


      # Test dsl attr accessor in a parameterless block
      #
      # * Asserts that the alternate setter syntax works
      # * Asserts that the getter syntax works

      def test_accessor_parameterless
        block_ = ::Proc.new do
          assert_equal(2, attr2(2))
          assert_equal(2, attr2)
        end
        target_ = AccessorTarget.new
        ::Blockenspiel.invoke(block_, target_)
        assert_equal(false, target_.instance_variable_defined?(:@attr1))
        assert_equal(2, target_.instance_variable_get(:@attr2))
      end


    end

  end
end
