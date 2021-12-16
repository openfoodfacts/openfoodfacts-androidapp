# -----------------------------------------------------------------------------
#
# Blockenspiel module tests
#
# This file contains tests for DSL module inclusion.
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

    class TestModules < ::Minitest::Test  # :nodoc:


      class Target1
        include ::Blockenspiel::DSL

        def initialize
          @hash = ::Hash.new
        end

        def set_value(key_, value_)
          @hash[key_] = value_
        end

        def _helper_method(key_, value_)
          @hash[key_] = value_
        end

        def get_value(key_)
          @hash[key_]
        end
        dsl_method :get_value, false

        def get_value2(key_)
          @hash[key_]
        end
        dsl_method :get_value2, false

      end


      module Module2

        def set_value(key_, value_)
          @hash[key_] = value_
        end

        def _get_value(key_)
          @hash[key_]
        end

      end

      class Target2a
        include ::Blockenspiel::DSL

        def initialize
          @hash = ::Hash.new
        end

        include Module2
      end

      class Target2b
        include ::Blockenspiel::DSL

        def initialize
          @hash = ::Hash.new
        end
      end

      class Target2c < Target2b
        include Module2
      end


      # Helper method

      def get_value(key_)
        return :helper
      end


      # Test simple usage.
      #
      # * Asserts that methods are mixed in to self.
      # * Asserts that methods are removed from self afterward.
      # * Asserts that the specified target object still receives the messages.

      def test_simple_target
        block_ = ::Proc.new do
          set_value(:a, 1)
        end
        target_ = Target1.new
        ::Blockenspiel.invoke(block_, target_)
        assert(!self.respond_to?(:set_value))
        assert_equal(1, target_.get_value(:a))
      end


      # Test omissions.
      #
      # * Asserts that underscore methods are not mixed in.
      # * Asserts that methods that are turned off after the fact cannot be called.

      def test_omissions
        block_ = ::Proc.new do
          set_value(:a, 1)
          assert(!self.respond_to?(:_helper_method))
          assert_equal(:helper, get_value(:a))
          assert_raises(::NoMethodError) do
            get_value2(:a)
          end
        end
        target_ = Target1.new
        ::Blockenspiel.invoke(block_, target_)
      end


      # Test module inclusion.
      #
      # * Asserts that methods from an included module are handled.

      def test_simple_module_inclusion
        block_ = ::Proc.new do
          set_value(:a, 1)
          assert(!self.respond_to?(:_get_value))
        end
        target_ = Target2a.new
        ::Blockenspiel.invoke(block_, target_)
        assert_equal(1, target_._get_value(:a))
      end


      # Test module inclusion from a subclass
      #
      # * Asserts that a module can be included from a DSL subclass.

      def test_simple_module_inclusion_from_subclass
        block_ = ::Proc.new do
          set_value(:a, 1)
          assert(!self.respond_to?(:_get_value))
        end
        target_ = Target2c.new
        ::Blockenspiel.invoke(block_, target_)
        assert_equal(1, target_._get_value(:a))
      end


    end

  end
end
